
document.addEventListener('DOMContentLoaded', () => {

    const form = document.querySelector('#registerForm');

    if (!form) return;

    const username = form.elements.username;
    const password = form.elements.password;
    const confirmPassword = form.elements.confirmPassword;

    // Normalize spaces in the username
    username.addEventListener('blur', () => {
        username.value = username.value.trim().replace(/\s+/g, ' ');
    });

    // Check password confirmation
    form.addEventListener('submit', event => {

        if (password.value !== confirmPassword.value) {

            event.preventDefault();

            confirmPassword.setCustomValidity(
                'Passwords do not match.'
            );

            confirmPassword.reportValidity();

        }

    });

    // Clear password error while typing
    confirmPassword.addEventListener('input', () => {
        confirmPassword.setCustomValidity('');
    });

});
