
package com.oop.project.service;

import com.oop.project.model.User;
import com.oop.project.repository.LoginLogRepository;
import com.oop.project.repository.UserRepository;

public class AuthService {
    
    private final UserRepository userRepository = new UserRepository();
    private final LoginLogRepository loginLogRepository = new LoginLogRepository();

    public User authenticate(String username, String password) {
        User user = userRepository.login(username, password);
        if (user != null) {
            loginLogRepository.log(user.getId(), "LOGIN");

        }
        return user;
    }

    public void logout(User user) {
        loginLogRepository.log(user.getId(), "LOGOUT");
    }
}
