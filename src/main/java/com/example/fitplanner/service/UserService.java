package com.example.fitplanner.service;

import com.example.fitplanner.dto.*;
import com.example.fitplanner.entity.model.ExerciseProgress;
import com.example.fitplanner.entity.model.Program;
import com.example.fitplanner.repository.ProgramRepository;
import com.example.fitplanner.util.UnitValidator;
import com.example.fitplanner.entity.enums.Role;
import com.example.fitplanner.entity.model.User;
import com.example.fitplanner.repository.UserRepository;
import com.example.fitplanner.util.SHA256Hasher;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Transactional
@Service
public class UserService {
    @Value("${app.security.admin-code}")
    private String adminSecret;

    @Value("${app.security.trainer-code}")
    private String trainerSecret;
    final private UserRepository userRepository;
    final private ProgramRepository programRepository;
    final private ModelMapper modelMapper;
    final private UnitValidator unitValidator;
    final private SHA256Hasher hasher;

    private final double KG_TO_LB = 2.20462262;

    @Autowired
    public UserService(UserRepository userRepository, ProgramRepository programRepository,
                       ModelMapper modelMapper,
                       UnitValidator unitValidator, SHA256Hasher encoder){
        this.userRepository = userRepository;
        this.programRepository = programRepository;
        this.modelMapper = modelMapper;
        this.unitValidator = unitValidator;
        this.hasher = encoder;
    }

    public void validateUserRegister(UserRegisterDto userRegisterDto, BindingResult bindingResult){
        if(!unitValidator.isValidUsername(userRegisterDto.getUsername())){
            bindingResult.rejectValue("username", "", "Invalid username format");
        }
        if(userRepository.getByUsername(userRegisterDto.getUsername()).isPresent()){
            bindingResult.rejectValue("username", "", "Username already exists");
        }
        if(userRepository.getByEmail(userRegisterDto.getEmail()).isPresent()){
            bindingResult.rejectValue("email", "", "Email already exists");
        }
        if(!userRegisterDto.getPassword().equals(userRegisterDto.getConfirmPassword())){
            bindingResult.rejectValue("confirmPassword", "", "Passwords do not match");
        }
    }

    public void validateUserLogin(UserLoginDto userLoginDto, BindingResult bindingResult){
        String hashedPassword = hasher.hash(userLoginDto.getPassword());
        if(unitValidator.isValidEmail(userLoginDto.getUsernameOrEmail())
                && userRepository.getByEmailAndPassword(userLoginDto.getUsernameOrEmail(), hashedPassword).isEmpty()){
            bindingResult.rejectValue("usernameOrEmail", "", "Email and Password do not match");
            return;
        }
        if(!unitValidator.isValidEmail(userLoginDto.getUsernameOrEmail())
        && userRepository.getByUsernameAndPassword(userLoginDto.getUsernameOrEmail(), hashedPassword).isEmpty()){
            bindingResult.rejectValue("usernameOrEmail", "", "Username and Password do not match");
            return;
        }
        User usernameUser = userRepository.findByUsername(userLoginDto.getUsernameOrEmail()).orElse(null);
        User emailUser = userRepository.findByEmail(userLoginDto.getUsernameOrEmail()).orElse(null);
        if(usernameUser != null && !usernameUser.getEnabled()) {
            bindingResult.rejectValue("usernameOrEmail", "", "User has been banned");
        }
        if(emailUser != null && !emailUser.getEnabled()) {
            bindingResult.rejectValue("usernameOrEmail", "", "User has been banned");
        }
    }

    public void save(UserRegisterDto userRegisterDto) {
// 1. Determine Role based on the Invite Code
        Role role = determineRole(userRegisterDto.getInviteCode());
        userRegisterDto.setRole(role);

        // 2. Hash Password
        String hashedPassword = hasher.hash(userRegisterDto.getPassword());
        userRegisterDto.setPassword(hashedPassword);

        // 3. Map and Save
        User user = modelMapper.map(userRegisterDto, User.class);
        userRepository.save(user);
    }

    private Role determineRole(String inviteCode) {
        // If code matches admin secret, they are ADMIN
        if (adminSecret.equals(inviteCode)) {
            return Role.ADMIN;
        }
        // If code matches trainer secret, they are TRAINER
        else if (trainerSecret.equals(inviteCode)) {
            return Role.TRAINER;
        }

        // Default role for everyone else
        return Role.CLIENT;
    }

