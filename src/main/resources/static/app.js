/**
 * EchoStock — Smartwatch Wear OS Web App & Interactive Emulator
 * Réplica fiel de EchoStock-WearApp (Jetpack Compose for Wear OS) para demostración en navegador y PWA en smartwatch.
 */

// ==================== 1. CATÁLOGO SEMILLA INICIAL (V2__datos_iniciales.sql) ====================
const SEED_PRODUCTS = [
  { sku: 'CJA-001', name: 'Cajas tipo A', locationCode: 'A-01-03', unit: 'cajas', systemQuantity: 50 },
  { sku: 'CJA-002', name: 'Cajas tipo B', locationCode: 'A-01-04', unit: 'cajas', systemQuantity: 120 },
  { sku: 'CJA-003', name: 'Cajas de cartón doble canal', locationCode: 'A-02-01', unit: 'cajas', systemQuantity: 75 },
  { sku: 'CJA-004', name: 'Cajas para envío pequeñas', locationCode: 'A-02-02', unit: 'cajas', systemQuantity: 300 },
  { sku: 'CJA-005', name: 'Cajas de archivo', locationCode: 'A-02-05', unit: 'cajas', systemQuantity: 42 },
  { sku: 'FLM-001', name: 'Rollos de film estirable', locationCode: 'B-03-01', unit: 'rollos', systemQuantity: 64 },
  { sku: 'FLM-002', name: 'Rollos de film de burbujas', locationCode: 'B-03-02', unit: 'rollos', systemQuantity: 18 },
  { sku: 'FLM-003', name: 'Film retráctil transparente', locationCode: 'B-03-04', unit: 'rollos', systemQuantity: 25 },
  { sku: 'CNT-001', name: 'Cinta adhesiva marrón', locationCode: 'B-04-01', unit: 'rollos', systemQuantity: 240 },
  { sku: 'CNT-002', name: 'Cinta adhesiva transparente', locationCode: 'B-04-02', unit: 'rollos', systemQuantity: 180 },
  { sku: 'CNT-003', name: 'Cinta de embalar con logotipo', locationCode: 'B-04-03', unit: 'rollos', systemQuantity: 95 },
  { sku: 'PLT-001', name: 'Pallets europeos de madera', locationCode: 'C-01-01', unit: 'pallets', systemQuantity: 36 },
  { sku: 'PLT-002', name: 'Pallets de plástico', locationCode: 'C-01-02', unit: 'pallets', systemQuantity: 14 },
  { sku: 'PLT-003', name: 'Medios pallets de madera', locationCode: 'C-01-03', unit: 'pallets', systemQuantity: 22 },
  { sku: 'TRN-001', name: 'Tornillos hexagonales M8', locationCode: 'D-02-01', unit: 'unidades', systemQuantity: 1500 },
  { sku: 'TRN-002', name: 'Tornillos para madera 4x40', locationCode: 'D-02-02', unit: 'unidades', systemQuantity: 2400 },
  { sku: 'TRC-001', name: 'Tuercas M8 galvanizadas', locationCode: 'D-02-03', unit: 'unidades', systemQuantity: 1800 },
  { sku: 'ARD-001', name: 'Arandelas planas M8', locationCode: 'D-02-04', unit: 'unidades', systemQuantity: 2000 },
  { sku: 'GNT-001', name: 'Guantes de nitrilo talla M', locationCode: 'E-01-01', unit: 'pares', systemQuantity: 400 },
  { sku: 'GNT-002', name: 'Guantes de nitrilo talla L', locationCode: 'E-01-02', unit: 'pares', systemQuantity: 350 },
  { sku: 'GNT-003', name: 'Guantes anticorte', locationCode: 'E-01-03', unit: 'pares', systemQuantity: 60 },
  { sku: 'CSC-001', name: 'Cascos de seguridad blancos', locationCode: 'E-02-01', unit: 'unidades', systemQuantity: 32 },
  { sku: 'CHL-001', name: 'Chalecos reflectantes', locationCode: 'E-02-02', unit: 'unidades', systemQuantity: 48 },
  { sku: 'ETQ-001', name: 'Etiquetas térmicas 100x150', locationCode: 'F-01-01', unit: 'rollos', systemQuantity: 85 },
  { sku: 'ETQ-002', name: 'Etiquetas de frágil', locationCode: 'F-01-02', unit: 'rollos', systemQuantity: 40 },
  { sku: 'BLS-001', name: 'Bolsas de plástico grandes', locationCode: 'F-02-01', unit: 'paquetes', systemQuantity: 130 },
  { sku: 'BRD-001', name: 'Bridas de nailon negras', locationCode: 'F-02-02', unit: 'bolsas', systemQuantity: 70 },
  { sku: 'FLJ-001', name: 'Flejes de polipropileno', locationCode: 'C-02-01', unit: 'rollos', systemQuantity: 16 },
  { sku: 'ESQ-001', name: 'Esquineros de cartón', locationCode: 'C-02-02', unit: 'unidades', systemQuantity: 900 },
  { sku: 'SPR-001', name: 'Separadores de cartón para pallet', locationCode: 'A-01-03', unit: 'unidades', systemQuantity: 210 },
  { sku: 'BAT-001', name: 'Baterías para escáner de mano', locationCode: 'G-01-01', unit: 'unidades', systemQuantity: 12 },
  { sku: 'PIL-001', name: 'Pilas alcalinas AA', locationCode: 'G-01-02', unit: 'paquetes', systemQuantity: 55 }
];

// ==================== 2. FRASES HABLADAS POR EL RELOJ (Phrases.kt) ====================
const Phrases = {
  location(code) {
    const parts = (code || '').split(/[- ]+/).filter(Boolean);
    if (parts.length !== 3) return (code || '').replace(/-/g, ' ');
    const [aisle, shelf, level] = parts;
    const s = shelf.replace(/^0+/, '') || '0';
    const l = level.replace(/^0+/, '') || '0';
    return `pasillo ${aisle}, estante ${s}, nivel ${l}`;
  },
  unit(u, qty) {
    if (qty === 1 && u && u.endsWith('s') && u.length > 2) {
      return u.endsWith('es') ? u.slice(0, -2) : u.slice(0, -1);
    }
    return u;
  },
  systemStatus(p) {
    return `${p.name}, ${this.location(p.locationCode)}. Sistema indica ${p.systemQuantity} ${this.unit(p.unit, p.systemQuantity)}.`;
  },
  outcome(result, difference, outcomeType, reason) {
    if (outcomeType === 'REJECTED') return `No se pudo registrar. ${reason || ''}`;
    if (outcomeType === 'QUEUED') return 'Guardado en el reloj. Se enviará al recuperar señal.';
    if (result === 'MATCH') return 'Conteo confirmado.';
    return `Discrepancia registrada, ${difference > 0 ? 'sobran' : 'faltan'} ${Math.abs(difference)}.`;
  },
  NOT_UNDERSTOOD: 'No entendí. Intenta de nuevo.',
  PROMPT_NEW_PRODUCT: 'Dicta el nombre y la cantidad del producto.',
  notFound(query) { return `No encontré ${query}.`; },
  countSet(n) { return `${n}.`; },
  productAdded(p) { return `${p.name} agregado con ${p.systemQuantity} ${this.unit(p.unit, p.systemQuantity)}.`; },
  productDeleted(name) { return `${name} eliminado.`; },
  confirmDelete(name) { return `¿Eliminar ${name}?`; }
};

