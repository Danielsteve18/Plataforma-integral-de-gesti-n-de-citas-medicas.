/**
 * LetterGlitch - Efecto de letras glitch en JavaScript puro
 * Convertido desde React para uso en proyectos Spring Boot
 */

export function createLetterGlitch({
  selector = '.letter-glitch',
  glitchColors = ['#2b4539', '#61dca3', '#61b3dc'],
  glitchSpeed = 50,
  centerVignette = false,
  outerVignette = true,
  smooth = true,
  characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ!@#$&*()-_+=/[]{};:<>.,0123456789'
} = {}) {

  const containers = document.querySelectorAll(selector);
  if (!containers.length) {
    console.warn('LetterGlitch: No se encontraron elementos con el selector:', selector);
    return [];
  }

  const instances = Array.from(containers).map(container => {
    return new LetterGlitchInstance(container, {
      glitchColors,
      glitchSpeed,
      centerVignette,
      outerVignette,
      smooth,
      characters
    });
  });

  return instances;
}

class LetterGlitchInstance {
  constructor(container, options) {
    this.container = container;
    this.options = options;
    this.canvas = null;
    this.context = null;
    this.animationId = null;
    this.letters = [];
    this.grid = { columns: 0, rows: 0 };
    this.lastGlitchTime = Date.now();
    this.resizeTimeout = null;

    this.fontSize = 16;
    this.charWidth = 10;
    this.charHeight = 20;
    this.lettersAndSymbols = Array.from(options.characters);

    this.init();
  }

  init() {
    this.setupContainer();
    this.createCanvas();
    this.setupContext();
    this.resizeCanvas();
    this.createVignettes();
    this.bindEvents();
    this.animate();
  }

  setupContainer() {
    this.container.style.position = 'relative';
    this.container.style.width = '100%';
    this.container.style.height = '100%';
    this.container.style.backgroundColor = '#000000';
    this.container.style.overflow = 'hidden';
  }

  createCanvas() {
    this.canvas = document.createElement('canvas');
    this.canvas.style.display = 'block';
    this.canvas.style.width = '100%';
    this.canvas.style.height = '100%';
    this.container.appendChild(this.canvas);
  }

  setupContext() {
    this.context = this.canvas.getContext('2d');
  }

  createVignettes() {
    // Outer vignette
    if (this.options.outerVignette) {
      const outerVignette = document.createElement('div');
      outerVignette.style.position = 'absolute';
      outerVignette.style.top = '0';
      outerVignette.style.left = '0';
      outerVignette.style.width = '100%';
      outerVignette.style.height = '100%';
      outerVignette.style.pointerEvents = 'none';
      outerVignette.style.background = 'radial-gradient(circle, rgba(0,0,0,0) 60%, rgba(0,0,0,1) 100%)';
      this.container.appendChild(outerVignette);
    }

    // Center vignette
    if (this.options.centerVignette) {
      const centerVignette = document.createElement('div');
      centerVignette.style.position = 'absolute';
      centerVignette.style.top = '0';
      centerVignette.style.left = '0';
      centerVignette.style.width = '100%';
      centerVignette.style.height = '100%';
      centerVignette.style.pointerEvents = 'none';
      centerVignette.style.background = 'radial-gradient(circle, rgba(0,0,0,0.8) 0%, rgba(0,0,0,0) 60%)';
      this.container.appendChild(centerVignette);
    }
  }

  getRandomChar() {
    return this.lettersAndSymbols[Math.floor(Math.random() * this.lettersAndSymbols.length)];
  }

  getRandomColor() {
    return this.options.glitchColors[Math.floor(Math.random() * this.options.glitchColors.length)];
  }

  hexToRgb(hex) {
    const shorthandRegex = /^#?([a-f\d])([a-f\d])([a-f\d])$/i;
    hex = hex.replace(shorthandRegex, (m, r, g, b) => {
      return r + r + g + g + b + b;
    });

    const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
    return result ? {
      r: parseInt(result[1], 16),
      g: parseInt(result[2], 16),
      b: parseInt(result[3], 16)
    } : null;
  }

