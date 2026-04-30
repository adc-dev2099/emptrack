// ── Shell / Layout builder ──
function buildShell({ page, title }) {
  const isAdmin = api.isAdmin();

  const nav = `
    <nav class="sidebar-nav">
      <div class="nav-section">
        <div class="nav-section-label">Overview</div>
        <div class="nav-item ${page === 'dashboard' ? 'active' : ''}" data-page="dashboard" onclick="navigate('dashboard')">
          <span class="nav-icon">📊</span> Dashboard
        </div>
      </div>
      <div class="nav-section">
        <div class="nav-section-label">Management</div>
        <div class="nav-item ${page === 'employees' ? 'active' : ''}" data-page="employees" onclick="navigate('employees')">
          <span class="nav-icon">👥</span> Employees
        </div>
        <div class="nav-item ${page === 'departments' ? 'active' : ''}" data-page="departments" onclick="navigate('departments')">
          <span class="nav-icon">🏢</span> Departments
        </div>
        ${isAdmin ? `
        <div class="nav-item ${page === 'users' ? 'active' : ''}" data-page="users" onclick="navigate('users')">
          <span class="nav-icon">◎</span> Users
        </div>` : ''}
      </div>
      <div class="nav-section">
        <div class="nav-section-label">Analytics</div>
        <div class="nav-item ${page === 'reports' ? 'active' : ''}" data-page="reports" onclick="navigate('reports')">
          <span class="nav-icon">📄</span> Reports
        </div>
      </div>
    </nav>
  `;

  const uname = api.username() || 'User';
  const role = api.role() || '';
  const initials = uname.slice(0, 2).toUpperCase();

  document.body.innerHTML = `
    <div class="app-layout">
      <aside class="sidebar">
        <div class="sidebar-logo">
          <div class="logo-mark">
            <div class="logo-icon">💼</div>
            <div>
              <div class="logo-text">EmpTrack</div>
              <div class="logo-sub">E.M. System</div>
            </div>
          </div>
        </div>
        ${nav}
        <div class="sidebar-user" id="sidebar-user-info">
          <div class="user-avatar">${initials}</div>
          <div class="user-info">
            <div class="user-name">${uname}</div>
            <div class="user-role">${role.replace('ROLE_', '')}</div>
          </div>
          <button class="btn-logout" onclick="api.logout()" title="Logout">⏻</button>
        </div>
      </aside>
      <div class="main-content">
        <div class="topbar">
          <div class="topbar-title">${title}</div>
          <div class="topbar-actions" id="topbar-actions"></div>
        </div>
        <div class="page-body" id="page-body"></div>
      </div>
    </div>
  `;
  injectConfirmModal();
}