// ==================== 3. NORMALIZACIÓN DE VOZ Y COMANDOS (VoiceText.kt) ====================
const VoiceText = (() => {
  const UNITS = {
    cero: 0, un: 1, uno: 1, una: 1, dos: 2, tres: 3, cuatro: 4,
    cinco: 5, seis: 6, siete: 7, ocho: 8, nueve: 9
  };
  const TEENS = {
    diez: 10, once: 11, doce: 12, trece: 13, catorce: 14, quince: 15,
    dieciseis: 16, diecisiete: 17, dieciocho: 18, diecinueve: 19,
    veinte: 20, veintiun: 21, veintiuno: 21, veintiuna: 21, veintidos: 22,
    veintitres: 23, veinticuatro: 24, veinticinco: 25, veintiseis: 26,
    veintisiete: 27, veintiocho: 28, veintinueve: 29
  };
  const TENS = {
    treinta: 30, cuarenta: 40, cincuenta: 50, sesenta: 60,
    setenta: 70, ochenta: 80, noventa: 90
  };
  const HUNDREDS = {
    cien: 100, ciento: 100, doscientos: 200, doscientas: 200,
    trescientos: 300, trescientas: 300, cuatrocientos: 400, cuatrocientas: 400,
    quinientos: 500, quinientas: 500, seiscientos: 600, seiscientas: 600,
    setecientos: 700, setecientas: 700, ochocientos: 800, ochocientas: 800,
    novecientos: 900, novecientas: 900
  };
  const CODE_FILLERS = new Set([
    'pasillo', 'ubicacion', 'estante', 'nivel', 'rack', 'sku', 'codigo',
    'referencia', 'ref', 'producto', 'numero', 'guion'
  ]);
  const LETTER_NAMES = {
    be: 'b', ce: 'c', se: 'c', efe: 'f', ge: 'g', hache: 'h',
    jota: 'j', cota: 'j', ka: 'k', ele: 'l', eme: 'm', ene: 'n',
    pe: 'p', cu: 'q', erre: 'r', ese: 's', te: 't', uve: 'v',
    ve: 'v', equis: 'x', ye: 'y', zeta: 'z'
  };
  const ADD_PREFIXES = new Set(['agregar', 'agrega', 'nuevo', 'nueva', 'crear', 'crea']);
  const DELETE_PREFIXES = new Set(['eliminar', 'elimina', 'borrar', 'borra', 'quitar', 'quita']);
  const LIGHT_MODE_PHRASES = new Set([
    'modo claro', 'tema claro', 'activar modo claro', 'activar tema claro',
    'poner modo claro', 'poner tema claro', 'cambiar a modo claro', 'cambiar a tema claro',
    'modo dia', 'tema dia', 'claro'
  ]);
  const DARK_MODE_PHRASES = new Set([
    'modo oscuro', 'tema oscuro', 'activar modo oscuro', 'activar tema oscuro',
    'poner modo oscuro', 'poner tema oscuro', 'cambiar a modo oscuro', 'cambiar a tema oscuro',
    'modo noche', 'tema noche', 'oscuro'
  ]);
  const TOGGLE_THEME_PHRASES = new Set([
    'cambiar tema', 'cambiar modo', 'alternar tema', 'alternar modo'
  ]);
  const KNOWN_UNITS = {
    caja: 'cajas', cajas: 'cajas',
    rollo: 'rollos', rollos: 'rollos',
    par: 'pares', pares: 'pares',
    pallet: 'pallets', pallets: 'pallets', pale: 'pallets', pales: 'pallets',
    paquete: 'paquetes', paquetes: 'paquetes',
    bolsa: 'bolsas', bolsas: 'bolsas',
    unidad: 'unidades', unidades: 'unidades'
  };

  function basic(text) {
    const noAccents = (text || '').toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '');
    return noAccents.replace(/[^a-z0-9ñ]+/g, ' ').trim().replace(/\s+/g, ' ');
  }

  function parseNumberAt(tokens, start) {
    let i = start;
    let total = 0;
    let consumed = false;
    const peek = (off = 0) => tokens[i + off];

    const uBeforeMil = peek() !== undefined ? (UNITS[peek()] ?? TEENS[peek()] ?? TENS[peek()]) : undefined;
    if (peek() === 'mil') {
      total = 1000; i++; consumed = true;
    } else if (uBeforeMil !== undefined && peek(1) === 'mil') {
      total = uBeforeMil * 1000; i += 2; consumed = true;
    }
    if (HUNDREDS[peek()] !== undefined) {
      total += HUNDREDS[peek()]; i++; consumed = true;
    }
    const tens = TENS[peek()];
    const teen = TEENS[peek()];
    const unit = UNITS[peek()];
    if (tens !== undefined) {
      total += tens; i++; consumed = true;
      if (peek() === 'y' && UNITS[peek(1)] !== undefined && peek(1) !== 'cero') {
        total += UNITS[peek(1)]; i += 2;
      }
    } else if (teen !== undefined) {
      total += teen; i++; consumed = true;
    } else if (unit !== undefined && !(consumed && unit === 0)) {
      total += unit; i++; consumed = true;
    }
    return consumed ? [total, i] : null;
  }

  function convertNumbers(tokens) {
    const out = [];
    let i = 0;
    while (i < tokens.length) {
      const parsed = parseNumberAt(tokens, i);
      if (!parsed) {
        out.push(tokens[i]);
        i++;
      } else {
        out.push(String(parsed[0]));
        i = parsed[1];
      }
    }
    return out;
  }

  function isSpelledToken(t) {
    return Boolean(t) && (t.length === 1 || LETTER_NAMES[t] !== undefined || /^\d+$/.test(t));
  }

  function convertLetterNames(tokens) {
    const out = [];
    let i = 0;
    while (i < tokens.length) {
      const t = tokens[i];
      if (t === 'doble' && (tokens[i + 1] === 've' || tokens[i + 1] === 'uve')) {
        out.push('w'); i += 2; continue;
      }
      if ((t === 'uve' || t === 've') && tokens[i + 1] === 'doble') {
        out.push('w'); i += 2; continue;
      }
      if (LETTER_NAMES[t] !== undefined) {
        out.push(LETTER_NAMES[t]);
      } else if (t === 'de' && (isSpelledToken(tokens[i - 1]) || isSpelledToken(tokens[i + 1]))) {
        out.push('d');
      } else {
        out.push(t);
      }
      i++;
    }
    return out;
  }

  function joinSpelledLetters(tokens) {
    const out = [];
    let buf = '';
    const flush = () => { if (buf) { out.push(buf); buf = ''; } };
    for (const t of tokens) {
      if (t.length === 1 && /^[a-zñ]$/.test(t)) {
        buf += t;
      } else {
        flush();
        out.push(t);
      }
    }
    flush();
    return out;
  }

  function normalize(text) {
    const tokens = basic(text).split(' ').filter(Boolean);
    const withNumbers = convertNumbers(tokens);
    return joinSpelledLetters(convertLetterNames(withNumbers)).join(' ');
  }

  function withoutFillers(text) {
    return basic(text).split(' ').filter((w) => !CODE_FILLERS.has(w)).join(' ');
  }

  function compact(text) {
    return normalize(withoutFillers(text)).replace(/\s+/g, '');
  }

  function segmentKey(text) {
    const norm = normalize(withoutFillers(text));
    const matches = norm.match(/[a-zñ]+|[0-9]+/g) || [];
    return matches.map((g) => (/^\d/.test(g) ? (g.replace(/^0+/, '') || '0') : g)).join('|');
  }

  function parseQuantity(text) {
    const nums = normalize(text)
      .split(' ')
      .map((t) => (/^\d+$/.test(t) ? parseInt(t, 10) : NaN))
      .filter((n) => !Number.isNaN(n));
    return nums.length > 0 ? nums[0] : null;
  }

  function parseCommand(text) {
    const basicText = basic(text);
    if (LIGHT_MODE_PHRASES.has(basicText) || basicText.startsWith('modo claro') || basicText.startsWith('tema claro')) {
      return { type: 'SET_THEME', dark: false };
    }
    if (DARK_MODE_PHRASES.has(basicText) || basicText.startsWith('modo oscuro') || basicText.startsWith('tema oscuro')) {
      return { type: 'SET_THEME', dark: true };
    }
    if (TOGGLE_THEME_PHRASES.has(basicText)) {
      return { type: 'TOGGLE_THEME' };
    }
    const tokens = basicText.split(' ').filter(Boolean);
    const first = tokens[0];
    if (!first) return { type: 'SEARCH', query: text };
    let restTokens = tokens.slice(1);
    if (restTokens[0] === 'producto') restTokens = restTokens.slice(1);
    const rest = restTokens.join(' ').trim();
    if (ADD_PREFIXES.has(first)) return { type: 'ADD', details: rest };
    if (DELETE_PREFIXES.has(first) && rest) return { type: 'DELETE', query: rest };
    return { type: 'SEARCH', query: text };
  }

  function parseNewProduct(rawText, defaults = {}) {
    const defaultName = defaults.name ?? '';
    const defaultLocation = defaults.locationCode ?? 'A-01-01';
    const defaultUnit = defaults.unit ?? 'unidades';
    const defaultQuantity = defaults.quantity ?? 10;

    const cmd = parseCommand(rawText);
    const cleaned = cmd.type === 'ADD' ? cmd.details : basic(rawText);
    const normalizedTokens = convertLetterNames(convertNumbers(basic(cleaned).split(' ').filter(Boolean)));
    if (normalizedTokens.length === 0) {
      return { name: defaultName, locationCode: defaultLocation, unit: defaultUnit, quantity: defaultQuantity };
    }

    let location = defaultLocation;
    let quantity = null;
    let unit = null;
    const nameTokens = [];

    let i = 0;
    while (i < normalizedTokens.length) {
      const t = normalizedTokens[i];
      if ((t === 'pasillo' || t === 'ubicacion') && i + 1 < normalizedTokens.length) {
        const aisle = (normalizedTokens[i + 1] || '').toUpperCase();
        const shelf = parseInt(normalizedTokens[i + 2], 10);
        const level = parseInt(normalizedTokens[i + 3], 10);
        if (/^[A-Z]$/.test(aisle) && !Number.isNaN(shelf) && !Number.isNaN(level)) {
          location = `${aisle}-${String(shelf).padStart(2, '0')}-${String(level).padStart(2, '0')}`;
          i += 4;
          continue;
        } else if (/^[A-Z]$/.test(aisle) && !Number.isNaN(shelf)) {
          location = `${aisle}-${String(shelf).padStart(2, '0')}-01`;
          i += 3;
          continue;
        }
      }
      if ((t === 'cantidad' || t === 'stock') && /^\d+$/.test(normalizedTokens[i + 1] || '')) {
        quantity = parseInt(normalizedTokens[i + 1], 10);
        i += 2;
        continue;
      }
      const asInt = /^\d+$/.test(t) ? parseInt(t, 10) : null;
      const lastIdx = normalizedTokens.length - 1;
      const isTrailingNumber = asInt !== null && (
        i === lastIdx || (i === lastIdx - 1 && KNOWN_UNITS[normalizedTokens[lastIdx]] !== undefined)
      );
      if (isTrailingNumber && nameTokens.length > 0) {
        quantity = asInt;
        const nextUnit = KNOWN_UNITS[normalizedTokens[i + 1]];
        if (nextUnit) {
          unit = nextUnit;
          i += 2;
        } else {
          i += 1;
        }
        continue;
      }
      nameTokens.push(t);
      i++;
    }

    const firstWord = nameTokens[0];
    const inferredUnit = unit || (firstWord === 'guante' || firstWord === 'guantes' ? 'pares' : KNOWN_UNITS[firstWord]) || defaultUnit;
    const joinedName = nameTokens.join(' ').trim();
    const finalName = joinedName
      ? joinedName.charAt(0).toUpperCase() + joinedName.slice(1)
      : defaultName;

    return {
      name: finalName,
      locationCode: location,
      unit: inferredUnit,
      quantity: quantity !== null ? quantity : defaultQuantity
    };
  }

  return {
    basic,
    normalize,
    compact,
    segmentKey,
    parseQuantity,
    parseCommand,
    parseNewProduct
  };
})();

