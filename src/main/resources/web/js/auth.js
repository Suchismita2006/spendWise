/**
 * SpendWise Authentication Controller
 */
document.addEventListener('DOMContentLoaded', () => {
    // If on auth page and already logged in, redirect to dashboard
    const currentUser = localStorage.getItem('spendwise_user');
    if (currentUser && window.location.pathname.endsWith('index.html')) {
        window.location.href = 'dashboard.html';
        return;
    }

    const tabLogin = document.getElementById('tab-login');
    const tabRegister = document.getElementById('tab-register');
    const formLogin = document.getElementById('form-login');
    const formRegister = document.getElementById('form-register');

    if (tabLogin && tabRegister) {
        tabLogin.addEventListener('click', () => {
            tabLogin.classList.add('active');
            tabRegister.classList.remove('active');
            formLogin.style.display = 'block';
            formRegister.style.display = 'none';
        });

        tabRegister.addEventListener('click', () => {
            tabRegister.classList.add('active');
            tabLogin.classList.remove('active');
            formRegister.style.display = 'block';
            formLogin.style.display = 'none';
        });
    }

    // Login Form Submit
    if (formLogin) {
        formLogin.addEventListener('submit', async (e) => {
            e.preventDefault();
            const email = document.getElementById('login-email').value.trim();
            const password = document.getElementById('login-password').value;

            try {
                showToast('Authenticating with Core Java backend...', 'info');
                const user = await API.login(email, password);
                localStorage.setItem('spendwise_user', JSON.stringify(user));
                showToast('Login successful! Redirecting...', 'success');
                setTimeout(() => {
                    window.location.href = 'dashboard.html';
                }, 600);
            } catch (err) {
                showToast(err.message, 'error');
            }
        });
    }

    // Register Form Submit
    if (formRegister) {
        formRegister.addEventListener('submit', async (e) => {
            e.preventDefault();
            const fullName = document.getElementById('reg-name').value.trim();
            const email = document.getElementById('reg-email').value.trim();
            const password = document.getElementById('reg-password').value;

            try {
                showToast('Registering user in MySQL...', 'info');
                const user = await API.register(fullName, email, password);
                localStorage.setItem('spendwise_user', JSON.stringify(user));
                showToast('Account created! Welcome to SpendWise.', 'success');
                setTimeout(() => {
                    window.location.href = 'dashboard.html';
                }, 600);
            } catch (err) {
                showToast(err.message, 'error');
            }
        });
    }

    // Demo Fill Button
    const demoBtn = document.getElementById('btn-fill-demo');
    if (demoBtn) {
        demoBtn.addEventListener('click', () => {
            if (tabLogin) tabLogin.click();
            document.getElementById('login-email').value = 'demo@spendwise.com';
            document.getElementById('login-password').value = 'password123';
            showToast('Demo credentials inserted!', 'info');
        });
    }
});

function showToast(message, type = 'info') {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        container.className = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = message;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(10px)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}
