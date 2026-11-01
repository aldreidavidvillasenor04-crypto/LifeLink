document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('[data-password-toggle]').forEach(button => {
    const input = document.getElementById(button.dataset.passwordToggle);
    if (!(input instanceof HTMLInputElement)) return;

    button.addEventListener('click', () => {
      const showPassword = input.type === 'password';
      input.type = showPassword ? 'text' : 'password';
      button.setAttribute('aria-pressed', String(showPassword));
      button.setAttribute('aria-label', `${showPassword ? 'Hide' : 'Show'} ${input.name === 'confirmPassword' ? 'confirm password' : 'password'}`);
      button.textContent = showPassword ? 'Hide' : 'Show';
    });
  });
});