// ==================== 4. RANKING DE BÚSQUEDA TOLERANTE A VOZ (ProductMatcher.kt) ====================
const ProductMatcher = (() => {
  const MIN_SCORE = 0.35;

  function similarity(a, b) {
    if (a === b) return 1.0;
    if (!a || !b) return 0.0;
    let prev = Array.from({ length: b.length + 1 }, (_, idx) => idx);
    let curr = new Array(b.length + 1);
    for (let i = 1; i <= a.length; i++) {
      curr[0] = i;
      for (let j = 1; j <= b.length; j++) {
        const cost = a[i - 1] === b[j - 1] ? 0 : 1;
        curr[j] = Math.min(curr[j - 1] + 1, prev[j] + 1, prev[j - 1] + cost);
      }
      const tmp = prev; prev = curr; curr = tmp;
    }
    return 1.0 - prev[b.length] / Math.max(a.length, b.length);
  }

  function nameScore(qTokens, qCompact, name) {
    const nTokens = VoiceText.normalize(name).split(' ').filter(Boolean);
    if (nTokens.length === 0) return 0.0;
    let tokenHits = 0;
    for (const qt of qTokens) {
      let maxSim = 0;
      for (const nt of nTokens) {
        const s = similarity(qt, nt);
        if (s > maxSim) maxSim = s;
      }
      tokenHits += maxSim;
    }
    const overlap = tokenHits / Math.max(qTokens.length, nTokens.length);
    const whole = similarity(qCompact, VoiceText.compact(name));
    return (0.6 * overlap + 0.4 * whole) * 0.85;
  }

  function searchOne(query, catalog) {
    const qCompact = VoiceText.compact(query);
    const qSegments = VoiceText.segmentKey(query);
    const qTokens = [...new Set(VoiceText.normalize(query).split(' ').filter(Boolean))];
    if (!qCompact) return [];

    const results = [];
    for (const p of catalog) {
      if (VoiceText.compact(p.sku) === qCompact) {
        results.push({ product: p, score: 1.0, matchedBy: 'SKU' });
      } else if (VoiceText.segmentKey(p.locationCode) === qSegments && qSegments.includes('|')) {
        results.push({ product: p, score: 0.9, matchedBy: 'LOCATION' });
      } else {
        const s = nameScore(qTokens, qCompact, p.name);
        if (s >= MIN_SCORE) {
          results.push({ product: p, score: s, matchedBy: 'NAME' });
        }
      }
    }
    return results;
  }

  function search(queries, catalog, limit = 5) {
    const best = new Map();
    for (const q of queries.filter((x) => x && x.trim())) {
      for (const hit of searchOne(q, catalog)) {
        const cur = best.get(hit.product.sku);
        if (!cur || hit.score > cur.score) {
          best.set(hit.product.sku, hit);
        }
      }
    }
    return [...best.values()].sort((a, b) => b.score - a.score).slice(0, limit);
  }

  function isDecisive(hits) {
    if (!hits || hits.length === 0) return false;
    const top = hits[0];
    const gap = hits.length === 1 ? 1.0 : top.score - hits[1].score;
    return (top.score >= 0.9 && gap >= 0.15) || (top.score >= 0.8 && gap >= 0.2);
  }

  return { search, isDecisive };
})();

// ==================== 5. COMPONENTE CANVAS @beui/voice-orb (VoiceOrb.kt) ====================
class VoiceOrbRenderer {
  constructor() {
    this.instances = [];
    this.startTime = performance.now();
    this.animate = this.animate.bind(this);
    requestAnimationFrame(this.animate);
  }

  register(canvas, getActivity, isEnabled = () => true) {
    if (!canvas) return;
    this.instances.push({ canvas, ctx: canvas.getContext('2d'), getActivity, isEnabled, smoothActivity: 0.15 });
  }

  buildLiquidOrbPath(ctx, cx, cy, baseRadius, phase, fastPhase, activity, amplitudeScale) {
    const points = 72;
    const idleAmp = baseRadius * 0.035 * amplitudeScale;
    const activeAmp = baseRadius * (0.14 * activity) * amplitudeScale;
    const totalAmp = idleAmp + activeAmp;

    ctx.beginPath();
    for (let i = 0; i < points; i++) {
      const theta = (i / points) * Math.PI * 2;
      const wave1 = Math.sin(theta * 3 + phase) * 0.50;
      const wave2 = Math.cos(theta * 4 - fastPhase * 1.4) * (0.30 + activity * 0.25);
      const wave3 = Math.sin(theta * 6 + fastPhase * 2.1) * (0.15 + activity * 0.35);
      const r = baseRadius + totalAmp * (wave1 + wave2 + wave3);
      const x = cx + Math.cos(theta) * r;
      const y = cy + Math.sin(theta) * r;
      if (i === 0) ctx.moveTo(x, y);
      else ctx.lineTo(x, y);
    }
    ctx.closePath();
  }

  animate(now) {
    const elapsed = now - this.startTime;
    const phase = ((elapsed % 3600) / 3600) * Math.PI * 2;
    const fastPhase = ((elapsed % 1500) / 1500) * Math.PI * 2;
    const breatheCycle = (elapsed % 3600) / 1800;
    const breathe = 0.95 + 0.10 * (breatheCycle <= 1 ? breatheCycle : 2 - breatheCycle);

    for (const item of this.instances) {
      const { canvas, ctx, getActivity, isEnabled } = item;
      const enabled = isEnabled();
      const targetAct = enabled ? Math.max(0, Math.min(1, getActivity())) : 0;
      item.smoothActivity += (targetAct - item.smoothActivity) * 0.14;
      const act = item.smoothActivity;

      const w = canvas.width;
      const h = canvas.height;
      ctx.clearRect(0, 0, w, h);

      const alphaMul = enabled ? 1.0 : 0.4;
      const cx = w / 2;
      const cy = h / 2;
      const minDim = Math.min(w, h);
      const baseRadius = (minDim * 0.36) * breathe * (1 + act * 0.14);
      const time = phase + fastPhase * act * 0.65;

      // 1. Halo ambiental exterior
      const glowRadius = baseRadius * (1.35 + act * 0.28);
      const glowGrad = ctx.createRadialGradient(cx, cy, 0, cx, cy, glowRadius);
      glowGrad.addColorStop(0, `rgba(90, 216, 255, ${(0.38 + act * 0.25) * alphaMul})`);
      glowGrad.addColorStop(0.55, `rgba(79, 140, 255, ${(0.18 + act * 0.12) * alphaMul})`);
      glowGrad.addColorStop(1, 'rgba(0, 0, 0, 0)');
      ctx.fillStyle = glowGrad;
      ctx.beginPath();
      ctx.arc(cx, cy, glowRadius, 0, Math.PI * 2);
      ctx.fill();

      // 2. Silueta líquida principal
      this.buildLiquidOrbPath(ctx, cx, cy, baseRadius, time, fastPhase, act, 1.0);
      const focalX = cx + Math.cos(phase) * baseRadius * 0.28;
      const focalY = cy + Math.sin(phase * 0.8) * baseRadius * 0.28;
      const mainGrad = ctx.createRadialGradient(focalX, focalY, baseRadius * 0.05, cx, cy, baseRadius * 1.25);
      mainGrad.addColorStop(0, `rgba(90, 216, 255, ${alphaMul})`);
      mainGrad.addColorStop(0.38, `rgba(45, 212, 191, ${alphaMul})`);
      mainGrad.addColorStop(0.72, `rgba(79, 140, 255, ${alphaMul})`);
      mainGrad.addColorStop(1, `rgba(129, 140, 248, ${0.92 * alphaMul})`);
      ctx.fillStyle = mainGrad;
      ctx.fill();

      // 3. Lóbulo interno de pigmento secundario
      ctx.save();
      ctx.globalCompositeOperation = 'screen';
      const innerCx = cx + Math.sin(phase * 1.2) * baseRadius * 0.12;
      const innerCy = cy + Math.cos(phase * 0.9) * baseRadius * 0.12;
      this.buildLiquidOrbPath(ctx, innerCx, innerCy, baseRadius * 0.76, -time * 1.15, fastPhase * 1.3, act, 1.25);
      const innerGrad = ctx.createRadialGradient(
        cx - Math.cos(fastPhase) * baseRadius * 0.22,
        cy - Math.sin(fastPhase) * baseRadius * 0.22,
        0,
        innerCx,
        innerCy,
        baseRadius * 0.9
      );
      innerGrad.addColorStop(0, `rgba(255, 209, 102, ${(0.42 + act * 0.28) * alphaMul})`);
      innerGrad.addColorStop(0.55, `rgba(45, 212, 191, ${(0.55 + act * 0.20) * alphaMul})`);
      innerGrad.addColorStop(1, 'rgba(0, 0, 0, 0)');
      ctx.fillStyle = innerGrad;
      ctx.fill();
      ctx.restore();

      // 4. Brillo especular superior 3D
      const hlX = cx - baseRadius * 0.26 + Math.cos(phase) * baseRadius * 0.06;
      const hlY = cy - baseRadius * 0.28 + Math.sin(phase) * baseRadius * 0.06;
      const hlRadius = baseRadius * 0.52;
      const hlGrad = ctx.createRadialGradient(hlX, hlY, 0, hlX, hlY, hlRadius);
      hlGrad.addColorStop(0, `rgba(255, 255, 255, ${0.65 * alphaMul})`);
      hlGrad.addColorStop(0.45, `rgba(255, 255, 255, ${0.18 * alphaMul})`);
      hlGrad.addColorStop(1, 'rgba(255, 255, 255, 0)');
      ctx.fillStyle = hlGrad;
      ctx.beginPath();
      ctx.arc(hlX, hlY, hlRadius, 0, Math.PI * 2);
      ctx.fill();

      // 5. Borde iridiscente sutil
      this.buildLiquidOrbPath(ctx, cx, cy, baseRadius, time, fastPhase, act, 1.0);
      ctx.strokeStyle = `rgba(255, 255, 255, ${0.42 * alphaMul})`;
      ctx.lineWidth = 1.6 + act * 1.1;
      ctx.stroke();
    }

    // Dibujar ondas Ripples en ListeningOverlay si está activo
    const ripplesCanvas = document.getElementById('listeningRipplesCanvas');
    if (ripplesCanvas && appState.listening !== 'IDLE') {
      const rCtx = ripplesCanvas.getContext('2d');
      const rw = ripplesCanvas.width;
      const rh = ripplesCanvas.height;
      rCtx.clearRect(0, 0, rw, rh);
      if (appState.listening === 'LISTENING') {
        const rPhase = (elapsed % 1600) / 1600;
        const base = 32;
        const max = Math.min(rw, rh) / 2 - 4;
        const signalColor = appState.isDark ? '90, 216, 255' : '2, 132, 199';
        for (let i = 0; i < 3; i++) {
          const p = (rPhase + i / 3) % 1;
          const radius = base + (max - base) * p;
          rCtx.beginPath();
          rCtx.arc(rw / 2, rh / 2, radius, 0, Math.PI * 2);
          rCtx.strokeStyle = `rgba(${signalColor}, ${(1 - p) * 0.55})`;
          rCtx.lineWidth = 2.2;
          rCtx.stroke();
        }
      }
    }

    requestAnimationFrame(this.animate);
  }
}

// ==================== 6. ESTADO Y FLUJO DE LA APP (AuditViewModel.kt) ====================
const serverPreconfig = window.__ECHOSTOCK_SERVER_CONFIG__ || {};
const savedConfig = JSON.parse(localStorage.getItem('echostock_web_config') || '{}');

