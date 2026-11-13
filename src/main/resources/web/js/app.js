/**
 * SpendWise Dashboard Application Controller
 */

let currentUser = null;
let categoryCatalog = { incomeCategories: [], expenseCategories: [] };
let activeFilterTimeout = null;

document.addEventListener('DOMContentLoaded', async () => {
    // 1. Verify Authentication State
    const userJson = localStorage.getItem('spendwise_user');
    if (!userJson) {
        window.location.href = 'index.html';
        return;
    }
    currentUser = JSON.parse(userJson);

    // Populate Sidebar User Profile
    const nameEl = document.getElementById('user-name');
    const emailEl = document.getElementById('user-email');
    const avatarEl = document.getElementById('user-avatar');
    if (nameEl) nameEl.textContent = currentUser.fullName;
    if (emailEl) emailEl.textContent = currentUser.email;
    if (avatarEl) avatarEl.textContent = currentUser.fullName.charAt(0).toUpperCase();

    // Setup Logout
    const logoutBtn = document.getElementById('btn-logout');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', () => {
            localStorage.removeItem('spendwise_user');
            window.location.href = 'index.html';
        });
    }

    // 2. Initialize Month & Year Selectors
    initPeriodSelectors();

    // 3. Load Category Catalog from backend
    await loadCategories();

    // 4. Set Default Date in Add Transaction modal
    const dateInput = document.getElementById('tx-date');
    if (dateInput) {
        dateInput.value = new Date().toISOString().split('T')[0];
    }

    // 5. Initial Data Fetch
    await refreshDashboard();
});

function initPeriodSelectors() {
    const now = new Date();
    const monthSelect = document.getElementById('select-month');
    const yearSelect = document.getElementById('select-year');

    if (monthSelect) monthSelect.value = now.getMonth() + 1;
    if (yearSelect) yearSelect.value = now.getFullYear();
}

function getSelectedPeriod() {
    const monthSelect = document.getElementById('select-month');
    const yearSelect = document.getElementById('select-year');
    const now = new Date();
    return {
        month: monthSelect ? parseInt(monthSelect.value, 10) : now.getMonth() + 1,
        year: yearSelect ? parseInt(yearSelect.value, 10) : now.getFullYear()
    };
}

async function onPeriodChange() {
    await refreshDashboard();
}

async function refreshDashboard() {
    await Promise.all([
        loadSummaryMetrics(),
        loadBudgets(),
        loadTransactions()
    ]);
}

// ==========================================
// CATEGORIES CATALOG
// ==========================================
async function loadCategories() {
    try {
        categoryCatalog = await API.getCategories();
        populateCategoryDropdowns();
    } catch (err) {
        console.error('Error fetching categories catalog:', err);
    }
}

function populateCategoryDropdowns() {
    const txCatSelect = document.getElementById('tx-category');
    const filterCatSelect = document.getElementById('filter-category');
    const budgetCatSelect = document.getElementById('budget-category');
    const currentType = document.getElementById('tx-type-input').value;

    const list = currentType === 'INCOME' ? categoryCatalog.incomeCategories : categoryCatalog.expenseCategories;

    if (txCatSelect) {
        txCatSelect.innerHTML = list.map(c => `<option value="${c}">${c}</option>`).join('');
    }

    if (budgetCatSelect) {
        budgetCatSelect.innerHTML = categoryCatalog.expenseCategories.map(c => `<option value="${c}">${c}</option>`).join('');
    }

    if (filterCatSelect) {
        const allCats = [...categoryCatalog.incomeCategories, ...categoryCatalog.expenseCategories];
        filterCatSelect.innerHTML = '<option value="ALL">All Categories</option>' +
            allCats.map(c => `<option value="${c}">${c}</option>`).join('');
    }
}

