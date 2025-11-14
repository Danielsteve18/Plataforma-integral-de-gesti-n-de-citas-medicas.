/**
 * FuzzyText - Efecto de texto glitchy/fuzzy en JavaScript puro
 * Convertido desde React para uso directo en HTML
 */

class FuzzyText {
  constructor(options = {}) {
    this.canvas = options.canvas;
    this.text = options.text || '';
    this.fontSize = options.fontSize || 'clamp(2rem, 10vw, 10rem)';
    this.fontWeight = options.fontWeight || 900;
    this.fontFamily = options.fontFamily || 'inherit';
    this.color = options.color || '#fff';
    this.enableHover = options.enableHover !== false;
    this.baseIntensity = options.baseIntensity || 0.18;
    this.hoverIntensity = options.hoverIntensity || 0.5;

    this.animationFrameId = null;
    this.isCancelled = false;
    this.isHovering = false;
    this.fuzzRange = 30;

    this.init();
  }

  async init() {
    if (document.fonts?.ready) {
      await document.fonts.ready;
    }
    if (this.isCancelled) return;

    const ctx = this.canvas.getContext('2d');
    if (!ctx) return;

    const computedFontFamily = this.fontFamily === 'inherit' 
      ? window.getComputedStyle(this.canvas).fontFamily || 'sans-serif' 
      : this.fontFamily;

    const fontSizeStr = typeof this.fontSize === 'number' ? `${this.fontSize}px` : this.fontSize;
    let numericFontSize;
    
    if (typeof this.fontSize === 'number') {
      numericFontSize = this.fontSize;
    } else {
      const temp = document.createElement('span');
      temp.style.fontSize = this.fontSize;
      temp.style.visibility = 'hidden';
      temp.style.position = 'absolute';
      document.body.appendChild(temp);
      const computedSize = window.getComputedStyle(temp).fontSize;
      numericFontSize = parseFloat(computedSize);
      document.body.removeChild(temp);
    }

    // Crear canvas offscreen para medir el texto
    const offscreen = document.createElement('canvas');
    const offCtx = offscreen.getContext('2d');
    if (!offCtx) return;

    offCtx.font = `${this.fontWeight} ${fontSizeStr} ${computedFontFamily}`;
    offCtx.textBaseline = 'alphabetic';
    const metrics = offCtx.measureText(this.text);

    const actualLeft = metrics.actualBoundingBoxLeft ?? 0;
    const actualRight = metrics.actualBoundingBoxRight ?? metrics.width;
    const actualAscent = metrics.actualBoundingBoxAscent ?? numericFontSize;
    const actualDescent = metrics.actualBoundingBoxDescent ?? numericFontSize * 0.2;

    const textBoundingWidth = Math.ceil(actualLeft + actualRight);
    const tightHeight = Math.ceil(actualAscent + actualDescent);

    const extraWidthBuffer = 10;
    const offscreenWidth = textBoundingWidth + extraWidthBuffer;

    offscreen.width = offscreenWidth;
    offscreen.height = tightHeight;

    const xOffset = extraWidthBuffer / 2;
    offCtx.font = `${this.fontWeight} ${fontSizeStr} ${computedFontFamily}`;
    offCtx.textBaseline = 'alphabetic';
    offCtx.fillStyle = this.color;
    offCtx.fillText(this.text, xOffset - actualLeft, actualAscent);

    const horizontalMargin = 50;
    const verticalMargin = 0;
    this.canvas.width = offscreenWidth + horizontalMargin * 2;
    this.canvas.height = tightHeight + verticalMargin * 2;
    ctx.translate(horizontalMargin, verticalMargin);

    // Área interactiva para hover
    const interactiveLeft = horizontalMargin + xOffset;
    const interactiveTop = verticalMargin;
    const interactiveRight = interactiveLeft + textBoundingWidth;
    const interactiveBottom = interactiveTop + tightHeight;

    // Guardar referencias para el canvas principal
    this.ctx = ctx;
    this.offscreen = offscreen;
    this.offscreenWidth = offscreenWidth;
    this.tightHeight = tightHeight;
    this.interactiveArea = { interactiveLeft, interactiveTop, interactiveRight, interactiveBottom };

    this.setupEventListeners();
    this.run();
  }

  run() {
    if (this.isCancelled) return;
    
    this.ctx.clearRect(-this.fuzzRange, -this.fuzzRange, 
      this.offscreenWidth + 2 * this.fuzzRange, 
      this.tightHeight + 2 * this.fuzzRange);
    
    const intensity = this.isHovering ? this.hoverIntensity : this.baseIntensity;
    
    for (let j = 0; j < this.tightHeight; j++) {
      const dx = Math.floor(intensity * (Math.random() - 0.5) * this.fuzzRange);
      this.ctx.drawImage(this.offscreen, 0, j, this.offscreenWidth, 1, dx, j, this.offscreenWidth, 1);
    }
    
    this.animationFrameId = window.requestAnimationFrame(() => this.run());
  }

  isInsideTextArea(x, y) {
    const { interactiveLeft, interactiveTop, interactiveRight, interactiveBottom } = this.interactiveArea;
    return x >= interactiveLeft && x <= interactiveRight && y >= interactiveTop && y <= interactiveBottom;
  }

  setupEventListeners() {
    if (!this.enableHover) return;

    this.handleMouseMove = (e) => {
      const rect = this.canvas.getBoundingClientRect();
      const x = e.clientX - rect.left;
      const y = e.clientY - rect.top;
      this.isHovering = this.isInsideTextArea(x, y);
    };

    this.handleMouseLeave = () => {
      this.isHovering = false;
    };

    this.handleTouchMove = (e) => {
      e.preventDefault();
      const rect = this.canvas.getBoundingClientRect();
      const touch = e.touches[0];
      const x = touch.clientX - rect.left;
      const y = touch.clientY - rect.top;
      this.isHovering = this.isInsideTextArea(x, y);
    };

    this.handleTouchEnd = () => {
      this.isHovering = false;
    };

    this.canvas.addEventListener('mousemove', this.handleMouseMove);
    this.canvas.addEventListener('mouseleave', this.handleMouseLeave);
    this.canvas.addEventListener('touchmove', this.handleTouchMove, { passive: false });
    this.canvas.addEventListener('touchend', this.handleTouchEnd);
  }

  destroy() {
    this.isCancelled = true;
    if (this.animationFrameId) {
      window.cancelAnimationFrame(this.animationFrameId);
    }
    
    if (this.enableHover && this.canvas) {
      this.canvas.removeEventListener('mousemove', this.handleMouseMove);
      this.canvas.removeEventListener('mouseleave', this.handleMouseLeave);
      this.canvas.removeEventListener('touchmove', this.handleTouchMove);
      this.canvas.removeEventListener('touchend', this.handleTouchEnd);
    }
  }
}

// Función helper para crear FuzzyText fácilmente
export function createFuzzyText(options) {
  return new FuzzyText(options);
}