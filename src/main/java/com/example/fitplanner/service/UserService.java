package com.example.fitplanner.service;

import com.example.fitplanner.config.UnitValidator;
import com.example.fitplanner.dto.UserDto;
import com.example.fitplanner.dto.UserLoginDto;
import com.example.fitplanner.dto.UserRegisterDto;
import com.example.fitplanner.entity.enums.Role;
import com.example.fitplanner.entity.model.User;
import com.example.fitplanner.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private UnitValidator unitValidator;
    @Autowired
    private PasswordEncoder passwordEncoder;

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
                && userRepository.getByEmailAndPassword(userLoginDto.getUsernameOrEmail(), passwordEncoder.encode(userLoginDto.getPassword())).isEmpty()){
            bindingResult.rejectValue("usernameOrEmail", "", "Email and Password do not match");
        }
        if(!unitValidator.isValidEmail(userLoginDto.getUsernameOrEmail())
        && userRepository.getByUsernameAndPassword(userLoginDto.getUsernameOrEmail(), passwordEncoder.encode(userLoginDto.getPassword())).isEmpty()){
            bindingResult.rejectValue("usernameOrEmail", "", "Username and Password do not match");
        }
    }

    public void save(UserRegisterDto userRegisterDto) {
        User user = modelMapper.map(userRegisterDto, User.class);
        user.setRole(determineRole(user.getUsername()));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
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

    public UserDto getById(Long id){
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid ID"));
        return modelMapper.map(user, UserDto.class);
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

    @Transactional
    public void updateUserSettings(UserDto userDto) {
        if (userDto == null || userDto.getId() == null) return;

        User user = userRepository.findById(userDto.getId()).orElse(null);
        if (user != null) {
            user.setTheme(userDto.getTheme());
            user.setLanguage(userDto.getLanguage());
            userRepository.save(user);
        }
    }
}