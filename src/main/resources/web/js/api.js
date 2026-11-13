/**
 * SpendWise API Client
 * Wraps HTTP communication with the Java backend REST endpoints.
 */
const API = {
    baseUrl: window.location.origin,

    async request(endpoint, options = {}) {
        const url = `${this.baseUrl}${endpoint}`;
        const defaultHeaders = {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        };

        const config = {
            ...options,
            headers: {
                ...defaultHeaders,
                ...options.headers
            }
        };

        try {
            const res = await fetch(url, config);
            const data = await res.json();
            if (!res.ok || (data && data.success === false)) {
                throw new Error(data.error || 'Server error occurred');
            }
            return data.data !== undefined ? data.data : data;
        } catch (err) {
            console.error(`API Error on [${options.method || 'GET'}] ${endpoint}:`, err);
            throw err;
        }
    },

    // Auth
    async register(fullName, email, password) {
        return this.request('/api/auth/register', {
            method: 'POST',
            body: JSON.stringify({ fullName, email, password })
        });
    },

    async login(email, password) {
        return this.request('/api/auth/login', {
            method: 'POST',
            body: JSON.stringify({ email, password })
        });
    },

    async getProfile(userId) {
        return this.request(`/api/auth/profile?userId=${userId}`);
    },

    // Transactions
    async getTransactions(userId, { type, category, startDate, endDate, search } = {}) {
        const params = new URLSearchParams({ userId });
        if (type && type !== 'ALL') params.append('type', type);
        if (category && category !== 'ALL') params.append('category', category);
        if (startDate) params.append('startDate', startDate);
        if (endDate) params.append('endDate', endDate);
        if (search) params.append('search', search);

        return this.request(`/api/transactions?${params.toString()}`);
    },

    async createTransaction(txData) {
        return this.request('/api/transactions', {
            method: 'POST',
            body: JSON.stringify(txData)
        });
    },

    async updateTransaction(txData) {
        return this.request('/api/transactions', {
            method: 'PUT',
            body: JSON.stringify(txData)
        });
    },

    async deleteTransaction(userId, transactionId) {
        return this.request(`/api/transactions?userId=${userId}&transactionId=${transactionId}`, {
            method: 'DELETE'
        });
    },

    async getAccountBalance(userId) {
        return this.request(`/api/account/balance?userId=${userId}`);
    },

    // Budgets
    async getBudgets(userId, month, year) {
        return this.request(`/api/budgets?userId=${userId}&month=${month}&year=${year}`);
    },

    async setBudget(budgetData) {
        return this.request('/api/budgets', {
            method: 'POST',
            body: JSON.stringify(budgetData)
        });
    },

    async deleteBudget(budgetId) {
        return this.request(`/api/budgets?budgetId=${budgetId}`, {
            method: 'DELETE'
        });
    },

    // Summary & Metadata
    async getMonthlySummary(userId, month, year) {
        return this.request(`/api/summary?userId=${userId}&month=${month}&year=${year}`);
    },

    async getCategories() {
        return this.request('/api/categories');
    }
};
