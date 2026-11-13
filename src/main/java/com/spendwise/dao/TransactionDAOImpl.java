package com.spendwise.dao;

import com.spendwise.exception.DatabaseException;
import com.spendwise.model.AbstractTransaction;
import com.spendwise.model.TransactionFactory;
import com.spendwise.model.TransactionType;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * JDBC implementation of ITransactionDAO executing parameterized queries against MySQL.
 */
public class TransactionDAOImpl extends BaseDAO implements ITransactionDAO {
    private static final Map<Integer, AbstractTransaction> memTransactions = new ConcurrentHashMap<>();
    private static final AtomicInteger idGen = new AtomicInteger(1000);

    @Override
    public AbstractTransaction create(AbstractTransaction tx) {
        if (!isDBAvailable()) {
            int newId = idGen.incrementAndGet();
            tx.setId(newId);
            memTransactions.put(newId, tx);
            return tx;
        }

        String sql = "INSERT INTO transactions (user_id, type, category, amount, description, transaction_date) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, tx.getUserId());
            ps.setString(2, tx.getType().name());
            ps.setString(3, tx.getCategory());
            ps.setDouble(4, tx.getAmount());
            ps.setString(5, tx.getDescription());
            ps.setDate(6, java.sql.Date.valueOf(tx.getTransactionDate()));

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    tx.setId(rs.getInt(1));
                }
            }
            return tx;
        } catch (SQLException e) {
            throw new DatabaseException("JDBC Error inserting transaction: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<AbstractTransaction> findById(int id) {
        if (!isDBAvailable()) {
            return Optional.ofNullable(memTransactions.get(id));
        }

        String sql = "SELECT transaction_id, user_id, type, category, amount, description, transaction_date, " +
                "created_at, updated_at FROM transactions WHERE transaction_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTransaction(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("JDBC Error fetching transaction by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<AbstractTransaction> findByUserId(int userId) {
        return findFiltered(userId, null, null, null, null, null);
    }

    @Override
    public List<AbstractTransaction> findFiltered(int userId, TransactionType type, String category,
                                                 LocalDate startDate, LocalDate endDate, String search) {
        if (!isDBAvailable()) {
            return memTransactions.values().stream()
                    .filter(t -> t.getUserId() == userId)
                    .filter(t -> type == null || t.getType() == type)
                    .filter(t -> category == null || category.trim().isEmpty() || "ALL".equalsIgnoreCase(category)
                            || t.getCategory().equalsIgnoreCase(category.trim()))
                    .filter(t -> startDate == null || !t.getTransactionDate().isBefore(startDate))
                    .filter(t -> endDate == null || !t.getTransactionDate().isAfter(endDate))
                    .filter(t -> search == null || search.trim().isEmpty()
                            || (t.getDescription() != null && t.getDescription().toLowerCase().contains(search.toLowerCase()))
                            || t.getCategory().toLowerCase().contains(search.toLowerCase()))
                    .sorted((a, b) -> b.getTransactionDate().compareTo(a.getTransactionDate()))
                    .collect(Collectors.toList());
        }

        StringBuilder sql = new StringBuilder("SELECT transaction_id, user_id, type, category, amount, description, " +
                "transaction_date, created_at, updated_at FROM transactions WHERE user_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(userId);

        if (type != null) {
            sql.append(" AND type = ?");
            params.add(type.name());
        }
        if (category != null && !category.trim().isEmpty() && !"ALL".equalsIgnoreCase(category)) {
            sql.append(" AND LOWER(category) = ?");
            params.add(category.trim().toLowerCase());
        }
        if (startDate != null) {
            sql.append(" AND transaction_date >= ?");
            params.add(java.sql.Date.valueOf(startDate));
        }
        if (endDate != null) {
            sql.append(" AND transaction_date <= ?");
            params.add(java.sql.Date.valueOf(endDate));
        }
        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (LOWER(description) LIKE ? OR LOWER(category) LIKE ?)");
            String term = "%" + search.trim().toLowerCase() + "%";
            params.add(term);
            params.add(term);
        }

        sql.append(" ORDER BY transaction_date DESC, transaction_id DESC");

        List<AbstractTransaction> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToTransaction(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("JDBC Error filtering transactions: " + e.getMessage(), e);
        }
    }

    @Override
    public List<AbstractTransaction> findByMonthAndYear(int userId, int month, int year) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        return findFiltered(userId, null, null, start, end, null);
    }

    @Override
    public boolean update(AbstractTransaction tx) {
        if (!isDBAvailable()) {
            if (memTransactions.containsKey(tx.getId())) {
                memTransactions.put(tx.getId(), tx);
                return true;
            }
            return false;
        }

        String sql = "UPDATE transactions SET type = ?, category = ?, amount = ?, description = ?, " +
                "transaction_date = ? WHERE transaction_id = ? AND user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tx.getType().name());
            ps.setString(2, tx.getCategory());
            ps.setDouble(3, tx.getAmount());
            ps.setString(4, tx.getDescription());
            ps.setDate(5, java.sql.Date.valueOf(tx.getTransactionDate()));
            ps.setInt(6, tx.getId());
            ps.setInt(7, tx.getUserId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("JDBC Error updating transaction: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int id) {
        if (!isDBAvailable()) {
            return memTransactions.remove(id) != null;
        }

        String sql = "DELETE FROM transactions WHERE transaction_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("JDBC Error deleting transaction: " + e.getMessage(), e);
        }
    }

    @Override
    public double getTotalByTypeAndMonth(int userId, TransactionType type, int month, int year) {
        if (!isDBAvailable()) {
            return memTransactions.values().stream()
                    .filter(t -> t.getUserId() == userId && t.getType() == type)
                    .filter(t -> t.getTransactionDate().getMonthValue() == month && t.getTransactionDate().getYear() == year)
                    .mapToDouble(AbstractTransaction::getAmount)
                    .sum();
        }

        String sql = "SELECT COALESCE(SUM(amount), 0.0) AS total FROM transactions " +
                "WHERE user_id = ? AND type = ? AND MONTH(transaction_date) = ? AND YEAR(transaction_date) = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, type.name());
            ps.setInt(3, month);
            ps.setInt(4, year);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
            return 0.0;
        } catch (SQLException e) {
            throw new DatabaseException("JDBC Error computing monthly transaction totals: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Double> getCategoryTotals(int userId, TransactionType type, int month, int year) {
        Map<String, Double> map = new LinkedHashMap<>();

        if (!isDBAvailable()) {
            memTransactions.values().stream()
                    .filter(t -> t.getUserId() == userId && t.getType() == type)
                    .filter(t -> t.getTransactionDate().getMonthValue() == month && t.getTransactionDate().getYear() == year)
                    .forEach(t -> map.put(t.getCategory(), map.getOrDefault(t.getCategory(), 0.0) + t.getAmount()));
            return map;
        }

        String sql = "SELECT category, COALESCE(SUM(amount), 0.0) AS cat_total FROM transactions " +
                "WHERE user_id = ? AND type = ? AND MONTH(transaction_date) = ? AND YEAR(transaction_date) = ? " +
                "GROUP BY category ORDER BY cat_total DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, type.name());
            ps.setInt(3, month);
            ps.setInt(4, year);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("category"), rs.getDouble("cat_total"));
                }
            }
            return map;
        } catch (SQLException e) {
            throw new DatabaseException("JDBC Error fetching category totals: " + e.getMessage(), e);
        }
    }

    private AbstractTransaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        int id = rs.getInt("transaction_id");
        int userId = rs.getInt("user_id");
        TransactionType type = TransactionType.fromString(rs.getString("type"));
        String category = rs.getString("category");
        double amount = rs.getDouble("amount");
        String description = rs.getString("description");
        LocalDate date = rs.getDate("transaction_date").toLocalDate();
        Timestamp created = rs.getTimestamp("created_at");
        Timestamp updated = rs.getTimestamp("updated_at");

        return TransactionFactory.create(
                type,
                id,
                userId,
                amount,
                category,
                description,
                date,
                created != null ? created.toLocalDateTime() : null,
                updated != null ? updated.toLocalDateTime() : null
        );
    }
}
