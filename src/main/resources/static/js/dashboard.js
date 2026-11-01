(() => {
  const page = document.body;
  const menuButton = document.querySelector('.mobile-menu-toggle');
  const sidebar = document.querySelector('.sidebar');
  const themeButton = document.querySelector('.theme-toggle');

  if (menuButton && sidebar) {
    menuButton.addEventListener('click', () => {
      const open = page.classList.toggle('sidebar-open');
      menuButton.setAttribute('aria-expanded', String(open));
    });
    document.addEventListener('click', event => {
      if (!page.classList.contains('sidebar-open')) return;
      if (sidebar.contains(event.target) || menuButton.contains(event.target)) return;
      page.classList.remove('sidebar-open');
      menuButton.setAttribute('aria-expanded', 'false');
    });
  }

  if (themeButton) {
    const saved = localStorage.getItem('lifelink-theme') === 'dark';
    page.classList.toggle('dark-mode', saved);
    themeButton.setAttribute('aria-pressed', String(saved));
    themeButton.addEventListener('click', () => {
      const dark = page.classList.toggle('dark-mode');
      themeButton.setAttribute('aria-pressed', String(dark));
      localStorage.setItem('lifelink-theme', dark ? 'dark' : 'light');
    });
  }
})();
