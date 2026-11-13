package com.spendwise.service;

import com.spendwise.model.User;

/**
 * OOP Pillar: ABSTRACTION
 * Interface declaring business operations for user authentication and profile management.
 */
public interface IUserService {
    User register(String fullName, String email, String password);
    User login(String email, String password);
    User getUserById(int userId);
    boolean updateProfile(int userId, String fullName, String currency);
}
