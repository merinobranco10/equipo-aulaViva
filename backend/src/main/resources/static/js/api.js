'use strict';

// Todo el texto dinámico se inserta con textContent / append(String): nunca HTML crudo (XSS).
const API = {
  homeByRole: { TEACHER: 'docente.html', STUDENT: 'estudiante.html', GUARDIAN: 'apoderado.html' },

  login(user, pass) {
    const bytes = new TextEncoder().encode(user + ':' + pass);
    sessionStorage.setItem('auth', btoa(String.fromCharCode(...bytes)));
  },

  logout() {
    sessionStorage.removeItem('auth');
    window.location.href = 'index.html';
  },

  async call(method, path, body, headers) {
    const res = await fetch(path, {
      method,
      headers: Object.assign({
        'Authorization': 'Basic ' + (sessionStorage.getItem('auth') || ''),
        'Content-Type': 'application/json'
      }, headers || {}),
      body: body ? JSON.stringify(body) : undefined
    });
    if (!res.ok) {
      throw await res.json().catch(() => ({ title: res.statusText, status: res.status }));
    }
    return res.status === 204 ? null : res.json();
  },

  // Carga /v1/me, valida el rol de la página, pinta la cabecera y conecta "Salir".
  async init(expectedRole) {
    let me;
    try {
      me = await API.call('GET', '/v1/me');
    } catch (e) {
      window.location.href = 'index.html';
      return null;
    }
    if (me.role !== expectedRole) {
      window.location.href = API.homeByRole[me.role] || 'index.html';
      return null;
    }
    document.getElementById('who').textContent = me.tenantName + ' · ' + me.fullName;
    document.getElementById('logout').addEventListener('click', API.logout);
    return me;
  },

  // Crea elementos de forma segura: h('div', {class:'x', onclick: fn}, 'texto', otroNodo)
  h(tag, props, ...kids) {
    const el = document.createElement(tag);
    for (const [k, v] of Object.entries(props || {})) {
      if (k === 'class') { el.className = v; }
      else if (k.startsWith('on')) { el.addEventListener(k.slice(2), v); }
      else { el.setAttribute(k, v); }
    }
    for (const kid of kids.flat()) { el.append(kid instanceof Node ? kid : String(kid)); }
    return el;
  },

  showError(box, err) {
    box.hidden = false;
    box.className = 'alert error';
    const status = err && err.status ? err.status + ' · ' : '';
    const title = (err && err.title) || 'No se pudo completar la operación';
    const detail = err && (err.detail || err.message);
    box.textContent = status + title + (detail ? ': ' + detail : '');
  },

  showOk(box, text) {
    box.hidden = false;
    box.className = 'alert ok';
    box.textContent = text;
  },

  clear(box) { box.hidden = true; box.textContent = ''; },

  // Pestañas por hash (#vista). Cada sección lleva data-view="nombre"; los enlaces de #tabs apuntan a #nombre.
  // Las secciones sin enlace (p. ej. "rendir") solo se abren por código.
  tabs(defaultView) {
    const show = () => {
      const wanted = window.location.hash.slice(1);
      const exists = Array.from(document.querySelectorAll('[data-view]')).some((e) => e.dataset.view === wanted);
      const name = exists ? wanted : defaultView;
      document.querySelectorAll('[data-view]').forEach((e) => { e.hidden = e.dataset.view !== name; });
      document.querySelectorAll('#tabs a').forEach((a) => {
        if (a.getAttribute('href') === '#' + name) { a.setAttribute('aria-current', 'page'); }
        else { a.removeAttribute('aria-current'); }
      });
    };
    window.addEventListener('hashchange', show);
    show();
  },

  uuid() {
    return (window.crypto && crypto.randomUUID) ? crypto.randomUUID()
      : 'k-' + Date.now() + '-' + Math.random().toString(16).slice(2);
  },

  fmtGrade(n) { return n === null || n === undefined ? '—' : Number(n).toFixed(1).replace('.', ','); }
};