const appState = {
  route: 'HOME', // HOME | RESULTS | PRODUCT | COUNT | ADD_PRODUCT | DONE
  history: ['HOME'],
  isDark: localStorage.getItem('echostock_theme_dark') !== 'false',
  viewMode: new URLSearchParams(window.location.search).get('mode') === 'watch' ? 'watch' : 'emulator',

  // Configuración de conexión
  apiBaseUrl: savedConfig.apiBaseUrl || serverPreconfig.apiBaseUrl || '/api/v1',
  apiKey: savedConfig.apiKey || serverPreconfig.apiKey || 'echostock-dev-key',
  operatorCode: savedConfig.operatorCode || serverPreconfig.operatorCode || 'OP-07',
  deviceId: 'wear-web-01',

  // Estado de red y sincronización (RF-005 / RF-006)
  simulatedOffline: false,
  connection: 'ONLINE', // ONLINE | OFFLINE | UNKNOWN
  pendingAudits: JSON.parse(localStorage.getItem('echostock_pending_audits') || '[]'),
  catalog: JSON.parse(localStorage.getItem('echostock_catalog') || 'null') || [...SEED_PRODUCTS],

  // Flujo de pantallas
  message: null, // { type, text, query, isDark }
  searching: false,
  hits: [],
  resultsTitle: 'Catálogo',
  offlineResults: false,
  product: null,
  counted: 0,
  submitting: false,
  confirmingDelete: false,
  newProductDraft: { name: '', locationCode: 'A-01-01', unit: 'unidades', quantity: 10 },
  done: null,

  // Voz
  listening: 'IDLE', // IDLE | PREPARING | LISTENING
  listenTarget: 'SEARCH', // SEARCH | COUNT | NEW_PRODUCT
  listenPrompt: '',
  listenPartial: '',
  micActivity: 0
};

function saveCatalog() {
  localStorage.setItem('echostock_catalog', JSON.stringify(appState.catalog));
}

function savePendingAudits() {
  localStorage.setItem('echostock_pending_audits', JSON.stringify(appState.pendingAudits));
  renderStatusPill();
}

// ==================== 7. HAPTICS Y TTS (Haptics.kt & Speaker.kt) ====================
const Haptics = {
  audioCtx: null,
  play(type) {
    // 1. Anillo de color en el borde del reloj (HapticPulseOverlay)
    const ring = document.getElementById('hapticRing');
    const chassis = document.getElementById('watchChassis');
    const dark = appState.isDark;
    const colors = {
      TAP: dark ? '#FFFFFF' : '#0F172A',
      HEARD: dark ? '#5AD8FF' : '#0284C7',
      CONFIRMED: dark ? '#2EE07E' : '#16A34A',
      DISCREPANCY: dark ? '#FF4F4F' : '#DC2626',
      QUEUED: dark ? '#FFB627' : '#D97706',
      ERROR: dark ? '#FF4F4F' : '#DC2626'
    };
    const strong = type !== 'TAP';
    const c = colors[type] || colors.TAP;

    if (ring) {
      ring.style.borderWidth = strong ? '7px' : '4px';
      ring.style.borderColor = c;
      ring.style.opacity = strong ? '1' : '0.48';
      clearTimeout(this._ringTimer);
      this._ringTimer = setTimeout(() => {
        ring.style.transition = `opacity ${strong ? 620 : 250}ms cubic-bezier(0.4, 0, 0.2, 1)`;
        ring.style.opacity = '0';
      }, 30);
    }

    // 2. Sacudida física sutil del chasis en vibraciones fuertes
    if (strong && chassis) {
      chassis.classList.remove('vibrating');
      void chassis.offsetWidth;
      chassis.classList.add('vibrating');
    }

    // 3. Vibración nativa del navegador/dispositivo (Web Vibration API, mismos patrones de Haptics.kt)
    if (navigator.vibrate) {
      const patterns = {
        TAP: [18],
        HEARD: [25, 45, 35],
        CONFIRMED: [70],
        DISCREPANCY: [45, 50, 45, 50, 70],
        QUEUED: [60, 60, 25],
        ERROR: [110, 40, 110]
      };
      try { navigator.vibrate(patterns[type] || [18]); } catch (_) {}
    }

    // 4. Micro-pulso háptico acústico sutil
    try {
      if (!this.audioCtx) {
        const AudioContextClass = window.AudioContext || window.webkitAudioContext;
        if (AudioContextClass) this.audioCtx = new AudioContextClass();
      }
      if (this.audioCtx && this.audioCtx.state === 'running') {
        const osc = this.audioCtx.createOscillator();
        const gain = this.audioCtx.createGain();
        osc.type = 'sine';
        osc.frequency.value = type === 'CONFIRMED' ? 190 : type === 'ERROR' || type === 'DISCREPANCY' ? 115 : 150;
        gain.gain.setValueAtTime(strong ? 0.045 : 0.02, this.audioCtx.currentTime);
        gain.gain.exponentialRampToValueAtTime(0.001, this.audioCtx.currentTime + (strong ? 0.09 : 0.035));
        osc.connect(gain);
        gain.connect(this.audioCtx.destination);
        osc.start();
        osc.stop(this.audioCtx.currentTime + (strong ? 0.095 : 0.04));
      }
    } catch (_) {}
  }
};

const Speaker = {
  captionTimer: null,
  speak(text) {
    if (!text) return;
    // Mostrar CaptionBanner en la parte superior del reloj
    const banner = document.getElementById('captionBanner');
    const captionText = document.getElementById('captionText');
    if (banner && captionText) {
      captionText.textContent = text;
      banner.classList.add('visible');
      clearTimeout(this.captionTimer);
      const duration = 1600 + text.length * 55;
      this.captionTimer = setTimeout(() => {
        banner.classList.remove('visible');
      }, duration);
    }

    // Síntesis de voz Web Speech API (RF-002)
    if ('speechSynthesis' in window) {
      try {
        window.speechSynthesis.cancel();
        const utter = new SpeechSynthesisUtterance(text);
        utter.lang = 'es-ES';
        utter.rate = 1.04;
        const voices = window.speechSynthesis.getVoices();
        const esVoice = voices.find((v) => v.lang && v.lang.toLowerCase().startsWith('es'));
        if (esVoice) utter.voice = esVoice;
        window.speechSynthesis.speak(utter);
      } catch (_) {}
    }
  },
  stop() {
    if ('speechSynthesis' in window) {
      try { window.speechSynthesis.cancel(); } catch (_) {}
    }
    const banner = document.getElementById('captionBanner');
    if (banner) banner.classList.remove('visible');
  }
};

// ==================== 8. CLIENTE API Y SINCRONIZACIÓN (EchoStockApi + Repositories) ====================
async function apiFetch(path, options = {}) {
  if (appState.simulatedOffline) {
    throw new Error('Modo offline simulado activo');
  }
  const base = appState.apiBaseUrl.replace(/\/+$/, '');
  const url = `${base}${path}`;
  const headers = {
    'Accept': 'application/json',
    'X-Api-Key': appState.apiKey,
    ...(options.headers || {})
  };
  if (options.body && !headers['Content-Type']) {
    headers['Content-Type'] = 'application/json';
  }
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 4500);
  try {
    const resp = await fetch(url, { ...options, headers, signal: controller.signal });
    return resp;
  } finally {
    clearTimeout(timeout);
  }
}

async function checkServerHealth() {
  if (appState.simulatedOffline) {
    appState.connection = 'OFFLINE';
    renderStatusPill();
    return false;
  }
  try {
    const resp = await apiFetch('/health');
    if (resp.ok) {
      appState.connection = 'ONLINE';
      renderStatusPill();
      await syncPendingAudits();
      await refreshCatalogFromServer();
      return true;
    }
  } catch (_) {}
  appState.connection = 'OFFLINE';
  renderStatusPill();
  return false;
}

async function refreshCatalogFromServer() {
  if (appState.simulatedOffline) return;
  try {
    const resp = await apiFetch('/products?page=0&size=100');
    if (resp.ok) {
      const page = await resp.json();
      if (Array.isArray(page.content) && page.content.length > 0) {
        appState.catalog = page.content;
        saveCatalog();
      }
    }
  } catch (_) {}
}

async function syncPendingAudits() {
  if (appState.simulatedOffline || appState.pendingAudits.length === 0) return;
  try {
    const batch = [...appState.pendingAudits];
    const resp = await apiFetch('/audits/batch', {
      method: 'POST',
      body: JSON.stringify({ audits: batch })
    });
    if (resp.ok) {
      const data = await resp.json();
      const doneIds = new Set([...(data.accepted || []), ...(data.duplicates || []), ...(data.rejected || []).map((r) => r.clientAuditId)]);
      appState.pendingAudits = appState.pendingAudits.filter((a) => !doneIds.has(a.clientAuditId));
      savePendingAudits();
      appState.connection = 'ONLINE';
      renderStatusPill();
      loadSupervisorAudits();
    }
  } catch (_) {}
}

// ==================== 9. NAVEGACIÓN Y ACCIONES DEL RELOJ ====================
function navigateTo(route, replace = false) {
  appState.route = route;
  if (replace) {
    appState.history = ['HOME', ...(route === 'HOME' ? [] : [route])];
  } else if (route === 'HOME') {
    appState.history = ['HOME'];
  } else if (appState.history[appState.history.length - 1] !== route) {
    appState.history.push(route);
  }
  renderWatchScreen();
}

function navigateBack() {
  if (appState.listening !== 'IDLE') {
    stopListening(true);
    return;
  }
  if (appState.route === 'HOME') return;
  Haptics.play('TAP');
  appState.history.pop();
  const prev = appState.history[appState.history.length - 1] || 'HOME';
  appState.route = prev;
  renderWatchScreen();
}

function toggleTheme() {
  Haptics.play('TAP');
  setDarkMode(!appState.isDark, true);
}

function setDarkMode(dark, announce = true) {
  appState.isDark = Boolean(dark);
  localStorage.setItem('echostock_theme_dark', String(appState.isDark));
  document.getElementById('watchScreen').setAttribute('data-theme', appState.isDark ? 'dark' : 'light');
  if (announce) {
    const modeName = appState.isDark ? 'oscuro' : 'claro';
    Speaker.speak(`Modo ${modeName} activado`);
    appState.message = { type: 'THEME', isDark: appState.isDark };
  }
  renderWatchScreen();
}

