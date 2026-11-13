package com.spendwise.service;

import com.spendwise.dao.IUserDAO;
import com.spendwise.dao.UserDAOImpl;
import com.spendwise.exception.AuthenticationException;
import com.spendwise.exception.ResourceNotFoundException;
import com.spendwise.model.User;
import com.spendwise.util.PasswordUtil;
import com.spendwise.util.ValidationUtil;

/**
 * Service implementation managing user registration, authentication, and secure password hashing.
 */
public class UserServiceImpl implements IUserService {
    private final IUserDAO userDAO;

    public UserServiceImpl() {
        this(new UserDAOImpl());
    }

    public UserServiceImpl(IUserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public User register(String fullName, String email, String password) {
        ValidationUtil.validateName(fullName);
        ValidationUtil.validateEmail(email);
        ValidationUtil.validatePasswordStrength(password);

        if (userDAO.existsByEmail(email)) {
            throw new AuthenticationException("An account with email '" + email + "' already exists.");
        }

        String salt = PasswordUtil.generateSalt();
        String passwordHash = PasswordUtil.hashPassword(password, salt);

        User newUser = new User(fullName, email, passwordHash, salt);
        return userDAO.create(newUser);
    }

    @Override
    public User login(String email, String password) {
        ValidationUtil.validateEmail(email);
        if (password == null || password.isEmpty()) {
            throw new AuthenticationException("Password cannot be blank.");
        }

        User user = userDAO.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException("No account found associated with email: " + email));

        boolean valid = PasswordUtil.verifyPassword(password, user.getSalt(), user.getPasswordHash());
        if (!valid) {
            throw new AuthenticationException("Invalid password. Please check your credentials.");
        }

        return user;
    }

    @Override
    public User getUserById(int userId) {
        return userDAO.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with ID " + userId + " does not exist."));
    }

    @Override
    public boolean updateProfile(int userId, String fullName, String currency) {
        User user = getUserById(userId);
        if (fullName != null && !fullName.trim().isEmpty()) {
            user.setFullName(fullName);
        }
        if (currency != null && !currency.trim().isEmpty()) {
            user.setCurrency(currency);
        }
        return userDAO.update(user);
    }
}