    public void save(UserLoginDto userLoginDto) {
        User user = modelMapper.map(userLoginDto, User.class);
        userRepository.save(user);
    }

    public <T> T getById(Long id, Class<T> dtoType) {
        return getById(id, dtoType, false);
    }

    public <T> T getById(Long id, Class<T> dtoType, boolean convertToLbs) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        T toReturn = modelMapper.map(user, dtoType);

        if (convertToLbs) {
            if (toReturn instanceof ProfileUserDto profileUser) {
                double rawLbs = profileUser.getWeight() * KG_TO_LB;
                double roundedLbs = Math.round(rawLbs * 10.0) / 10.0;

                profileUser.setWeight(roundedLbs);
            }
        }
        return toReturn;
    }

    public Long getIdByUsernameOrEmail(String usernameOrEmail) {
        User user = null;
        if(unitValidator.isValidEmail(usernameOrEmail)){
            user = userRepository.getByEmail(usernameOrEmail).orElseThrow(() -> new IllegalArgumentException("Email not found"));
        }
        if(!unitValidator.isValidEmail(usernameOrEmail)){
            user = userRepository.getByUsername(usernameOrEmail).orElseThrow(() -> new IllegalArgumentException("Username not found"));
        }
        return user.getId();
    }

    public void updateProfile(ProfileUserDto profileDto) {
        User user = userRepository.findById(profileDto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid ID"));
        user.setFirstName(profileDto.getFirstName());
        user.setLastName(profileDto.getLastName());

        user.setProfileImageUrl(profileDto.getProfileImageUrl());

        user.setAge(profileDto.getAge());
        double weightToSave = profileDto.getWeight();
        if ("lbs".equals(profileDto.getMeasuringUnits())) {
            weightToSave = weightToSave / KG_TO_LB;
        }

        user.setWeight(weightToSave);

        user.setTheme(profileDto.getTheme());
        user.setLanguage(profileDto.getLanguage());
        user.setMeasuringUnits(profileDto.getMeasuringUnits());
        userRepository.save(user);
    }

    public List<WeightEntryDto> getWeightHistory(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<WeightEntryDto> history = new ArrayList<>();

        // Initial weight (from User entity)
        history.add(new WeightEntryDto(user.getCreatedAt(), user.getWeight()));

        // If you later have a separate weight update history table, you can add it here:
        // List<WeightUpdate> updates = weightUpdateRepository.findByUserId(userId);
        // for (WeightUpdate update : updates) {
        //     history.add(new WeightEntryDto(update.getDate(), update.getWeight()));
        // }

        return history;
    }

    public List<User> searchTrainers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return userRepository.findByRole(Role.TRAINER); // Return all if empty
        }
        return userRepository.searchTrainers(query);
    }

    // Inside UserService.java
    public List<TrainerSearchDto> searchTrainersAndMap(String query) {
        List<User> trainers = (query == null || query.isBlank())
                ? userRepository.findByRole(Role.TRAINER)
                : userRepository.searchTrainers(query);

        return trainers.stream()
                .map(user -> TrainerSearchDto.builder()
                        .id(user.getId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .username(user.getUsername())
                        .username(user.getUsername())
                        .profileImageUrl(user.getProfileImageUrl())
                        .experience("Certified Trainer") // Placeholder or logic based on user data
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void toggleStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow();

        // Flip the boolean
        user.setEnabled(!user.getEnabled());
        userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow();
        userRepository.delete(user);
    }

    public DetailedUserDto getDetailedUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow();

        // Map basic user details
        DetailedUserDto dto = modelMapper.map(user, DetailedUserDto.class);

        // Fetch and map programs specifically
        List<Program> programs = programRepository.findAllByUserId(id);
        List<DetailedProgramDto> programDtos = programs.stream()
                .map(p -> modelMapper.map(p, DetailedProgramDto.class))
                .collect(Collectors.toList());

        dto.setPrograms(new LinkedHashSet<>(programDtos)); // Using Set as per your DTO definition
        return dto;
    }

    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserDto> dtos = new ArrayList<>();
        for (User user : users) {
            dtos.add(modelMapper.map(user, UserDto.class));
        }
        return dtos;
    }
}