// ---- Búsqueda de productos (RF-001) ----
async function executeSearch(alternatives, forDeletion = false) {
  const shown = alternatives[0];
  appState.searching = true;
  appState.message = { type: 'SEARCHING', query: shown };
  renderWatchScreen();

  let hits = [];
  let offline = false;

  try {
    const resp = await apiFetch(`/products/search?q=${encodeURIComponent(shown)}&limit=5`);
    if (resp.ok) {
      const data = await resp.json();
      hits = (data.results || []).map((r) => ({
        product: r.product,
        score: r.score,
        matchedBy: r.matchedBy
      }));
      appState.connection = 'ONLINE';
    } else {
      throw new Error('HTTP ' + resp.status);
    }
  } catch (_) {
    offline = true;
    appState.connection = 'OFFLINE';
    hits = ProductMatcher.search(alternatives, appState.catalog, 5);
  }

  appState.searching = false;
  renderStatusPill();

  if (hits.length === 0) {
    Haptics.play('ERROR');
    Speaker.speak(Phrases.notFound(shown));
    appState.message = { type: 'NOT_FOUND', query: shown };
    renderWatchScreen();
  } else if (forDeletion || ProductMatcher.isDecisive(hits)) {
    appState.message = null;
    appState.offlineResults = offline;
    openProduct(hits[0].product, forDeletion);
  } else {
    appState.message = null;
    appState.hits = hits;
    appState.resultsTitle = `“${shown}”`;
    appState.offlineResults = offline;
    navigateTo('RESULTS');
  }
}

function onSearchVoice(alternatives) {
  const valid = (alternatives || []).map((s) => s.trim()).filter(Boolean);
  if (valid.length === 0) {
    Haptics.play('ERROR');
    Speaker.speak(Phrases.NOT_UNDERSTOOD);
    appState.message = { type: 'NOT_UNDERSTOOD' };
    renderWatchScreen();
    return;
  }
  Haptics.play('HEARD');
  const cmd = VoiceText.parseCommand(valid[0]);
  if (cmd.type === 'SET_THEME') {
    Haptics.play('CONFIRMED');
    setDarkMode(cmd.dark, true);
  } else if (cmd.type === 'TOGGLE_THEME') {
    toggleTheme();
  } else if (cmd.type === 'ADD') {
    openAddProduct(cmd.details);
  } else if (cmd.type === 'DELETE') {
    executeSearch([cmd.query], true);
  } else {
    executeSearch(valid, false);
  }
}

async function browseCatalog() {
  Haptics.play('TAP');
  await refreshCatalogFromServer();
  const sorted = [...appState.catalog].sort((a, b) => a.locationCode.localeCompare(b.locationCode));
  appState.hits = sorted.map((p) => ({ product: p, score: 0, matchedBy: 'NAME' }));
  appState.resultsTitle = 'Catálogo';
  appState.offlineResults = appState.connection === 'OFFLINE';
  appState.message = null;
  navigateTo('RESULTS');
}

// ---- Pantalla de Producto (RF-002 / RF-003 / RF-004) ----
function openProduct(product, confirmDelete = false) {
  appState.product = product;
  appState.counted = product.systemQuantity;
  appState.confirmingDelete = confirmDelete;
  appState.done = null;
  if (confirmDelete) {
    Speaker.speak(Phrases.confirmDelete(product.name));
  } else {
    Speaker.speak(Phrases.systemStatus(product));
  }
  navigateTo('PRODUCT');
}

function repeatProductStatus() {
  if (appState.product) {
    Haptics.play('TAP');
    Speaker.speak(Phrases.systemStatus(appState.product));
  }
}

function requestDeleteProduct() {
  if (!appState.product) return;
  Haptics.play('TAP');
  appState.confirmingDelete = true;
  Speaker.speak(Phrases.confirmDelete(appState.product.name));
  renderWatchScreen();
}

function cancelDeleteProduct() {
  Haptics.play('TAP');
  appState.confirmingDelete = false;
  renderWatchScreen();
}

async function confirmDeleteProduct() {
  const p = appState.product;
  if (!p || appState.submitting) return;
  appState.submitting = true;
  renderWatchScreen();

  appState.catalog = appState.catalog.filter((item) => item.sku !== p.sku);
  saveCatalog();

  try {
    await apiFetch(`/products/${encodeURIComponent(p.sku)}`, { method: 'DELETE' });
    appState.connection = 'ONLINE';
  } catch (_) {
    appState.connection = 'OFFLINE';
  }

  appState.submitting = false;
  Haptics.play('CONFIRMED');
  Speaker.speak(Phrases.productDeleted(p.name));
  appState.message = { type: 'DELETED', name: p.name };
  navigateTo('HOME', true);
}

// ---- Conteo y Discrepancias (RF-004) ----
function openCountScreen() {
  if (!appState.product) return;
  Haptics.play('TAP');
  Speaker.stop();
  appState.counted = appState.product.systemQuantity;
  navigateTo('COUNT');
}

function adjustCount(delta, withHaptic = true) {
  if (appState.submitting) return;
  if (withHaptic) Haptics.play('TAP');
  const prev = appState.counted;
  appState.counted = Math.max(0, Math.min(99999, appState.counted + delta));
  if (appState.counted !== prev) {
    renderCountValues(delta > 0 ? 'up' : 'down');
  }
}

function onCountVoice(alternatives) {
  let parsed = null;
  for (const alt of alternatives || []) {
    const n = VoiceText.parseQuantity(alt);
    if (n !== null) { parsed = n; break; }
  }
  if (parsed === null) {
    Haptics.play('ERROR');
    Speaker.speak(Phrases.NOT_UNDERSTOOD);
    return;
  }
  Haptics.play('HEARD');
  appState.counted = Math.max(0, Math.min(99999, parsed));
  Speaker.speak(Phrases.countSet(appState.counted));
  renderWatchScreen();
}

// ---- Agregar Nuevo Producto por Voz ----
function openAddProduct(initialRaw = '') {
  Speaker.stop();
  Haptics.play('TAP');
  const seed = initialRaw || (appState.message?.type === 'NOT_FOUND' ? appState.message.query : '');
  appState.newProductDraft = seed
    ? VoiceText.parseNewProduct(seed)
    : { name: '', locationCode: 'A-01-01', unit: 'unidades', quantity: 10 };
  appState.message = null;
  Speaker.speak(Phrases.PROMPT_NEW_PRODUCT);
  navigateTo('ADD_PRODUCT');
}

function adjustNewProductQuantity(delta, withHaptic = true) {
  if (appState.submitting) return;
  if (withHaptic) Haptics.play('TAP');
  appState.newProductDraft.quantity = Math.max(0, Math.min(99999, appState.newProductDraft.quantity + delta));
  const el = document.getElementById('addProductQtyValue');
  if (el) el.textContent = String(appState.newProductDraft.quantity);
}

function onNewProductVoice(alternatives) {
  const text = (alternatives || []).find((s) => s && s.trim());
  if (!text) {
    Haptics.play('ERROR');
    Speaker.speak(Phrases.NOT_UNDERSTOOD);
    return;
  }
  Haptics.play('HEARD');
  appState.newProductDraft = VoiceText.parseNewProduct(text, appState.newProductDraft);
  Speaker.speak(`${appState.newProductDraft.name}, ${appState.newProductDraft.quantity} ${appState.newProductDraft.unit}.`);
  renderWatchScreen();
}

function generateSku(name) {
  const norm = name.toUpperCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/[^A-Z0-9]/g, '');
  const consonants = norm.replace(/[AEIOU0-9]/g, '');
  const prefix = (consonants.length >= 3 ? consonants : (norm + 'PRD')).slice(0, 3);
  const existing = new Set(appState.catalog.map((p) => p.sku));
  for (let i = 1; i <= 999; i++) {
    const candidate = `${prefix}-${String(i).padStart(3, '0')}`;
    if (!existing.has(candidate)) return candidate;
  }
  return `${prefix}-${Date.now() % 1000}`;
}

async function saveNewProduct() {
  const d = appState.newProductDraft;
  if (!d.name.trim() || appState.submitting) return;
  appState.submitting = true;
  renderWatchScreen();

  const sku = generateSku(d.name);
  let created = {
    sku,
    name: d.name.trim(),
    locationCode: d.locationCode,
    unit: d.unit,
    systemQuantity: d.quantity
  };

  try {
    const resp = await apiFetch('/products', {
      method: 'POST',
      body: JSON.stringify(created)
    });
    if (resp.ok) {
      created = await resp.json();
      appState.connection = 'ONLINE';
    }
  } catch (_) {
    appState.connection = 'OFFLINE';
  }

  appState.catalog = [created, ...appState.catalog.filter((p) => p.sku !== created.sku)];
  saveCatalog();
  appState.submitting = false;
  Haptics.play('CONFIRMED');
  Speaker.speak(Phrases.productAdded(created));
  appState.product = created;
  appState.counted = created.systemQuantity;
  appState.confirmingDelete = false;
  navigateTo('PRODUCT', true);
}

// ---- Envío de Auditoría (RF-003 / RF-005 / RF-006) ----
function generateUuid() {
  if (crypto && crypto.randomUUID) return crypto.randomUUID();
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0;
    return (c === 'x' ? r : (r & 0x3) | 0x8).toString(16);
  });
}

async function submitAudit(countedQty) {
  const p = appState.product;
  if (!p || appState.submitting) return;
  appState.submitting = true;
  renderWatchScreen();

  const difference = countedQty - p.systemQuantity;
  const result = difference === 0 ? 'MATCH' : 'DISCREPANCY';
  const draft = {
    clientAuditId: generateUuid(),
    sku: p.sku,
    operatorCode: appState.operatorCode,
    deviceId: appState.deviceId,
    systemQuantity: p.systemQuantity,
    countedQuantity: countedQty,
    result,
    recordedAt: new Date().toISOString()
  };

  let outcomeType = 'SYNCED'; // SYNCED | QUEUED | REJECTED
  let rejectReason = '';

  try {
    const resp = await apiFetch('/audits', {
      method: 'POST',
      body: JSON.stringify(draft)
    });
    if (resp.ok) {
      appState.connection = 'ONLINE';
      outcomeType = 'SYNCED';
      loadSupervisorAudits();
    } else if ([400, 404, 409, 422].includes(resp.status)) {
      appState.connection = 'ONLINE';
      outcomeType = 'REJECTED';
      const prob = await resp.json().catch(() => ({}));
      rejectReason = prob.detail || prob.title || `Rechazada (${resp.status})`;
    } else {
      throw new Error('HTTP ' + resp.status);
    }
  } catch (_) {
    appState.connection = 'OFFLINE';
    outcomeType = 'QUEUED';
    appState.pendingAudits.push(draft);
    savePendingAudits();
  }

  appState.submitting = false;
  renderStatusPill();

  if (outcomeType === 'SYNCED') {
    Haptics.play(result === 'MATCH' ? 'CONFIRMED' : 'DISCREPANCY');
  } else if (outcomeType === 'QUEUED') {
    Haptics.play('QUEUED');
  } else {
    Haptics.play('ERROR');
  }

  Speaker.speak(Phrases.outcome(result, difference, outcomeType, rejectReason));
  appState.done = { result, difference, outcomeType, reason: rejectReason, product: p };
  navigateTo('DONE', true);

  clearTimeout(appState._doneTimer);
  appState._doneTimer = setTimeout(() => {
    if (appState.route === 'DONE') resetToHome();
  }, outcomeType === 'REJECTED' ? 4000 : 2400);
}

