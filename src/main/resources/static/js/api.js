// ── API Client ──
const BASE = 'http://localhost:8080';

const api = {
  // ── Auth ──
  token: () => localStorage.getItem('et_token'),
  role: () => localStorage.getItem('et_role'),
  username: () => localStorage.getItem('et_username'),
  fullName: () => localStorage.getItem('et_fullname'),

  headers() {
    const h = { 'Content-Type': 'application/json' };
    const t = this.token();
    if (t) h['Authorization'] = `Bearer ${t}`;
    return h;
  },

  async request(method, path, body) {
    const opts = { method, headers: this.headers() };
    if (body !== undefined) opts.body = JSON.stringify(body);
    const res = await fetch(BASE + path, opts);
    if (res.status === 401) {
      this.logout();
      return;
    }
    if (!res.ok) {
      let msg = `Error ${res.status}`;
      try { const e = await res.json(); msg = e.message || e.error || msg; } catch {}
      throw new Error(msg);
    }
    const ct = res.headers.get('content-type') || '';
    if (ct.includes('application/json')) return res.json();
    return res.text();
  },

  get: (path) => api.request('GET', path),
  post: (path, body) => api.request('POST', path, body),
  put: (path, body) => api.request('PUT', path, body),
  patch: (path) => api.request('PATCH', path),
  delete: (path) => api.request('DELETE', path),

  // ── Auth ──
  async login(username, password) {
    const data = await this.post('/auth/login', { username, password });
    localStorage.setItem('et_token', data.token);
    localStorage.setItem('et_role', data.role);
    return data;
  },

  logout() {
    ['et_token','et_role','et_username','et_fullname'].forEach(k => localStorage.removeItem(k));
    window.location.href = '/login.html';
  },

  isLoggedIn: () => !!localStorage.getItem('et_token'),
  isAdmin: () => localStorage.getItem('et_role') === 'ROLE_ADMIN',

  // ── Employees ──
  employees: {
    getAll: () => api.get('/employees'),
    getById: (id) => api.get(`/employees/${id}`),
    searchFilter: (params) => {
      const q = new URLSearchParams(params).toString();
      return api.get(`/employees/search-filter?${q}`);
    },
    create: (data) => api.post('/employees', data),
    update: (id, data) => api.put(`/employees/${id}`, data),
    delete: (id) => api.delete(`/employees/${id}`),
    activate: (id) => api.patch(`/employees/${id}/activate`),
    avgSalary: (params) => {
      const q = new URLSearchParams(params).toString();
      return api.get(`/employees/stats/average-salary?${q}`);
    },
    avgAge: (params) => {
      const q = new URLSearchParams(params).toString();
      return api.get(`/employees/stats/average-age?${q}`);
    },
  },

  // ── Departments ──
  departments: {
    getAll: () => api.get('/departments'),
    getById: (id) => api.get(`/departments/${id}`),
    create: (data) => api.post('/departments', data),
    update: (id, data) => api.put(`/departments/${id}`, data),
    delete: (id) => api.delete(`/departments/${id}`),
    activate: (id) => api.patch(`/departments/${id}/activate`),
  },

  // ── Users (admin only) ──
  users: {
    getAll: () => api.get('/users'),
    getById: (id) => api.get(`/users/${id}`),
    searchFilter: (params) => {
      const q = new URLSearchParams(params).toString();
      return api.get(`/users/search-filter?${q}`);
    },
    create: (data) => api.post('/users', data),
    update: (id, data) => api.put(`/users/${id}`, data),
    delete: (id) => api.delete(`/users/${id}`),
    activate: (id) => api.patch(`/users/${id}/activate`),
  },

  // ── Dev ──
  seed: () => api.post('/dev/seed'),
};