  interpolateColor(start, end, factor) {
    const result = {
      r: Math.round(start.r + (end.r - start.r) * factor),
      g: Math.round(start.g + (end.g - start.g) * factor),
      b: Math.round(start.b + (end.b - start.b) * factor)
    };
    return `rgb(${result.r}, ${result.g}, ${result.b})`;
  }

  calculateGrid(width, height) {
    const columns = Math.ceil(width / this.charWidth);
    const rows = Math.ceil(height / this.charHeight);
    return { columns, rows };
  }

  initializeLetters(columns, rows) {
    this.grid = { columns, rows };
    const totalLetters = columns * rows;
    this.letters = Array.from({ length: totalLetters }, () => ({
      char: this.getRandomChar(),
      color: this.getRandomColor(),
      targetColor: this.getRandomColor(),
      colorProgress: 1
    }));
  }

  resizeCanvas() {
    if (!this.canvas) return;

    const dpr = window.devicePixelRatio || 1;
    const rect = this.container.getBoundingClientRect();

    this.canvas.width = rect.width * dpr;
    this.canvas.height = rect.height * dpr;

    this.canvas.style.width = `${rect.width}px`;
    this.canvas.style.height = `${rect.height}px`;

    if (this.context) {
      this.context.setTransform(dpr, 0, 0, dpr, 0, 0);
    }

    const { columns, rows } = this.calculateGrid(rect.width, rect.height);
    this.initializeLetters(columns, rows);
    this.drawLetters();
  }

  drawLetters() {
    if (!this.context || this.letters.length === 0) return;

    const { width, height } = this.canvas.getBoundingClientRect();
    this.context.clearRect(0, 0, width, height);
    this.context.font = `${this.fontSize}px monospace`;
    this.context.textBaseline = 'top';

    this.letters.forEach((letter, index) => {
      const x = (index % this.grid.columns) * this.charWidth;
      const y = Math.floor(index / this.grid.columns) * this.charHeight;
      this.context.fillStyle = letter.color;
      this.context.fillText(letter.char, x, y);
    });
  }

  updateLetters() {
    if (!this.letters || this.letters.length === 0) return;

    const updateCount = Math.max(1, Math.floor(this.letters.length * 0.05));

    for (let i = 0; i < updateCount; i++) {
      const index = Math.floor(Math.random() * this.letters.length);
      if (!this.letters[index]) continue;

      this.letters[index].char = this.getRandomChar();
      this.letters[index].targetColor = this.getRandomColor();

      if (!this.options.smooth) {
        this.letters[index].color = this.letters[index].targetColor;
        this.letters[index].colorProgress = 1;
      } else {
        this.letters[index].colorProgress = 0;
      }
    }
  }

  handleSmoothTransitions() {
    let needsRedraw = false;
    this.letters.forEach(letter => {
      if (letter.colorProgress < 1) {
        letter.colorProgress += 0.05;
        if (letter.colorProgress > 1) letter.colorProgress = 1;

        const startRgb = this.hexToRgb(letter.color);
        const endRgb = this.hexToRgb(letter.targetColor);
        if (startRgb && endRgb) {
          letter.color = this.interpolateColor(startRgb, endRgb, letter.colorProgress);
          needsRedraw = true;
        }
      }
    });

    if (needsRedraw) {
      this.drawLetters();
    }
  }

  animate() {
    const now = Date.now();
    if (now - this.lastGlitchTime >= this.options.glitchSpeed) {
      this.updateLetters();
      this.drawLetters();
      this.lastGlitchTime = now;
    }

    if (this.options.smooth) {
      this.handleSmoothTransitions();
    }

    this.animationId = requestAnimationFrame(() => this.animate());
  }

  bindEvents() {
    const handleResize = () => {
      clearTimeout(this.resizeTimeout);
      this.resizeTimeout = setTimeout(() => {
        cancelAnimationFrame(this.animationId);
        this.resizeCanvas();
        this.animate();
      }, 100);
    };

    window.addEventListener('resize', handleResize);

    // Guardar referencia para cleanup
    this.handleResize = handleResize;
  }

  destroy() {
    if (this.animationId) {
      cancelAnimationFrame(this.animationId);
    }
    if (this.handleResize) {
      window.removeEventListener('resize', this.handleResize);
    }
    if (this.canvas && this.canvas.parentNode) {
      this.canvas.parentNode.removeChild(this.canvas);
    }
  }
}