function resetToHome() {
  clearTimeout(appState._doneTimer);
  Speaker.stop();
  appState.message = null;
  appState.done = null;
  navigateTo('HOME', true);
}

// ==================== 10. RECONOCIMIENTO DE VOZ Y SIMULADOR (ListeningOverlay) ====================
let activeRecognition = null;
let simulatedDictationTimer = null;

function startListening(target, promptText) {
  Speaker.stop();
  Haptics.play('TAP');
  appState.listenTarget = target;
  appState.listenPrompt = promptText;
  appState.listenPartial = '';
  appState.listening = 'LISTENING';
  appState.micActivity = 0.78;
  renderListeningOverlay();

  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
  if (!SpeechRecognition) {
    // En navegadores sin Web Speech API queda listo para recibir un comando del panel de demostración
    return;
  }

  try {
    if (activeRecognition) {
      try { activeRecognition.abort(); } catch (_) {}
    }
    const rec = new SpeechRecognition();
    activeRecognition = rec;
    rec.lang = 'es-ES';
    rec.continuous = false;
    rec.interimResults = true;
    rec.maxAlternatives = 3;

    let finalAlternatives = null;

    rec.onresult = (event) => {
      let interim = '';
      for (let i = event.resultIndex; i < event.results.length; i++) {
        const res = event.results[i];
        if (res.isFinal) {
          finalAlternatives = Array.from(res).map((a) => a.transcript);
        } else {
          interim += res[0].transcript;
        }
      }
      appState.listenPartial = finalAlternatives ? finalAlternatives[0] : interim;
      appState.micActivity = 1.0;
      renderListeningOverlay();
    };

    rec.onerror = () => {
      if (appState.listening !== 'IDLE' && !simulatedDictationTimer) {
        stopListening(true);
        appState.message = { type: 'VOICE_UNAVAILABLE' };
        renderWatchScreen();
      }
    };

    rec.onend = () => {
      if (simulatedDictationTimer) return;
      if (appState.listening === 'IDLE') return;
      const alts = finalAlternatives || (appState.listenPartial ? [appState.listenPartial] : null);
      appState.listening = 'IDLE';
      appState.micActivity = 0;
      renderListeningOverlay();
      dispatchVoiceResult(alts);
    };

    rec.start();
  } catch (_) {}
}

function stopListening(cancel = false) {
  clearTimeout(simulatedDictationTimer);
  simulatedDictationTimer = null;
  if (activeRecognition) {
    try { activeRecognition.stop(); } catch (_) {}
    activeRecognition = null;
  }
  const partial = appState.listenPartial;
  appState.listening = 'IDLE';
  appState.micActivity = 0;
  renderListeningOverlay();
  if (!cancel && partial) {
    dispatchVoiceResult([partial]);
  }
}

function dispatchVoiceResult(alternatives) {
  if (appState.listenTarget === 'COUNT') {
    onCountVoice(alternatives);
  } else if (appState.listenTarget === 'NEW_PRODUCT') {
    onNewProductVoice(alternatives);
  } else {
    onSearchVoice(alternatives);
  }
}

/**
 * Simula hablarle al micrófono del reloj (para la demostración en navegador con 1 clic).
 * Muestra el ListeningOverlay con las ondas y transcribe palabra por palabra en vivo.
 */
function simulateVoiceCommand(phrase, forceTarget = null) {
  if (activeRecognition) {
    try { activeRecognition.abort(); } catch (_) {}
    activeRecognition = null;
  }
  clearTimeout(simulatedDictationTimer);

  const target = forceTarget || (appState.route === 'COUNT' ? 'COUNT' : appState.route === 'ADD_PRODUCT' ? 'NEW_PRODUCT' : 'SEARCH');
  Speaker.stop();
  Haptics.play('TAP');
  appState.listenTarget = target;
  appState.listenPrompt = 'Escuchando micrófono…';
  appState.listenPartial = '';
  appState.listening = 'LISTENING';
  appState.micActivity = 0.95;
  renderListeningOverlay();

  const words = phrase.split(' ');
  let idx = 0;

  function step() {
    if (idx < words.length) {
      idx++;
      appState.listenPartial = words.slice(0, idx).join(' ');
      appState.micActivity = 1.0;
      renderListeningOverlay();
      simulatedDictationTimer = setTimeout(step, 140);
    } else {
      simulatedDictationTimer = setTimeout(() => {
        simulatedDictationTimer = null;
        appState.listening = 'IDLE';
        appState.micActivity = 0;
        renderListeningOverlay();
        dispatchVoiceResult([phrase]);
      }, 280);
    }
  }
  simulatedDictationTimer = setTimeout(step, 180);
}

// ==================== 11. RENDERIZADO DEL RELOJ ====================
function renderStatusPill() {
  const dot = document.getElementById('statusDot');
  const label = document.getElementById('statusLabel');
  if (!dot || !label) return;

  const pending = appState.pendingAudits.length;
  if (appState.connection === 'OFFLINE') {
    dot.style.background = 'var(--echo-offline)';
    label.textContent = pending > 0 ? `Sin conexión · ${pending} en cola` : 'Sin conexión';
  } else if (pending > 0) {
    dot.style.background = 'var(--echo-offline)';
    label.textContent = `Enviando ${pending}…`;
  } else {
    dot.style.background = 'var(--echo-match)';
    label.textContent = `En línea · ${appState.operatorCode}`;
  }

  // Botón superior de simulación offline
  const offBtn = document.getElementById('toggleOfflineBtn');
  if (offBtn) {
    offBtn.classList.toggle('offline-active', appState.simulatedOffline);
    offBtn.innerHTML = appState.simulatedOffline
      ? '<span>📡</span> WiFi Almacén: CORTADO (Offline)'
      : '<span>📶</span> WiFi Almacén: En línea';
  }
}

function renderListeningOverlay() {
  const overlay = document.getElementById('listeningOverlay');
  const statusEl = document.getElementById('listeningStatusText');
  const promptEl = document.getElementById('listeningPromptText');
  if (!overlay) return;

  if (appState.listening === 'IDLE') {
    overlay.classList.remove('active');
    return;
  }
  overlay.classList.add('active');
  statusEl.textContent = appState.listening === 'PREPARING' ? 'PREPARANDO…' : 'ESCUCHANDO';
  const hasPartial = Boolean(appState.listenPartial);
  promptEl.textContent = hasPartial ? appState.listenPartial : appState.listenPrompt;
  promptEl.style.color = hasPartial ? 'var(--echo-on-surface)' : 'var(--echo-muted)';
  promptEl.style.fontWeight = hasPartial ? '700' : '400';
}

function renderCountValues(bumpDirection = null) {
  const p = appState.product;
  if (!p) return;
  const numEl = document.getElementById('countNumberValue');
  const diffEl = document.getElementById('countDiffText');
  const submitBtn = document.getElementById('countSubmitBtn');
  const minusBtn = document.getElementById('countMinusBtn');

  const diff = appState.counted - p.systemQuantity;
  const matches = diff === 0;
  const accent = matches ? 'var(--echo-match)' : 'var(--echo-discrepancy)';

  if (numEl) {
    numEl.textContent = String(appState.counted);
    numEl.style.color = accent;
    if (bumpDirection) {
      numEl.classList.remove('bump-up', 'bump-down');
      void numEl.offsetWidth;
      numEl.classList.add(bumpDirection === 'up' ? 'bump-up' : 'bump-down');
    }
  }
  if (diffEl) {
    if (matches) {
      diffEl.textContent = 'Igual al sistema';
      diffEl.style.color = 'var(--echo-match)';
    } else if (diff > 0) {
      diffEl.textContent = `+${diff} sobre ${p.systemQuantity} del sistema`;
      diffEl.style.color = 'var(--echo-muted)';
    } else {
      diffEl.textContent = `−${Math.abs(diff)} bajo ${p.systemQuantity} del sistema`;
      diffEl.style.color = 'var(--echo-muted)';
    }
  }
  if (submitBtn) {
    submitBtn.style.background = accent;
  }
  if (minusBtn) {
    minusBtn.disabled = appState.counted <= 0 || appState.submitting;
  }
}

