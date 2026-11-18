'use strict';
(async function () {
  const me = await API.init('STUDENT');
  if (!me) { return; }
  const h = API.h;
  const $ = (id) => document.getElementById(id);
  let currentEval = null;
  let currentKey = null;
  let sent = false;

  // Tras recargar no hay examen ni revisión cargados: volver a la lista.
  if (window.location.hash === '#rendir' || window.location.hash === '#revision') {
    window.location.hash = '#evaluaciones';
  }
  API.tabs('evaluaciones');

  function lockAnswers(locked) {
    document.querySelectorAll('#exam input[type=radio]').forEach((r) => { r.disabled = locked; });
  }

  let allItems = [];

  function matches(e) {
    const text = $('f-text').value.trim().toLowerCase();
    const status = $('f-status').value;
    const course = $('f-course').value;
    return (!text || e.title.toLowerCase().includes(text))
      && (!status || e.status === status)
      && (!course || e.courseId === course);
  }

  function renderList() {
    const box = $('list');
    box.replaceChildren();
    const shown = allItems.filter(matches);
    $('list-count').textContent = allItems.length ? 'Mostrando ' + shown.length + ' de ' + allItems.length : '';
    if (!allItems.length) { box.append(h('p', { class: 'muted' }, 'No tienes evaluaciones.')); return; }
    if (!shown.length) { box.append(h('p', { class: 'muted' }, 'Ninguna evaluación coincide con los filtros.')); return; }
    shown.forEach((e) => {
      const right = e.status === 'PENDING'
        ? h('button', { onclick: () => openEvaluation(e.id) }, 'Rendir')
        : h('div', { class: 'actions' },
          h('span', { class: 'badge ok' }, 'Nota ' + API.fmtGrade(e.grade)),
          h('button', { class: 'secondary', onclick: () => openReview(e.id) }, 'Revisar respuestas'));
      box.append(h('div', { class: 'row' },
        h('div', {}, h('strong', {}, e.title),
          h('div', { class: 'muted' }, e.courseName + ' · ' + (e.status === 'PENDING' ? 'Pendiente' : 'Completada'))),
        right));
    });
  }

  async function loadList() {
    API.clear($('list-msg'));
    try {
      allItems = await API.call('GET', '/v1/me/evaluations');
      renderList();
    } catch (err) { $('list').replaceChildren(); API.showError($('list-msg'), err); }
  }

  me.courses.forEach((c) => $('f-course').append(h('option', { value: c.id }, c.name)));
  $('f-course-box').hidden = me.courses.length < 2; // con un solo curso el filtro no aporta
  ['f-text', 'f-status', 'f-course'].forEach((id) => $(id).addEventListener('input', renderList));

  async function openEvaluation(id) {
    API.clear($('eval-msg'));
    $('result').replaceChildren();
    sent = false;
    try {
      const ev = await API.call('GET', '/v1/evaluations/' + encodeURIComponent(id));
      currentEval = ev;
      currentKey = API.uuid(); // misma clave si reintenta el envío: la API es idempotente
      const panel = $('exam');
      panel.replaceChildren(h('h2', {}, ev.title));
      ev.questions.forEach((q, i) => {
        const opts = q.options.map((o, j) => h('label', { class: 'opt' },
          h('input', { type: 'radio', name: 'q-' + i, value: String(j) }), ' ' + o));
        panel.append(h('fieldset', {}, h('legend', {}, (i + 1) + '. ' + q.text + ' (' + q.points + ' pts)'), opts));
      });
      panel.append(h('button', { id: 'send', type: 'button', onclick: askConfirmation }, 'Enviar respuestas'));
      window.location.hash = '#rendir';
    } catch (err) { window.location.hash = '#rendir'; API.showError($('eval-msg'), err); }
  }

  // Pantalla de confirmación: avisa de preguntas sin marcar y de que el envío es definitivo.
  function askConfirmation() {
    if (!currentEval || sent) { return; }
    API.clear($('eval-msg'));
    const missing = [];
    currentEval.questions.forEach((q, i) => {
      if (!document.querySelector('input[name="q-' + i + '"]:checked')) { missing.push(i + 1); }
    });
    if (missing.length === currentEval.questions.length) {
      API.showError($('eval-msg'), { title: 'Responde al menos una pregunta antes de enviar' });
      return;
    }
    const body = $('confirm-body');
    body.replaceChildren();
    if (missing.length) {
      body.append(h('p', { class: 'alert warn', role: 'alert' },
        'Tienes ' + missing.length + (missing.length === 1 ? ' pregunta sin responder' : ' preguntas sin responder')
        + ' (' + missing.join(', ') + '). Se contarán como incorrectas.'));
    } else {
      body.append(h('p', {}, 'Respondiste todas las preguntas.'));
    }
    body.append(h('p', {}, 'Una vez enviadas, no podrás cambiar tus respuestas. ¿Confirmas el envío?'));
    $('confirm-ok').disabled = false;
    $('confirm').showModal();
  }

  $('confirm-cancel').addEventListener('click', () => $('confirm').close());
  $('confirm-ok').addEventListener('click', () => {
    $('confirm-ok').disabled = true; // evita doble clic
    $('confirm').close();
    submit();
  });

  async function submit() {
    const ev = currentEval;
    const answers = [];
    ev.questions.forEach((q, i) => {
      const checked = document.querySelector('input[name="q-' + i + '"]:checked');
      if (checked) { answers.push({ questionId: q.id, selectedOption: Number(checked.value) }); }
    });
    const send = $('send');
    lockAnswers(true); // las alternativas ya no se pueden cambiar
    send.disabled = true;
    try {
      const r = await API.call('POST', '/v1/evaluations/' + encodeURIComponent(ev.id) + '/submissions',
        { answers }, { 'Idempotency-Key': currentKey });
      sent = true;
      const res = $('result');
      res.replaceChildren(
        h('h3', {}, 'Nota ' + API.fmtGrade(r.grade)),
        h('p', { class: 'muted' }, 'Puntaje ' + r.score + ' / ' + r.maxScore),
        h('button', { type: 'button', onclick: () => openReview(ev.id) }, 'Ver mis respuestas marcadas'));
      send.hidden = true;
      loadList();
    } catch (err) {
      // El envío falló: se desbloquea para poder reintentar (misma Idempotency-Key).
      lockAnswers(false);
      send.disabled = false;
      API.showError($('eval-msg'), err);
    }
  }

  async function openReview(id) {
    API.clear($('review-msg'));
    const out = $('review-out');
    out.replaceChildren();
    try {
      const r = await API.call('GET', '/v1/evaluations/' + encodeURIComponent(id) + '/submissions/me');
      out.append(h('h2', {}, r.title),
        h('p', { class: 'muted' }, 'Nota ' + API.fmtGrade(r.grade) + ' · Puntaje ' + r.score + ' / ' + r.maxScore));
      r.questions.forEach((q, i) => {
        const items = q.options.map((o, j) => {
          const isSel = q.selectedOption === j;
          const isRight = q.correctOption === j;
          let cls = '';
          let tag = '';
          if (isSel && isRight) { cls = 'good'; tag = ' ✔ tu respuesta (correcta)'; }
          else if (isSel) { cls = 'bad'; tag = ' ✘ tu respuesta'; }
          else if (isRight) { cls = 'good'; tag = ' ✔ respuesta correcta'; }
          return h('li', { class: cls }, o + tag);
        });
        const fieldset = h('fieldset', {}, h('legend', {}, (i + 1) + '. ' + q.text + ' (' + q.points + ' pts)'),
          h('ul', { class: 'review-opts' }, items));
        if (q.selectedOption === null || q.selectedOption === undefined) {
          fieldset.append(h('p', { class: 'muted' }, 'No respondiste esta pregunta.'));
        }
        fieldset.append(h('p', { class: q.correct ? 'fb-ok' : 'fb-bad' }, q.message));
        out.append(fieldset);
      });
      window.location.hash = '#revision';
    } catch (err) { window.location.hash = '#revision'; API.showError($('review-msg'), err); }
  }

  me.courses.forEach((c) => $('t-course').append(h('option', { value: c.id }, c.name)));
  $('ask').addEventListener('click', async () => {
    const out = $('tutor-out');
    out.replaceChildren();
    API.clear($('tutor-msg'));
    const question = $('t-question').value.trim();
    if (!question) { API.showError($('tutor-msg'), { title: 'Escribe una pregunta' }); return; }
    try {
      const r = await API.call('POST', '/v1/tutor/questions',
        { courseId: $('t-course').value, question }, { 'Idempotency-Key': API.uuid() });
      out.append(h('p', {}, r.answer));
      if (r.sources && r.sources.length) { out.append(h('p', { class: 'muted' }, 'Fuentes: ' + r.sources.join(' · '))); }
      if (r.mode) { out.append(h('p', { class: 'muted' }, 'Modo: ' + r.mode)); }
    } catch (err) { API.showError($('tutor-msg'), err); } // hoy: 501 hasta el Día 4
  });

  loadList();
})();