// ==========================================
// SUMMARY METRICS & BREAKDOWN
// ==========================================
async function loadSummaryMetrics() {
    try {
        const { month, year } = getSelectedPeriod();
        const summary = await API.getMonthlySummary(currentUser.userId, month, year);

        // Update cards
        const cardIncome = document.getElementById('card-income');
        const cardExpense = document.getElementById('card-expense');
        const cardBalance = document.getElementById('card-balance');
        const cardBalanceSub = document.getElementById('card-balance-sub');
        const cardAlerts = document.getElementById('card-alerts');
        const cardAlertsSub = document.getElementById('card-alerts-sub');

        if (cardIncome) cardIncome.textContent = formatCurrency(summary.totalIncome);
        if (cardExpense) cardExpense.textContent = formatCurrency(summary.totalExpense);

        if (cardBalance) {
            cardBalance.textContent = formatCurrency(summary.netSavings);
            cardBalance.style.color = summary.netSavings >= 0 ? 'var(--income)' : 'var(--expense)';
        }

        if (cardBalanceSub) {
            cardBalanceSub.textContent = `Savings Rate: ${summary.savingsRate}%`;
        }

        if (cardAlerts) {
            const totalAlerts = summary.exceededBudgetsCount + summary.warningBudgetsCount;
            cardAlerts.textContent = totalAlerts > 0 ? `${totalAlerts} Budget Alerts` : '0 Alerts';
            cardAlerts.style.color = summary.exceededBudgetsCount > 0 ? 'var(--expense)' : (summary.warningBudgetsCount > 0 ? 'var(--warning)' : 'var(--income)');
        }

        if (cardAlertsSub) {
            if (summary.exceededBudgetsCount > 0) {
                cardAlertsSub.textContent = `${summary.exceededBudgetsCount} category budgets EXCEEDED!`;
                cardAlertsSub.style.color = 'var(--expense)';
            } else if (summary.warningBudgetsCount > 0) {
                cardAlertsSub.textContent = `${summary.warningBudgetsCount} category near limit (>80%)`;
                cardAlertsSub.style.color = 'var(--warning)';
            } else {
                cardAlertsSub.textContent = 'All categories within budget limits';
                cardAlertsSub.style.color = 'var(--text-muted)';
            }
        }

        // Render Category Expense Breakdown
        renderCategoryBreakdown(summary.categoryExpenses, summary.totalExpense);
    } catch (err) {
        console.error('Error loading summary:', err);
    }
}

function renderCategoryBreakdown(expenses, total) {
    const container = document.getElementById('category-breakdown-container');
    if (!container) return;

    const entries = Object.entries(expenses || {});
    if (entries.length === 0 || total <= 0) {
        container.innerHTML = '<div class="empty-state">No expense activity recorded yet for this month.</div>';
        return;
    }

    entries.sort((a, b) => b[1] - a[1]);

    container.innerHTML = entries.map(([category, amount]) => {
        const pct = Math.round((amount / total) * 100);
        return `
            <div class="breakdown-row">
                <div class="breakdown-labels">
                    <span><strong>${category}</strong></span>
                    <span>${formatCurrency(amount)} (${pct}%)</span>
                </div>
                <div class="progress-track">
                    <div class="progress-bar safe" style="width: ${pct}%; background: #6366f1;"></div>
                </div>
            </div>
        `;
    }).join('');
}

// ==========================================
// BUDGET MANAGEMENT
// ==========================================
async function loadBudgets() {
    try {
        const { month, year } = getSelectedPeriod();
        const budgets = await API.getBudgets(currentUser.userId, month, year);
        const container = document.getElementById('budget-list-container');
        if (!container) return;

        if (!budgets || budgets.length === 0) {
            container.innerHTML = '<div class="empty-state">No monthly budgets configured. Click "+ Set" to establish category spending targets.</div>';
            return;
        }

        container.innerHTML = budgets.map(b => {
            const barClass = b.isExceeded ? 'exceeded' : (b.isWarning ? 'warning' : 'safe');
            const clampedPct = Math.min(100, Math.max(0, b.percentageUsed));
            return `
                <div class="budget-item">
                    <div class="budget-meta">
                        <span class="budget-cat-name">${b.category}</span>
                        <div style="display: flex; align-items: center; gap: 8px;">
                            <span class="budget-status-pill ${b.status}">${b.status}</span>
                            <button type="button" class="btn-tbl-action delete" title="Delete Budget" onclick="deleteBudget(${b.id})">
                                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                    <polyline points="3 6 5 6 21 6"></polyline>
                                    <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                                </svg>
                            </button>
                        </div>
                    </div>
                    <div class="progress-track">
                        <div class="progress-bar ${barClass}" style="width: ${clampedPct}%;"></div>
                    </div>
                    <div class="budget-figures">
                        <span>Spent: ${formatCurrency(b.currentSpent)} of ${formatCurrency(b.monthlyLimit)}</span>
                        <span>${b.percentageUsed}%</span>
                    </div>
                </div>
            `;
        }).join('');
    } catch (err) {
        console.error('Error loading budgets:', err);
    }
}