function renderWatchScreen() {
  // Activar vista actual
  const routes = ['HOME', 'RESULTS', 'PRODUCT', 'COUNT', 'ADD_PRODUCT', 'DONE'];
  for (const r of routes) {
    const el = document.getElementById(`view-${r}`);
    if (el) el.classList.toggle('active', appState.route === r);
  }

  // PositionIndicator solo en RESULTS
  const posInd = document.getElementById('positionIndicator');
  if (posInd) posInd.classList.toggle('visible', appState.route === 'RESULTS');

  // 1. HOME
  if (appState.route === 'HOME') {
    const ring = document.getElementById('homeSearchingRing');
    if (ring) ring.classList.toggle('active', appState.searching);

    const msgEl = document.getElementById('homeMessageText');
    if (msgEl) {
      const m = appState.message;
      let text = 'Dicta producto, “agregar…” o “eliminar…”';
      let color = 'var(--echo-muted)';
      if (m?.type === 'SEARCHING') { text = `Buscando “${m.query}”`; color = 'var(--echo-on-surface)'; }
      else if (m?.type === 'NOT_FOUND') { text = `No encontré “${m.query}”`; color = 'var(--echo-discrepancy)'; }
      else if (m?.type === 'DELETED') { text = `Eliminado: ${m.name}`; color = 'var(--echo-match)'; }
      else if (m?.type === 'THEME') { text = m.isDark ? 'Modo oscuro activado' : 'Modo claro activado'; color = 'var(--echo-match)'; }
      else if (m?.type === 'NOT_UNDERSTOOD') { text = 'No entendí, toca y repite'; color = 'var(--echo-discrepancy)'; }
      else if (m?.type === 'VOICE_UNAVAILABLE') { text = 'Usa los comandos de voz o el catálogo'; color = 'var(--echo-offline)'; }
      msgEl.textContent = text;
      msgEl.style.color = color;
    }

    const themeIcon = document.getElementById('themeToggleIcon');
    if (themeIcon) {
      themeIcon.innerHTML = appState.isDark
        ? '<circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M4.93 4.93l1.41 1.41M17.66 17.66l1.41 1.41M2 12h2M20 12h2M6.34 17.66l-1.41 1.41M19.07 4.93l-1.41 1.41"/>'
        : '<path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/>';
    }
    renderStatusPill();
  }

  // 2. RESULTS
  if (appState.route === 'RESULTS') {
    document.getElementById('resultsHeaderTitle').textContent = appState.resultsTitle;
    document.getElementById('resultsOfflineBanner').style.display = appState.offlineResults ? 'block' : 'none';
    const listEl = document.getElementById('resultsItemsContainer');
    listEl.innerHTML = '';
    if (appState.hits.length === 0) {
      const empty = document.createElement('div');
      empty.style.color = 'var(--echo-muted)';
      empty.style.fontSize = '13px';
      empty.textContent = 'Sin productos';
      listEl.appendChild(empty);
    } else {
      for (const hit of appState.hits) {
        const p = hit.product;
        const btn = document.createElement('button');
        btn.className = 'wear-chip';
        btn.innerHTML = `
          <span class="wear-chip-title">${escapeHtml(p.name)}</span>
          <span class="wear-chip-sub">${escapeHtml(p.locationCode)} · ${escapeHtml(p.sku)} · ${p.systemQuantity} ${escapeHtml(p.unit)}</span>
        `;
        btn.addEventListener('click', () => {
          Haptics.play('TAP');
          openProduct(p, false);
        });
        listEl.appendChild(btn);
      }
    }
    updateScalingLazyColumn();
  }

  // 3. PRODUCT
  if (appState.route === 'PRODUCT' && appState.product) {
    const p = appState.product;
    document.getElementById('productEyebrow').textContent = `${p.locationCode} · ${p.sku}`;
    document.getElementById('productName').textContent = p.name;
    document.getElementById('productSystemQty').textContent = String(p.systemQuantity);
    document.getElementById('productUnit').textContent = p.unit;
    document.getElementById('productNormalSection').style.display = appState.confirmingDelete ? 'none' : 'flex';
    document.getElementById('productDeleteSection').style.display = appState.confirmingDelete ? 'flex' : 'none';
  }

  // 4. COUNT
  if (appState.route === 'COUNT' && appState.product) {
    renderCountValues();
  }

  // 5. ADD_PRODUCT
  if (appState.route === 'ADD_PRODUCT') {
    const d = appState.newProductDraft;
    const hasName = Boolean(d.name && d.name.trim());
    const nameEl = document.getElementById('addProductNameText');
    nameEl.textContent = hasName ? d.name : 'Toca el orbe y dicta el nombre';
    nameEl.style.color = hasName ? 'var(--echo-on-surface)' : 'var(--echo-muted)';
    nameEl.style.fontWeight = hasName ? '600' : '400';
    document.getElementById('addProductMetaText').textContent = `${d.locationCode} · ${d.unit}`;
    document.getElementById('addProductQtyValue').textContent = String(d.quantity);
    document.getElementById('addProductSaveBtn').disabled = !hasName || appState.submitting;
  }

  // 6. DONE
  if (appState.route === 'DONE' && appState.done) {
    const { result, difference, outcomeType, reason, product } = appState.done;
    const doneView = document.getElementById('view-DONE');
    const iconEl = document.getElementById('doneIconSvg');
    const titleEl = document.getElementById('doneTitleText');
    const subEl = document.getElementById('doneSubtitleText');

    if (outcomeType === 'REJECTED') {
      doneView.style.background = 'var(--echo-discrepancy)';
      titleEl.textContent = 'No registrado';
      subEl.textContent = reason || 'Rechazada por el servidor';
      iconEl.innerHTML = '<circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>';
    } else if (outcomeType === 'QUEUED') {
      doneView.style.background = 'var(--echo-offline)';
      titleEl.textContent = 'Guardado en el reloj';
      subEl.textContent = 'Se enviará al recuperar señal';
      iconEl.innerHTML = '<path d="M22.61 16.95A5 5 0 0 0 18 10h-1.26a8 8 0 0 0-7.05-6M5 5a8 8 0 0 0 4 15h9a5 5 0 0 0 1.7-.3"/><line x1="1" y1="1" x2="23" y2="23"/>';
    } else if (result === 'MATCH') {
      doneView.style.background = 'var(--echo-match)';
      titleEl.textContent = 'Confirmado';
      subEl.textContent = product.name;
      iconEl.innerHTML = '<path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/>';
    } else {
      doneView.style.background = 'var(--echo-discrepancy)';
      titleEl.textContent = difference > 0 ? `Sobran ${Math.abs(difference)}` : `Faltan ${Math.abs(difference)}`;
      subEl.textContent = 'Discrepancia enviada a revisión';
      iconEl.innerHTML = '<path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/>';
    }
  }
}

