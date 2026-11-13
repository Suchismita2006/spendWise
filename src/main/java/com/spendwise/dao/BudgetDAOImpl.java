package com.spendwise.dao;

import com.spendwise.exception.DatabaseException;
import com.spendwise.model.Budget;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * JDBC implementation of IBudgetDAO for monthly spending boundary persistence.
 */
public class BudgetDAOImpl extends BaseDAO implements IBudgetDAO {
    private static final Map<Integer, Budget> memBudgets = new ConcurrentHashMap<>();
    private static final AtomicInteger idGen = new AtomicInteger(500);

    @Override
    public Budget upsert(Budget budget) {
        if (!isDBAvailable()) {
            Optional<Budget> existing = memBudgets.values().stream()
                    .filter(b -> b.getUserId() == budget.getUserId()
                            && b.getCategory().equalsIgnoreCase(budget.getCategory())
                            && b.getMonth() == budget.getMonth()
                            && b.getYear() == budget.getYear())
                    .findFirst();

            if (existing.isPresent()) {
                Budget b = existing.get();
                b.setMonthlyLimit(budget.getMonthlyLimit());
                return b;
            } else {
                int newId = idGen.incrementAndGet();
                budget.setId(newId);
                memBudgets.put(newId, budget);
                return budget;
            }
        }

        String sql = "INSERT INTO budgets (user_id, category, monthly_limit, month, year) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE monthly_limit = VALUES(monthly_limit), updated_at = CURRENT_TIMESTAMP";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, budget.getUserId());
            ps.setString(2, budget.getCategory());
            ps.setDouble(3, budget.getMonthlyLimit());
            ps.setInt(4, budget.getMonth());
            ps.setInt(5, budget.getYear());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    budget.setId(rs.getInt(1));
                } else if (budget.getId() == 0) {
                    // If updated existing key, fetch the ID
                    Optional<Budget> found = findByUserCategoryPeriod(
                            budget.getUserId(), budget.getCategory(), budget.getMonth(), budget.getYear());
                    found.ifPresent(b -> budget.setId(b.getId()));
                }
            }
            return budget;
        } catch (SQLException e) {
            throw new DatabaseException("JDBC Error upserting budget: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Budget> findById(int id) {
        if (!isDBAvailable()) {
            return Optional.ofNullable(memBudgets.get(id));
        }

        String sql = "SELECT budget_id, user_id, category, monthly_limit, month, year, created_at, updated_at " +
                "FROM budgets WHERE budget_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToBudget(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("JDBC Error fetching budget by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Budget> findByUserCategoryPeriod(int userId, String category, int month, int year) {
        if (!isDBAvailable()) {
            return memBudgets.values().stream()
                    .filter(b -> b.getUserId() == userId
                            && b.getCategory().equalsIgnoreCase(category.trim())
                            && b.getMonth() == month
                            && b.getYear() == year)
                    .findFirst();
        }

        String sql = "SELECT budget_id, user_id, category, monthly_limit, month, year, created_at, updated_at " +
                "FROM budgets WHERE user_id = ? AND LOWER(category) = ? AND month = ? AND year = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, category.trim().toLowerCase());
            ps.setInt(3, month);
            ps.setInt(4, year);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToBudget(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("JDBC Error fetching budget by user and category: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Budget> findByUserAndPeriod(int userId, int month, int year) {
        if (!isDBAvailable()) {
            return memBudgets.values().stream()
                    .filter(b -> b.getUserId() == userId && b.getMonth() == month && b.getYear() == year)
                    .sorted(Comparator.comparing(Budget::getCategory))
                    .collect(Collectors.toList());
        }

        String sql = "SELECT budget_id, user_id, category, monthly_limit, month, year, created_at, updated_at " +
                "FROM budgets WHERE user_id = ? AND month = ? AND year = ? ORDER BY category ASC";
        List<Budget> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, month);
            ps.setInt(3, year);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToBudget(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("JDBC Error listing budgets for user: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int id) {
        if (!isDBAvailable()) {
            return memBudgets.remove(id) != null;
        }

        String sql = "DELETE FROM budgets WHERE budget_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("JDBC Error deleting budget: " + e.getMessage(), e);
        }
    }

    private Budget mapResultSetToBudget(ResultSet rs) throws SQLException {
        Timestamp created = rs.getTimestamp("created_at");
        Timestamp updated = rs.getTimestamp("updated_at");

        return new Budget(
                rs.getInt("budget_id"),
                rs.getInt("user_id"),
                rs.getString("category"),
                rs.getDouble("monthly_limit"),
                rs.getInt("month"),
                rs.getInt("year"),
                created != null ? created.toLocalDateTime() : null,
                updated != null ? updated.toLocalDateTime() : null
        );
    }
}
