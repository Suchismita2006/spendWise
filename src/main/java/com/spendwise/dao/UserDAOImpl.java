package com.spendwise.dao;

import com.spendwise.exception.DatabaseException;
import com.spendwise.model.User;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * JDBC implementation of IUserDAO communicating directly with MySQL.
 * Includes in-memory fallback store if MySQL service is offline.
 */
public class UserDAOImpl extends BaseDAO implements IUserDAO {
    // In-memory fallback repository
    private static final Map<Integer, User> memUsers = new ConcurrentHashMap<>();
    private static final Map<String, Integer> memEmailIndex = new ConcurrentHashMap<>();
    private static final AtomicInteger idGen = new AtomicInteger(100);

    @Override
    public User create(User user) {
        if (!isDBAvailable()) {
            int newId = idGen.incrementAndGet();
            user.setId(newId);
            memUsers.put(newId, user);
            memEmailIndex.put(user.getEmail().toLowerCase(), newId);
            return user;
        }

        String sql = "INSERT INTO users (full_name, email, password_hash, salt, currency) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail().toLowerCase());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getSalt());
            ps.setString(5, user.getCurrency());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new DatabaseException("Creating user failed, no rows affected.", null);
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setId(rs.getInt(1));
                }
            }
            return user;
        } catch (SQLException e) {
            throw new DatabaseException("JDBC Error creating user: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> findById(int id) {
        if (!isDBAvailable()) {
            return Optional.ofNullable(memUsers.get(id));
        }

        String sql = "SELECT user_id, full_name, email, password_hash, salt, currency, created_at, updated_at FROM users WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("JDBC Error fetching user by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null) return Optional.empty();
        String normalizedEmail = email.trim().toLowerCase();

        if (!isDBAvailable()) {
            Integer id = memEmailIndex.get(normalizedEmail);
            if (id != null) {
                return Optional.ofNullable(memUsers.get(id));
            }
            return Optional.empty();
        }

        String sql = "SELECT user_id, full_name, email, password_hash, salt, currency, created_at, updated_at FROM users WHERE LOWER(email) = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, normalizedEmail);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("JDBC Error fetching user by email: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null) return false;
        String normalizedEmail = email.trim().toLowerCase();

        if (!isDBAvailable()) {
            return memEmailIndex.containsKey(normalizedEmail);
        }

        String sql = "SELECT 1 FROM users WHERE LOWER(email) = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, normalizedEmail);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("JDBC Error checking email existence: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(User user) {
        if (!isDBAvailable()) {
            memUsers.put(user.getId(), user);
            memEmailIndex.put(user.getEmail().toLowerCase(), user.getId());
            return true;
        }

        String sql = "UPDATE users SET full_name = ?, email = ?, password_hash = ?, salt = ?, currency = ? WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail().toLowerCase());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getSalt());
            ps.setString(5, user.getCurrency());
            ps.setInt(6, user.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("JDBC Error updating user: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int id) {
        if (!isDBAvailable()) {
            User u = memUsers.remove(id);
            if (u != null) {
                memEmailIndex.remove(u.getEmail().toLowerCase());
                return true;
            }
            return false;
        }

        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("JDBC Error deleting user: " + e.getMessage(), e);
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        Timestamp createdTs = rs.getTimestamp("created_at");
        Timestamp updatedTs = rs.getTimestamp("updated_at");

        return new User(
                rs.getInt("user_id"),
                rs.getString("full_name"),
                rs.getString("email"),
                rs.getString("password_hash"),
                rs.getString("salt"),
                rs.getString("currency"),
                createdTs != null ? createdTs.toLocalDateTime() : null,
                updatedTs != null ? updatedTs.toLocalDateTime() : null
        );
    }
}
