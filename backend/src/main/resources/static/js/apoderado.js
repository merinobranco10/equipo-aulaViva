'use strict';
(async function () {
  const me = await API.init('GUARDIAN');
  if (!me) { return; }
  const h = API.h;
  const box = document.getElementById('students');
  if (!me.students.length) {
    box.append(h('p', { class: 'muted' }, 'No tienes pupilos vinculados.'));
    return;
  }

  const plural = (n, one, many) => n + ' ' + (n === 1 ? one : many);

  // Detalle agrupado por curso: cada curso muestra su promedio y se pliega por separado.
  function renderDetail(panel, items, prefix) {
    panel.replaceChildren();
    if (!items.length) { panel.append(h('p', { class: 'muted' }, 'Aún no hay evaluaciones asignadas.')); return; }

    const byCourse = new Map();
    items.forEach((e) => {
      if (!byCourse.has(e.courseId)) { byCourse.set(e.courseId, { name: e.courseName, items: [] }); }
      byCourse.get(e.courseId).items.push(e);
    });
    const courses = Array.from(byCourse.values()).sort((x, y) => x.name.localeCompare(y.name, 'es'));
    const allGrades = items.filter((e) => e.status === 'COMPLETED' && e.grade !== null).map((e) => Number(e.grade));
    if (allGrades.length) {
      panel.append(h('p', {}, h('strong', {}, 'Mejor nota: '), API.fmtGrade(Math.max(...allGrades)),
        ' · ', h('strong', {}, 'Nota más baja: '), API.fmtGrade(Math.min(...allGrades))));
    }

    const toggles = [];
    const bodies = [];
    function setOpen(k, open) {
      bodies[k].hidden = !open;
      toggles[k].btn.setAttribute('aria-expanded', String(open));
      toggles[k].arrow.textContent = open ? '▾' : '▸';
    }

    const list = h('div', {});
    courses.forEach((c, k) => {
      const done = c.items.filter((e) => e.status === 'COMPLETED' && e.grade !== null);
      const pending = c.items.length - done.length;
      const avg = done.length ? done.reduce((sum, e) => sum + Number(e.grade), 0) / done.length : null;
      const id = prefix + '-c' + k;
      const arrow = h('span', { 'aria-hidden': 'true' }, '▸');
      const btn = h('button', { type: 'button', class: 'course-toggle', 'aria-expanded': 'false', 'aria-controls': id },
        h('span', {}, h('strong', {}, c.name), ' ',
          h('span', { class: 'course-sum' }, '· Promedio ' + (avg === null ? 'sin notas aún' : API.fmtGrade(avg))
            + ' · ' + plural(done.length, 'completada', 'completadas') + ' · ' + plural(pending, 'pendiente', 'pendientes'))),
        arrow);
      const body = h('div', { id, class: 'course-body', hidden: '' });
      c.items.forEach((e) => body.append(h('div', { class: 'row' },
        h('strong', {}, e.title),
        e.status === 'COMPLETED'
          ? h('span', { class: 'badge ok' }, 'Nota ' + API.fmtGrade(e.grade))
          : h('span', { class: 'badge pending' }, 'Pendiente'))));
      toggles.push({ btn, arrow });
      bodies.push(body);
      btn.addEventListener('click', () => setOpen(k, bodies[k].hidden));
      list.append(h('section', { class: 'course' }, btn, body));
    });

    if (courses.length > 1) {
      const all = h('button', { type: 'button', class: 'secondary small' }, 'Desplegar todos');
      let allOpen = false;
      all.addEventListener('click', () => {
        allOpen = !allOpen;
        courses.forEach((_, k) => setOpen(k, allOpen));
        all.textContent = allOpen ? 'Plegar todos' : 'Desplegar todos';
      });
      panel.append(all);
    }
    panel.append(list);
    if (courses.length === 1) { setOpen(0, true); } // con un solo curso se muestra abierto
  }

  for (const [idx, s] of me.students.entries()) {
    const card = h('section', { class: 'card' }, h('h3', {}, s.fullName));
    box.append(card);
    try {
      const p = await API.call('GET', '/v1/students/' + encodeURIComponent(s.id) + '/progress');
      card.append(h('div', { class: 'stats' },
        h('div', { class: 'stat' }, h('strong', {}, p.completedEvaluations), 'Completadas'),
        h('div', { class: 'stat' }, h('strong', {}, p.pendingEvaluations), 'Pendientes'),
        h('div', { class: 'stat' }, h('strong', {}, API.fmtGrade(p.averageGrade)), p.averageGrade === null ? 'Sin notas aún' : 'Promedio')));
    } catch (err) {
      const msg = h('div', { role: 'alert' });
      card.append(msg);
      API.showError(msg, err);
      continue;
    }
    const panelId = 'detail-' + idx;
    const panel = h('div', { id: panelId, class: 'detail', hidden: '' });
    const msg = h('div', { role: 'alert', hidden: '' });
    const btn = h('button', { type: 'button', class: 'secondary', 'aria-expanded': 'false', 'aria-controls': panelId }, 'Ver detalle');
    let loaded = false;
    btn.addEventListener('click', async () => {
      const opening = panel.hidden;
      panel.hidden = !opening;
      btn.setAttribute('aria-expanded', String(opening));
      btn.textContent = opening ? 'Ocultar detalle' : 'Ver detalle';
      if (opening && !loaded) {
        API.clear(msg);
        try {
          renderDetail(panel, await API.call('GET', '/v1/students/' + encodeURIComponent(s.id) + '/evaluations'), panelId);
          loaded = true;
        } catch (err) { API.showError(msg, err); }
      }
    });
    card.append(btn, msg, panel);
  }
})();
