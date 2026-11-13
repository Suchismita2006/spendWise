package com.spendwise.dao;

import com.spendwise.model.AbstractTransaction;
import com.spendwise.model.TransactionType;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * OOP Pillar: ABSTRACTION
 * Interface contract defining polymorphic transaction persistence and aggregation queries.
 */
public interface ITransactionDAO {
    AbstractTransaction create(AbstractTransaction tx);
    Optional<AbstractTransaction> findById(int id);
    List<AbstractTransaction> findByUserId(int userId);
    List<AbstractTransaction> findFiltered(int userId, TransactionType type, String category,
                                          LocalDate startDate, LocalDate endDate, String search);
    List<AbstractTransaction> findByMonthAndYear(int userId, int month, int year);
    boolean update(AbstractTransaction tx);
    boolean delete(int id);
    double getTotalByTypeAndMonth(int userId, TransactionType type, int month, int year);
    Map<String, Double> getCategoryTotals(int userId, TransactionType type, int month, int year);
}
