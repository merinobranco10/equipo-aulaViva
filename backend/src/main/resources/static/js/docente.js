'use strict';
(async function () {
  const me = await API.init('TEACHER');
  if (!me) { return; }
  const h = API.h;
  const $ = (id) => document.getElementById(id);
  const INITIAL_QUESTIONS = 3;
  const MIN_QUESTIONS = 1;
  const MAX_QUESTIONS = 50; // mismo límite que CreateEvaluationRequest
  const MIN_OPTIONS = 2;
  const MAX_OPTIONS = 4;    // la API admite hasta 10; la UI limita a 4
  const DEFAULT_OPTIONS = 4;
  let uid = 0; // nombres únicos para los grupos de radio

  API.tabs('crear');
  me.courses.forEach((c) => {
    $('course').append(h('option', { value: c.id }, c.name));
    $('f-course').append(h('option', { value: c.id }, c.name));
  });

  const blocks = [];

  // ---------- alternativas ----------
  function renumberOptions(b) {
    b.options.forEach((o, k) => {
      o.input.setAttribute('aria-label', 'Alternativa ' + (k + 1));
      o.radio.setAttribute('aria-label', 'Marcar alternativa ' + (k + 1) + ' como correcta');
      o.rm.setAttribute('aria-label', 'Quitar alternativa ' + (k + 1));
      o.rm.disabled = b.options.length <= MIN_OPTIONS;
    });
    b.addOptBtn.disabled = b.options.length >= MAX_OPTIONS;
    b.optCount.textContent = b.options.length + ' alternativas (entre ' + MIN_OPTIONS + ' y ' + MAX_OPTIONS + ')';
  }

  function addOption(b) {
    if (b.options.length >= MAX_OPTIONS) { return null; }
    const radio = h('input', { type: 'radio', name: 'correct-' + b.n });
    const input = h('input', { maxlength: '500' });
    const rm = h('button', { type: 'button', class: 'secondary small' }, 'Quitar');
    const row = h('div', { class: 'opt-row' }, radio, input, rm);
    const opt = { row, radio, input, rm };
    rm.addEventListener('click', () => removeOption(b, opt));
    b.optsBox.append(row);
    b.options.push(opt);
    renumberOptions(b);
    return opt;
  }

  function removeOption(b, opt) {
    if (b.options.length <= MIN_OPTIONS) { return; }
    const k = b.options.indexOf(opt);
    b.options.splice(k, 1);
    opt.row.remove();
    renumberOptions(b);
    b.options[Math.min(k, b.options.length - 1)].input.focus();
  }

  // ---------- preguntas ----------
  function renumber() {
    blocks.forEach((b, i) => {
      b.legend.textContent = 'Pregunta ' + (i + 1);
      b.text.setAttribute('aria-label', 'Enunciado pregunta ' + (i + 1));
      b.removeBtn.disabled = blocks.length <= MIN_QUESTIONS;
      b.removeBtn.setAttribute('aria-label', 'Quitar pregunta ' + (i + 1));
    });
    $('add-q').disabled = blocks.length >= MAX_QUESTIONS;
    $('q-count').textContent = blocks.length + (blocks.length === 1 ? ' pregunta' : ' preguntas')
      + ' (máximo ' + MAX_QUESTIONS + ')';
  }

  function isFilled(b) {
    return b.text.value.trim() !== '' || b.options.some((o) => o.input.value.trim() !== '');
  }

  function addQuestion() {
    if (blocks.length >= MAX_QUESTIONS) { return null; }
    const b = {
      n: uid++,
      options: [],
      legend: h('legend', {}, ''),
      text: h('input', { maxlength: '1000' }),
      points: h('input', { type: 'number', min: '1', max: '100', value: '2', 'aria-label': 'Puntos' }),
      expl: h('input', { maxlength: '1000', 'aria-label': 'Explicación (opcional)' }),
      optsBox: h('div', {}),
      optCount: h('span', { class: 'muted' }),
      addOptBtn: h('button', { type: 'button', class: 'secondary small' }, '+ Agregar alternativa'),
      removeBtn: h('button', { type: 'button', class: 'secondary' }, 'Quitar pregunta')
    };
    b.el = h('fieldset', {},
      b.legend,
      h('label', {}, 'Enunciado'), b.text,
      h('label', {}, 'Alternativas (marca la correcta)'), b.optsBox,
      h('div', { class: 'actions' }, b.addOptBtn, b.optCount),
      h('div', { class: 'grid2' },
        h('div', {}, h('label', {}, 'Puntos'), b.points),
        h('div', {}, h('label', {}, 'Explicación (opcional)'), b.expl)),
      b.removeBtn);
    b.addOptBtn.addEventListener('click', () => { const o = addOption(b); if (o) { o.input.focus(); } });
    b.removeBtn.addEventListener('click', () => removeQuestion(b));
    for (let k = 0; k < DEFAULT_OPTIONS; k++) { addOption(b); }
    $('questions').append(b.el);
    blocks.push(b);
    renumber();
    return b;
  }

  function removeQuestion(block) {
    if (blocks.length <= MIN_QUESTIONS) { return; }
    const i = blocks.indexOf(block);
    if (isFilled(block) && !window.confirm('¿Quitar la pregunta ' + (i + 1) + '? Se perderá lo que escribiste.')) { return; }
    blocks.splice(i, 1);
    block.el.remove();
    renumber();
    blocks[Math.min(i, blocks.length - 1)].text.focus();
  }

  $('add-q').addEventListener('click', () => {
    const b = addQuestion();
    if (b) { b.text.focus(); }
  });

  function resetForm() {
    $('title').value = '';
    blocks.splice(0).forEach((b) => b.el.remove());
    for (let i = 0; i < INITIAL_QUESTIONS; i++) { addQuestion(); }
  }

  function collect() {
    const questions = blocks.map((b, i) => {
      const options = [];
      let correct = -1;
      b.options.forEach((o) => {
        const v = o.input.value.trim();
        if (!v) { return; }
        if (o.radio.checked) { correct = options.length; }
        options.push(v);
      });
      if (!b.text.value.trim()) { throw { title: 'Falta el enunciado', detail: 'Pregunta ' + (i + 1) }; }
      if (options.length < MIN_OPTIONS) { throw { title: 'Faltan alternativas', detail: 'Pregunta ' + (i + 1) + ' necesita al menos ' + MIN_OPTIONS + ' con texto.' }; }
      if (correct < 0) { throw { title: 'Falta la alternativa correcta', detail: 'Pregunta ' + (i + 1) + ': marca una alternativa que tenga texto.' }; }
      const q = { text: b.text.value.trim(), options, correctOption: correct, points: Number(b.points.value) || 1 };
      if (b.expl.value.trim()) { q.explanation = b.expl.value.trim(); }
      return q;
    });
    if (!questions.length) { throw { title: 'Agrega al menos una pregunta' }; }
    if (!$('title').value.trim()) { throw { title: 'Falta el título' }; }
    return { courseId: $('course').value, title: $('title').value.trim(), questions };
  }

  // ---------- lista con filtros ----------
  let allItems = [];

  function matches(e) {
    const text = $('f-text').value.trim().toLowerCase();
    const course = $('f-course').value;
    return (!text || e.title.toLowerCase().includes(text) || e.id.toLowerCase().includes(text))
      && (!course || e.courseId === course);
  }

  function renderList() {
    const box = $('list');
    box.replaceChildren();
    const shown = allItems.filter(matches);
    $('list-count').textContent = allItems.length ? 'Mostrando ' + shown.length + ' de ' + allItems.length : '';
    if (!allItems.length) { box.append(h('p', { class: 'muted' }, 'Aún no hay evaluaciones.')); return; }
    if (!shown.length) { box.append(h('p', { class: 'muted' }, 'Ninguna evaluación coincide con los filtros.')); return; }
    shown.forEach((e) => box.append(h('div', { class: 'row' },
      h('div', {}, h('strong', {}, e.title), h('div', { class: 'muted' }, e.courseName)),
      h('code', {}, e.id))));
  }

  async function loadList() {
    API.clear($('list-msg'));
    try {
      allItems = await API.call('GET', '/v1/me/evaluations');
      renderList();
    } catch (err) { $('list').replaceChildren(); API.showError($('list-msg'), err); }
  }

  ['f-text', 'f-course'].forEach((id) => $(id).addEventListener('input', renderList));
  $('f-clear').addEventListener('click', () => {
    $('f-text').value = '';
    $('f-course').value = '';
    renderList();
  });

  // ---------- crear y consultar ----------
  $('create').addEventListener('click', async () => {
    API.clear($('create-msg'));
    const button = $('create');
    try {
      const body = collect();
      button.disabled = true; // evita doble envío
      const created = await API.call('POST', '/v1/evaluations', body, { 'Idempotency-Key': API.uuid() });
      API.showOk($('create-msg'), 'Evaluación creada. ID: ' + created.id + ' (estado ' + created.status + '). Revísala en "Mis evaluaciones".');
      $('lookup-id').value = created.id;
      resetForm();
      loadList();
    } catch (err) { API.showError($('create-msg'), err); }
    button.disabled = false;
  });

  $('lookup').addEventListener('click', async () => {
    const out = $('lookup-out');
    out.replaceChildren();
    API.clear($('lookup-msg'));
    const id = $('lookup-id').value.trim();
    if (!id) { API.showError($('lookup-msg'), { title: 'Indica un ID' }); return; }
    try {
      const ev = await API.call('GET', '/v1/evaluations/' + encodeURIComponent(id));
      out.append(h('h3', {}, ev.title));
      ev.questions.forEach((q, i) => out.append(h('p', {}, (i + 1) + '. ' + q.text + ' — ' + q.options.join(' | '))));
    } catch (err) { API.showError($('lookup-msg'), err); }
  });

  resetForm();
  loadList();
})();
