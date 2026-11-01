document.addEventListener('DOMContentLoaded', () => {

    const body = document.body;
    const toggle = document.getElementById('themeToggle');
    const label = document.getElementById('themeLabel');

    // Default dark mode
    body.classList.add('dark');

    toggle.addEventListener('change', () => {
        if (toggle.checked) {
            body.classList.remove('light');
            body.classList.add('dark');
            label.textContent = 'Dark Mode';
        } else {
            body.classList.remove('dark');
            body.classList.add('light');
            label.textContent = 'Light Mode';
        }
    });

    // Show / hide password
    const passInput = document.getElementById('password');
    const togglePass = document.getElementById('togglePass');

    togglePass.addEventListener('click', () => {
        if (passInput.type === 'password') {
            passInput.type = 'text';
            togglePass.textContent = '🙈';
        } else {
            passInput.type = 'password';
            togglePass.textContent = '👁';
        }
    });

});