package com.example.fitplanner.service;

import com.example.fitplanner.dto.ProfileUserDto;
import com.example.fitplanner.util.UnitValidator;
import com.example.fitplanner.dto.UserLoginDto;
import com.example.fitplanner.dto.UserRegisterDto;
import com.example.fitplanner.entity.enums.Role;
import com.example.fitplanner.entity.model.User;
import com.example.fitplanner.repository.UserRepository;
import com.example.fitplanner.util.SHA256Hasher;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

@Transactional
@Service
public class UserService {
    final private UserRepository userRepository;
    final private ModelMapper modelMapper;
    final private UnitValidator unitValidator;
    final private SHA256Hasher hasher;

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
        if(unitValidator.isValidEmail(userLoginDto.getUsernameOrEmail())
                && userRepository.getByEmailAndPassword(userLoginDto.getUsernameOrEmail(), hasher.hash(userLoginDto.getPassword())).isEmpty()){
            bindingResult.rejectValue("usernameOrEmail", "", "Email and Password do not match");
        }
        if(!unitValidator.isValidEmail(userLoginDto.getUsernameOrEmail())
        && userRepository.getByUsernameAndPassword(userLoginDto.getUsernameOrEmail(), hasher.hash(userLoginDto.getPassword())).isEmpty()){
            bindingResult.rejectValue("usernameOrEmail", "", "Username and Password do not match");
        }
    }

    public void save(UserRegisterDto userRegisterDto) {
        userRegisterDto.setRole(determineRole(userRegisterDto.getUsername()));
        userRegisterDto.setPassword(hasher.hash(userRegisterDto.getPassword()));
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
        User user = getById(id);
        System.out.println(user);
        System.out.println(modelMapper.map(user, dtoType));
        return modelMapper.map(user, dtoType);
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

//    public void updateUserSettings(UserDto userDto) {
//        User user = userRepository.findById(userDto.getId())
//                .orElseThrow(() -> new EntityNotFoundException("User not found"));
//        user.setTheme(userDto.getTheme());
//        user.setLanguage(userDto.getLanguage());
//        user.setMeasuringUnits(userDto.getMeasuringUnits());
//    }

    private User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid ID"));
    }

    public void updateProfile(ProfileUserDto profileDto) {
        User user = userRepository.findById(profileDto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid ID"));
        user.setFirstName(profileDto.getFirstName());
        user.setLastName(profileDto.getLastName());

        user.setProfileImageUrl(profileDto.getProfileImageUrl());

        user.setAge(profileDto.getAge());
        user.setWeight(profileDto.getWeight());

        user.setTheme(profileDto.getTheme());
        user.setLanguage(profileDto.getLanguage());
        user.setMeasuringUnits(profileDto.getMeasuringUnits());
        userRepository.save(user);
    }
}