
package com.oop.project.service;

import com.oop.project.dao.LoginLogDAO;
import com.oop.project.dao.UserDAO;
import com.oop.project.model.User;

public class AuthService {
    
    private final UserDAO userDAO = new UserDAO();
    private final LoginLogDAO logDAO = new LoginLogDAO();

    public User authenticate(String username, String password) {
        User user = userDAO.login(username, password);
        if (user != null) {
            logDAO.log(user.getId(), "LOGIN");

        }
        return user;
    }

    public void logout(User user) {
        logDAO.log(user.getId(), "LOGOUT");
    }
}
