// ── UI Utilities ──

// ── Toast Notifications ──
function showToast(msg, type = 'info') {
  let container = document.getElementById('toast-container');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toast-container';
    container.className = 'toast-container';
    document.body.appendChild(container);
  }
  const icons = { success: '✓', error: '✕', info: 'ℹ' };
  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.innerHTML = `<span>${icons[type] || icons.info}</span><span>${msg}</span>`;
  container.appendChild(toast);
  setTimeout(() => { toast.style.opacity = '0'; toast.style.transform = 'translateX(20px)'; toast.style.transition = '0.3s'; setTimeout(() => toast.remove(), 300); }, 3500);
}

// ── Modal ──
function openModal(id) {
  document.getElementById(id).classList.add('open');
}
function closeModal(id) {
  document.getElementById(id).classList.remove('open');
}

// ── Format currency ──
function formatCurrency(val) {
  if (val === null || val === undefined) return '—';
  return new Intl.NumberFormat('en-PH',{ style:'currency', currency:'PHP', minimumFractionDigits:2, maximumFractionDigits:2 }).format(val).replace(/\u00A0/g,'');
}

// ── Format date ──
function formatDate(val) {
  if (!val) return '—';
  return new Date(val).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
}

// ── Badge HTML ──
function activeBadge(active) {
  return active
    ? '<span class="badge badge-active">Active</span>'
    : '<span class="badge badge-inactive">Inactive</span>';
}

function roleBadge(role) {
  const r = (role || '').replace('ROLE_', '');
  return r === 'ADMIN'
    ? '<span class="badge badge-admin">Admin</span>'
    : '<span class="badge badge-user">User</span>';
}

// ── Confirm Delete Modal ──
function showConfirm(title, name, onConfirm) {
  document.getElementById('confirm-title').textContent = title;
  document.getElementById('confirm-name-span').textContent = name;
  const btn = document.getElementById('confirm-ok-btn');
  btn.onclick = () => { closeModal('confirm-modal'); onConfirm(); };
  openModal('confirm-modal');
}

// ── Page guard ──
function requireAuth() {
  if (!api.isLoggedIn()) {
    window.location.href = '/login.html';
    return false;
  }
  return true;
}

// ── Build sidebar user info ──
function buildSidebarUser() {
  const uname = api.username() || 'User';
  const role = api.role() || '';
  const initials = uname.slice(0, 2).toUpperCase();
  const el = document.getElementById('sidebar-user-info');
  if (!el) return;
  el.innerHTML = `
    <div class="user-avatar">${initials}</div>
    <div class="user-info">
      <div class="user-name">${uname}</div>
      <div class="user-role">${role.replace('ROLE_', '')}</div>
    </div>
    <button class="btn-logout" onclick="api.logout()" title="Logout">⏻</button>
  `;
}

// ── Highlight active nav ──
function setActiveNav(page) {
  document.querySelectorAll('.nav-item').forEach(el => {
    el.classList.toggle('active', el.dataset.page === page);
  });
}

// ── Navigate ──
function navigate(page) {
  const pages = {
    dashboard: 'index.html',
    employees: 'employees.html',
    departments: 'departments.html',
    users: 'users.html',
    reports: 'reports.html',
  };
  if (pages[page]) window.location.href = pages[page];
}

// ── Pagination helper ──
function buildPagination(container, current, total, onPage) {
  if (total <= 1) { container.innerHTML = ''; return; }
  let html = '';
  html += `<button class="page-btn" onclick="(${onPage})(${current - 1})" ${current === 0 ? 'disabled' : ''}>‹</button>`;
  const start = Math.max(0, current - 2);
  const end = Math.min(total - 1, current + 2);
  if (start > 0) html += `<button class="page-btn" onclick="(${onPage})(0)">1</button>${start > 1 ? '<span style="color:var(--text-muted);padding:0 4px">…</span>' : ''}`;
  for (let i = start; i <= end; i++) {
    html += `<button class="page-btn ${i === current ? 'active' : ''}" onclick="(${onPage})(${i})">${i + 1}</button>`;
  }
  if (end < total - 1) html += `${end < total - 2 ? '<span style="color:var(--text-muted);padding:0 4px">…</span>' : ''}<button class="page-btn" onclick="(${onPage})(${total - 1})">${total}</button>`;
  html += `<button class="page-btn" onclick="(${onPage})(${current + 1})" ${current === total - 1 ? 'disabled' : ''}>›</button>`;
  container.innerHTML = html;
}

// ── Shared confirm modal markup (injected once) ──
function injectConfirmModal() {
  if (document.getElementById('confirm-modal')) return;
  document.body.insertAdjacentHTML('beforeend', `
    <div class="modal-backdrop" id="confirm-modal">
      <div class="modal" style="max-width:380px">
        <div class="modal-header">
          <span class="modal-title" id="confirm-title">Confirm</span>
          <button class="modal-close" onclick="closeModal('confirm-modal')">✕</button>
        </div>
        <div class="modal-body">
          <p class="confirm-text">Are you sure you want to proceed with <span class="confirm-name" id="confirm-name-span"></span>?</p>
        </div>
        <div class="modal-footer">
          <button class="btn btn-ghost" onclick="closeModal('confirm-modal')">Cancel</button>
          <button class="btn btn-danger" id="confirm-ok-btn">Confirm</button>
        </div>
      </div>
    </div>
  `);
}