function escapeHtml(str) {
  return String(str || '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

// Efecto ScalingLazyColumn curvo de Wear OS
function updateScalingLazyColumn() {
  const scroller = document.getElementById('resultsScrollContainer');
  const thumb = document.getElementById('positionIndicatorThumb');
  if (!scroller) return;

  const maxScroll = Math.max(1, scroller.scrollHeight - scroller.clientHeight);
  const ratio = Math.max(0, Math.min(1, scroller.scrollTop / maxScroll));
  if (thumb) {
    thumb.style.transform = `translateY(${ratio * 175}%)`;
  }

  const rect = scroller.getBoundingClientRect();
  const centerY = rect.top + rect.height / 2;
  const chips = scroller.querySelectorAll('.wear-chip');
  chips.forEach((chip) => {
    const cr = chip.getBoundingClientRect();
    const chipCenter = cr.top + cr.height / 2;
    const dist = Math.abs(centerY - chipCenter) / (rect.height / 2);
    const scale = Math.max(0.84, 1 - Math.pow(Math.min(1, dist), 2) * 0.16);
    const opacity = Math.max(0.45, 1 - Math.pow(Math.min(1, dist), 2) * 0.5);
    chip.style.transform = `scale(${scale.toFixed(3)})`;
    chip.style.opacity = opacity.toFixed(2);
  });
}

// ==================== 12. PANEL DE SUPERVISIÓN ERP/WMS EN VIVO ====================
async function loadSupervisorAudits() {
  const container = document.getElementById('supervisorAuditsList');
  if (!container) return;
  if (appState.simulatedOffline) {
    container.innerHTML = `<div class="section-card-desc">Modo offline simulado activo (${appState.pendingAudits.length} auditoría(s) guardadas en la cola interna del reloj). Activa el WiFi para sincronizarlas en lote.</div>`;
    return;
  }
  try {
    const resp = await apiFetch('/audits?page=0&size=8');
    if (!resp.ok) throw new Error('HTTP ' + resp.status);
    const data = await resp.json();
    const audits = data.content || [];
    if (audits.length === 0) {
      container.innerHTML = '<div class="section-card-desc">Aún no hay auditorías registradas en el servidor. Dicta un producto en el reloj y confirma o registra una discrepancia.</div>';
      return;
    }
    container.innerHTML = '';
    for (const a of audits) {
      const diffStr = a.difference === 0 ? 'Coincide' : (a.difference > 0 ? `+${a.difference}` : `${a.difference}`);
      const row = document.createElement('div');
      row.className = 'audit-item';
      row.innerHTML = `
        <div class="audit-info">
          <div class="audit-sku">${escapeHtml(a.sku)} · <span style="font-weight:500;color:#94A3B8;">Contado: ${a.countedQuantity} (Sist: ${a.systemQuantity} · ${diffStr})</span></div>
          <div class="audit-meta">Operario ${escapeHtml(a.operatorCode)} · ${new Date(a.recordedAt).toLocaleTimeString('es-ES')}</div>
        </div>
        <div class="audit-actions">
          <span class="audit-badge ${escapeHtml(a.status)}">${escapeHtml(a.status)}</span>
          ${a.status === 'PENDING_REVIEW' ? `
            <button class="mini-btn approve" data-approve="${a.id}" title="Aprobar y actualizar stock en BD">Aprobar</button>
            <button class="mini-btn reject" data-reject="${a.id}" title="Rechazar discrepancia">✕</button>
          ` : ''}
        </div>
      `;
      container.appendChild(row);
    }

    container.querySelectorAll('[data-approve]').forEach((btn) => {
      btn.addEventListener('click', async () => {
        const id = btn.getAttribute('data-approve');
        await apiFetch(`/audits/${id}/approve`, { method: 'POST' });
        await refreshCatalogFromServer();
        await loadSupervisorAudits();
      });
    });
    container.querySelectorAll('[data-reject]').forEach((btn) => {
      btn.addEventListener('click', async () => {
        const id = btn.getAttribute('data-reject');
        await apiFetch(`/audits/${id}/reject`, { method: 'POST' });
        await loadSupervisorAudits();
      });
    });
  } catch (_) {
    container.innerHTML = '<div class="section-card-desc">No se pudo conectar al backend en este momento. El reloj opera en modo autónomo (Offline-First RF-006).</div>';
  }
}

// ==================== 13. INTERACCIONES DE HARDWARE (Hold-to-repeat, Corona, Swipe) ====================
function bindRepeatHoldButton(btn, onStep) {
  if (!btn) return;
  let holdTimeout = null;
  let repeatInterval = null;
  let repeated = false;

  const stop = () => {
    clearTimeout(holdTimeout);
    clearInterval(repeatInterval);
    holdTimeout = null;
    repeatInterval = null;
    btn.classList.remove('pressed');
  };

  btn.addEventListener('pointerdown', (e) => {
    if (btn.disabled) return;
    e.preventDefault();
    btn.classList.add('pressed');
    repeated = false;
    holdTimeout = setTimeout(() => {
      repeatInterval = setInterval(() => {
        if (btn.disabled) { stop(); return; }
        repeated = true;
        onStep();
      }, 70);
    }, 450);
  });

  btn.addEventListener('pointerup', () => {
    const wasRepeated = repeated;
    stop();
    if (!wasRepeated && !btn.disabled) onStep();
  });

  btn.addEventListener('pointerleave', stop);
  btn.addEventListener('pointercancel', stop);
}

function setupRotaryAndSwipe() {
  const screen = document.getElementById('watchScreen');
  const crown = document.getElementById('watchCrown');
  let crownOffset = 0;

  function onRotaryStep(steps) {
    crownOffset += steps * 4;
    if (crown) crown.style.backgroundPosition = `0px ${crownOffset}px`;
    if (appState.route === 'COUNT') {
      adjustCount(steps, true);
    } else if (appState.route === 'ADD_PRODUCT') {
      adjustNewProductQuantity(steps, true);
    } else if (appState.route === 'RESULTS') {
      const scroller = document.getElementById('resultsScrollContainer');
      if (scroller) scroller.scrollBy({ top: steps * 38, behavior: 'smooth' });
    }
  }

  // Rueda del ratón sobre el reloj o la corona giratoria
  const wheelHandler = (e) => {
    if (['COUNT', 'ADD_PRODUCT', 'RESULTS'].includes(appState.route)) {
      e.preventDefault();
      const dir = e.deltaY < 0 ? 1 : -1;
      onRotaryStep(dir);
    }
  };
  if (screen) screen.addEventListener('wheel', wheelHandler, { passive: false });
  if (crown) crown.addEventListener('wheel', wheelHandler, { passive: false });

  // Arrastrar la corona giratoria verticalmente
  if (crown) {
    let draggingCrown = false;
    let lastY = 0;
    crown.addEventListener('pointerdown', (e) => {
      draggingCrown = true;
      lastY = e.clientY;
      crown.setPointerCapture(e.pointerId);
    });
    crown.addEventListener('pointermove', (e) => {
      if (!draggingCrown) return;
      const dy = lastY - e.clientY;
      if (Math.abs(dy) >= 14) {
        const steps = Math.trunc(dy / 14);
        lastY -= steps * 14;
        onRotaryStep(steps);
      }
    });
    crown.addEventListener('pointerup', () => { draggingCrown = false; });
  }

  // Swipe-to-dismiss de izquierda a derecha en la pantalla del reloj (como Wear OS)
  if (screen) {
    let startX = null;
    let startY = null;
    screen.addEventListener('pointerdown', (e) => {
      startX = e.clientX;
      startY = e.clientY;
    });
    screen.addEventListener('pointerup', (e) => {
      if (startX === null || startY === null) return;
      const dx = e.clientX - startX;
      const dy = Math.abs(e.clientY - startY);
      if (dx > 75 && dy < 60 && appState.route !== 'HOME') {
        navigateBack();
      }
      startX = null;
      startY = null;
    });
  }
}

// ==================== 14. RELOJ EN TIEMPO REAL Y ARRANQUE ====================
function updateClock() {
  const el = document.getElementById('wearTimeText');
  if (!el) return;
  const now = new Date();
  const hh = String(now.getHours()).padStart(2, '0');
  const mm = String(now.getMinutes()).padStart(2, '0');
  el.textContent = `${hh}:${mm}`;
}

function setViewMode(mode) {
  appState.viewMode = mode;
  document.body.setAttribute('data-view-mode', mode);
  document.querySelectorAll('[data-set-mode]').forEach((btn) => {
    btn.classList.toggle('active', btn.getAttribute('data-set-mode') === mode);
  });
}

let deferredPwaPrompt = null;
window.addEventListener('beforeinstallprompt', (e) => {
  e.preventDefault();
  deferredPwaPrompt = e;
  const installBtn = document.getElementById('installPwaBtn');
  if (installBtn) installBtn.style.display = 'inline-flex';
});

document.addEventListener('DOMContentLoaded', () => {
  // Registrar Service Worker para instalación PWA y soporte Offline
  if ('serviceWorker' in navigator) {
    navigator.serviceWorker.register('./sw.js').catch(() => {});
  }

  // Inicializar modo de vista y tema
  setViewMode(appState.viewMode);
  document.getElementById('watchScreen').setAttribute('data-theme', appState.isDark ? 'dark' : 'light');

  // Reloj de sistema Wear OS
  updateClock();
  setInterval(updateClock, 10000);

  // Inicializar los 4 Orbes Líquidos (@beui/voice-orb)
  const orbEngine = new VoiceOrbRenderer();
  orbEngine.register(
    document.getElementById('homeOrbCanvas'),
    () => (appState.searching ? 0.65 : 0.16),
    () => !appState.searching
  );
  orbEngine.register(
    document.getElementById('countOrbCanvas'),
    () => 0.18,
    () => !appState.submitting
  );
  orbEngine.register(
    document.getElementById('addOrbCanvas'),
    () => 0.18,
    () => !appState.submitting
  );
  orbEngine.register(
    document.getElementById('listeningOrbCanvas'),
    () => (appState.listening === 'IDLE' ? 0 : appState.micActivity || 0.85),
    () => true
  );

  // Eventos de la pantalla HOME
  document.getElementById('homeOrbBtn').addEventListener('click', () => {
    startListening('SEARCH', 'Producto, “agregar…” o “eliminar…”');
  });
  document.getElementById('themeToggleBtn').addEventListener('click', toggleTheme);
  document.getElementById('browseCatalogBtn').addEventListener('click', browseCatalog);
  document.getElementById('homeAddProductBtn').addEventListener('click', () => openAddProduct(''));

  // Eventos de RESULTS
  document.getElementById('resultsAddProductBtn').addEventListener('click', () => openAddProduct(''));
  document.getElementById('resultsScrollContainer').addEventListener('scroll', updateScalingLazyColumn);

  // Eventos de PRODUCT
  document.getElementById('productRepeatBox').addEventListener('click', repeatProductStatus);
  document.getElementById('productDeleteRequestBtn').addEventListener('click', requestDeleteProduct);
  document.getElementById('productCancelDeleteBtn').addEventListener('click', cancelDeleteProduct);
  document.getElementById('productConfirmDeleteBtn').addEventListener('click', confirmDeleteProduct);
  document.getElementById('productMismatchBtn').addEventListener('click', openCountScreen);
  document.getElementById('productMatchBtn').addEventListener('click', () => {
    if (appState.product) submitAudit(appState.product.systemQuantity);
  });

  // Eventos de COUNT
  bindRepeatHoldButton(document.getElementById('countMinusBtn'), () => adjustCount(-1, true));
  bindRepeatHoldButton(document.getElementById('countPlusBtn'), () => adjustCount(1, true));
  document.getElementById('countDictateBtn').addEventListener('click', () => {
    const u = appState.product?.unit || 'unidades';
    startListening('COUNT', `¿Cuántas ${u} contaste?`);
  });
  document.getElementById('countSubmitBtn').addEventListener('click', () => submitAudit(appState.counted));

  // Eventos de ADD_PRODUCT
  bindRepeatHoldButton(document.getElementById('addMinusBtn'), () => adjustNewProductQuantity(-1, true));
  bindRepeatHoldButton(document.getElementById('addPlusBtn'), () => adjustNewProductQuantity(1, true));
  document.getElementById('addDictateBtn').addEventListener('click', () => {
    startListening('NEW_PRODUCT', 'Nombre y cantidad (ej. Guantes 20)');
  });
  document.getElementById('addProductSaveBtn').addEventListener('click', saveNewProduct);

  // Eventos de DONE y LISTENING
  document.getElementById('view-DONE').addEventListener('click', resetToHome);
  document.getElementById('listeningOverlay').addEventListener('click', () => stopListening(false));
  document.getElementById('watchSideBtn').addEventListener('click', navigateBack);

  // Controles superiores de vista y modo offline (opcionales si existen)
  document.querySelectorAll('[data-set-mode]').forEach((btn) => {
    btn.addEventListener('click', () => setViewMode(btn.getAttribute('data-set-mode')));
  });
  document.getElementById('exitWatchModeBtn')?.addEventListener('click', () => setViewMode('emulator'));

  document.getElementById('toggleOfflineBtn')?.addEventListener('click', async () => {
    appState.simulatedOffline = !appState.simulatedOffline;
    if (appState.simulatedOffline) {
      appState.connection = 'OFFLINE';
      Haptics.play('QUEUED');
      renderStatusPill();
      loadSupervisorAudits();
    } else {
      Haptics.play('CONFIRMED');
      await checkServerHealth();
    }
  });

  // Pestañas del panel de demostración
  document.querySelectorAll('[data-panel-tab]').forEach((tab) => {
    tab.addEventListener('click', () => {
      const target = tab.getAttribute('data-panel-tab');
      document.querySelectorAll('[data-panel-tab]').forEach((t) => t.classList.toggle('active', t === tab));
      document.querySelectorAll('[data-panel-section]').forEach((sec) => {
        sec.classList.toggle('active', sec.getAttribute('data-panel-section') === target);
      });
      if (target === 'supervisor') loadSupervisorAudits();
    });
  });

  // Botones de inyección de voz 1-clic para la demostración
  document.querySelectorAll('[data-voice-phrase]').forEach((btn) => {
    btn.addEventListener('click', () => {
      const phrase = btn.getAttribute('data-voice-phrase');
      const target = btn.getAttribute('data-voice-target') || null;
      simulateVoiceCommand(phrase, target);
    });
  });

  // Configuración de servidor en el panel
  const urlInput = document.getElementById('configApiUrl');
  const keyInput = document.getElementById('configApiKey');
  const opInput = document.getElementById('configOperator');
  if (urlInput) urlInput.value = appState.apiBaseUrl;
  if (keyInput) keyInput.value = appState.apiKey;
  if (opInput) opInput.value = appState.operatorCode;

  document.getElementById('saveConfigBtn')?.addEventListener('click', async () => {
    appState.apiBaseUrl = urlInput.value.trim() || '/api/v1';
    appState.apiKey = keyInput.value.trim() || 'echostock-dev-key';
    appState.operatorCode = opInput.value.trim() || 'OP-07';
    localStorage.setItem('echostock_web_config', JSON.stringify({
      apiBaseUrl: appState.apiBaseUrl,
      apiKey: appState.apiKey,
      operatorCode: appState.operatorCode
    }));
    const ok = await checkServerHealth();
    const statusMsg = document.getElementById('configSaveStatus');
    if (statusMsg) {
      statusMsg.textContent = ok ? '✓ Conectado al servidor correctamente' : '⚠ Sin respuesta del servidor (operando en modo Offline)';
      statusMsg.style.color = ok ? '#2EE07E' : '#FFB627';
    }
  });

  document.getElementById('refreshAuditsBtn')?.addEventListener('click', loadSupervisorAudits);

  document.getElementById('installPwaBtn')?.addEventListener('click', async () => {
    if (deferredPwaPrompt) {
      deferredPwaPrompt.prompt();
      await deferredPwaPrompt.userChoice;
      deferredPwaPrompt = null;
    } else {
      setViewMode('watch');
    }
  });

  setupRotaryAndSwipe();
  renderWatchScreen();
  checkServerHealth();
  setInterval(checkServerHealth, 15000);
});