async function saveBudget(e) {
    e.preventDefault();
    const category = document.getElementById('budget-category').value;
    const limit = parseFloat(document.getElementById('budget-limit').value);
    const { month, year } = getSelectedPeriod();

    try {
        await API.setBudget({
            userId: currentUser.userId,
            category,
            monthlyLimit: limit,
            month,
            year
        });

        closeBudgetModal();
        showToast(`Budget for ${category} successfully updated!`, 'success');
        await Promise.all([loadBudgets(), loadSummaryMetrics()]);
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function deleteBudget(id) {
    if (!confirm('Are you sure you want to delete this monthly budget target?')) return;
    try {
        await API.deleteBudget(id);
        showToast('Budget deleted.', 'info');
        await Promise.all([loadBudgets(), loadSummaryMetrics()]);
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// ==========================================
// TRANSACTIONS MANAGEMENT & FILTERING
// ==========================================
async function loadTransactions() {
    try {
        const type = document.getElementById('filter-type').value;
        const category = document.getElementById('filter-category').value;
        const search = document.getElementById('filter-search').value.trim();
        const { month, year } = getSelectedPeriod();

        // Calculate month start and end dates
        const startDate = `${year}-${String(month).padStart(2, '0')}-01`;
        const lastDay = new Date(year, month, 0).getDate();
        const endDate = `${year}-${String(month).padStart(2, '0')}-${String(lastDay).padStart(2, '0')}`;

        const transactions = await API.getTransactions(currentUser.userId, {
            type,
            category,
            startDate,
            endDate,
            search
        });

        const tbody = document.getElementById('tx-tbody');
        const countLabel = document.getElementById('tx-count-label');
        if (countLabel) countLabel.textContent = `Showing ${transactions.length} transactions`;

        if (!tbody) return;

        if (!transactions || transactions.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="6" class="empty-state">
                        No transactions found matching criteria. Click "+ Add Transaction" to create one.
                    </td>
                </tr>
            `;
            return;
        }

        tbody.innerHTML = transactions.map(t => {
            const isIncome = t.type === 'INCOME';
            const badgeClass = isIncome ? 'income' : 'expense';
            const amountClass = isIncome ? 'amount-income' : 'amount-expense';
            const sign = isIncome ? '+' : '-';

            return `
                <tr>
                    <td><strong>${t.transactionDate}</strong></td>
                    <td><span class="badge ${badgeClass}">${t.type}</span></td>
                    <td>${t.category}</td>
                    <td style="color: var(--text-secondary);">${t.description || '-'}</td>
                    <td class="${amountClass}">${sign}${formatCurrency(t.amount)}</td>
                    <td style="text-align: right;">
                        <div class="table-actions" style="justify-content: flex-end;">
                            <button type="button" class="btn-tbl-action" title="Edit" onclick="editTransaction(${JSON.stringify(t).replace(/"/g, '&quot;')})">
                                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                    <path d="M12 20h9"></path>
                                    <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"></path>
                                </svg>
                            </button>
                            <button type="button" class="btn-tbl-action delete" title="Delete" onclick="deleteTransaction(${t.id})">
                                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                    <polyline points="3 6 5 6 21 6"></polyline>
                                    <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                                </svg>
                            </button>
                        </div>
                    </td>
                </tr>
            `;
        }).join('');
    } catch (err) {
        console.error('Error loading transactions:', err);
    }
}

function debounceFilter() {
    clearTimeout(activeFilterTimeout);
    activeFilterTimeout = setTimeout(() => {
        loadTransactions();
    }, 250);
}

function resetFilters() {
    document.getElementById('filter-search').value = '';
    document.getElementById('filter-type').value = 'ALL';
    document.getElementById('filter-category').value = 'ALL';
    loadTransactions();
}

async function saveTransaction(e) {
    e.preventDefault();
    const id = parseInt(document.getElementById('tx-edit-id').value, 10);
    const type = document.getElementById('tx-type-input').value;
    const amount = parseFloat(document.getElementById('tx-amount').value);
    const category = document.getElementById('tx-category').value;
    const transactionDate = document.getElementById('tx-date').value;
    const description = document.getElementById('tx-description').value.trim();

    try {
        if (id > 0) {
            await API.updateTransaction({
                userId: currentUser.userId,
                transactionId: id,
                type,
                amount,
                category,
                transactionDate,
                description
            });
            showToast('Transaction updated successfully!', 'success');
        } else {
            await API.createTransaction({
                userId: currentUser.userId,
                type,
                amount,
                category,
                transactionDate,
                description
            });
            showToast('New transaction recorded!', 'success');
        }

        closeTransactionModal();
        await refreshDashboard();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function deleteTransaction(id) {
    if (!confirm('Are you sure you want to delete this transaction record?')) return;
    try {
        await API.deleteTransaction(currentUser.userId, id);
        showToast('Transaction removed.', 'info');
        await refreshDashboard();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

function editTransaction(tx) {
    document.getElementById('tx-edit-id').value = tx.id;
    document.getElementById('modal-tx-title').textContent = 'Edit Transaction #' + tx.id;
    setTxType(tx.type);
    document.getElementById('tx-amount').value = tx.amount;
    document.getElementById('tx-category').value = tx.category;
    document.getElementById('tx-date').value = tx.transactionDate;
    document.getElementById('tx-description').value = tx.description || '';

    const modal = document.getElementById('modal-transaction');
    if (modal) modal.classList.add('active');
}

// ==========================================
// MODALS & HELPERS
// ==========================================
function openTransactionModal() {
    document.getElementById('tx-edit-id').value = '0';
    document.getElementById('modal-tx-title').textContent = 'Add New Transaction';
    document.getElementById('form-transaction').reset();
    document.getElementById('tx-date').value = new Date().toISOString().split('T')[0];
    setTxType('EXPENSE');

    const modal = document.getElementById('modal-transaction');
    if (modal) modal.classList.add('active');
}

function closeTransactionModal() {
    const modal = document.getElementById('modal-transaction');
    if (modal) modal.classList.remove('active');
}

function openBudgetModal() {
    document.getElementById('form-budget').reset();
    const modal = document.getElementById('modal-budget');
    if (modal) modal.classList.add('active');
}

function closeBudgetModal() {
    const modal = document.getElementById('modal-budget');
    if (modal) modal.classList.remove('active');
}

function setTxType(type) {
    document.getElementById('tx-type-input').value = type;
    const btnExp = document.getElementById('btn-type-expense');
    const btnInc = document.getElementById('btn-type-income');

    if (type === 'INCOME') {
        btnInc.classList.add('active');
        btnExp.classList.remove('active');
    } else {
        btnExp.classList.add('active');
        btnInc.classList.remove('active');
    }
    populateCategoryDropdowns();
}

function switchSection(section) {
    document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));
    const link = document.getElementById('nav-' + section);
    if (link) link.classList.add('active');

    // Scroll smoothly to target section
    const target = document.getElementById('section-' + section);
    if (target) {
        target.scrollIntoView({ behavior: 'smooth' });
    }
}

function formatCurrency(amount) {
    const num = Number(amount) || 0;
    return '₹' + num.toLocaleString('en-IN', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}
