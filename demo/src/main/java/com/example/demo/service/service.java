package com.example.projectname.service;

import com.example.projectname.model.User;
import com.example.projectname.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Create a new user.
     */
    public User createUser(String username, String email, String rawPassword) {
        // Check if email or username is already taken
        if (userRepository.findByEmail(email).isPresent() || userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Email or Username already exists");
        }

        // Encrypt the password
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Save the user
        User user = User.builder()
                .username(username)
                .email(email)
                .password(encodedPassword)
                .build();
        return userRepository.save(user);
    }

    /**
     * Authenticate a user by username/email and password.
     */
    public User authenticateUser(String usernameOrEmail, String rawPassword) {
        Optional<User> userOptional = userRepository.findByEmail(usernameOrEmail)
                .or(() -> userRepository.findByUsername(usernameOrEmail));

        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOptional.get();
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        return user;
    }
}
