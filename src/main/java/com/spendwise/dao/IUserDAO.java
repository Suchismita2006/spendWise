package com.spendwise.dao;

import com.spendwise.model.User;
import java.util.Optional;

/**
 * OOP Pillar: ABSTRACTION
 * Interface contract defining user persistence operations.
 */
public interface IUserDAO {
    User create(User user);
    Optional<User> findById(int id);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean update(User user);
    boolean delete(int id);
}
