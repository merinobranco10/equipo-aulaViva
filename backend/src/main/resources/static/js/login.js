'use strict';
(function () {
  const msg = document.getElementById('msg');
  document.getElementById('login-form').addEventListener('submit', async (ev) => {
    ev.preventDefault();
    API.clear(msg);
    API.login(document.getElementById('user').value.trim(), document.getElementById('pass').value);
    try {
      const me = await API.call('GET', '/v1/me');
      const home = API.homeByRole[me.role];
      if (!home) {
        sessionStorage.removeItem('auth');
        API.showError(msg, { title: 'Sin vista en la UI del MVP', detail: 'El rol ' + me.role + ' no tiene pantalla en esta demo.' });
        return;
      }
      window.location.href = home;
    } catch (err) {
      sessionStorage.removeItem('auth');
      if (err && err.status === 401) {
        API.showError(msg, { title: 'Usuario o contraseña incorrectos' });
      } else {
        API.showError(msg, err);
      }
    }
  });
})();
