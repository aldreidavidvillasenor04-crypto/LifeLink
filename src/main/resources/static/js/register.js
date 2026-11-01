document.addEventListener('DOMContentLoaded', () => {
  const accountType = document.querySelector('#accountType');
  const wrapper = document.querySelector('#adminCodeWrap');
  const code = document.querySelector('#adminCode');
  const donorFields = document.querySelector('#donorFields');
  const form = document.querySelector('#registerForm');
  const refresh = () => {
    const adminRequested = accountType.value === 'ADMIN';
    wrapper.hidden = !adminRequested;
    code.required = adminRequested;
    donorFields.hidden = adminRequested;
    donorFields.querySelectorAll('input, select').forEach(field => {
      field.disabled = adminRequested;
      field.required = !adminRequested;
    });
  };
  accountType.addEventListener('change', refresh);
  refresh();
  form.addEventListener('submit', event => {
    const password = form.elements.password.value;
    const confirm = form.elements.confirmPassword.value;
    if (password !== confirm) {
      event.preventDefault();
      form.elements.confirmPassword.setCustomValidity('Passwords do not match.');
      form.elements.confirmPassword.reportValidity();
    }
  });
  form.elements.confirmPassword.addEventListener('input', event => event.target.setCustomValidity(''));
});
