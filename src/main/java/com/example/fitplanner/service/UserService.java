package com.example.fitplanner.service;

import com.example.fitplanner.dto.*;
import com.example.fitplanner.entity.model.ExerciseProgress;
import com.example.fitplanner.util.UnitValidator;
import com.example.fitplanner.entity.enums.Role;
import com.example.fitplanner.entity.model.User;
import com.example.fitplanner.repository.UserRepository;
import com.example.fitplanner.util.SHA256Hasher;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Transactional
@Service
public class UserService {
    final private UserRepository userRepository;
    final private ModelMapper modelMapper;
    final private UnitValidator unitValidator;
    final private SHA256Hasher hasher;

    private final double KG_TO_LB = 2.20462262;

    @Autowired
    public UserService(UserRepository userRepository,
                       ModelMapper modelMapper,
                       UnitValidator unitValidator, SHA256Hasher encoder){
        this.userRepository = userRepository;
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
        }
        if(!unitValidator.isValidEmail(userLoginDto.getUsernameOrEmail())
        && userRepository.getByUsernameAndPassword(userLoginDto.getUsernameOrEmail(), hashedPassword).isEmpty()){
            bindingResult.rejectValue("usernameOrEmail", "", "Username and Password do not match");
        }
    }

    public void save(UserRegisterDto userRegisterDto) {
        String hashedPassword = hasher.hash(userRegisterDto.getPassword());
        userRegisterDto.setRole(determineRole(userRegisterDto.getUsername()));
        userRegisterDto.setPassword(hashedPassword);
        User user = modelMapper.map(userRegisterDto, User.class);
        userRepository.save(user);
    }

    private Role determineRole(String username) {
        if(username.contains("ADMIN")) return Role.ADMIN;
        else if(username.contains("TRAINER")) return Role.TRAINER;
        else return Role.CLIENT;
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
}