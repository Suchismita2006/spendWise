package com.spendwise.dao;

import com.spendwise.model.Budget;
import java.util.List;
import java.util.Optional;

/**
 * OOP Pillar: ABSTRACTION
 * Interface contract defining budget management and persistence operations.
 */
public interface IBudgetDAO {
    Budget upsert(Budget budget);
    Optional<Budget> findById(int id);
    Optional<Budget> findByUserCategoryPeriod(int userId, String category, int month, int year);
    List<Budget> findByUserAndPeriod(int userId, int month, int year);
    boolean delete(int id);
}
