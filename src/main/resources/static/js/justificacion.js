document.addEventListener('DOMContentLoaded', () => {
  const el = document.querySelector('#scrambled-text p');
  if (!el) return;

  const originalText = el.textContent;
  const scrambleChars = '.:;|*#@%$&';
  const scrambleSpeed = 80;       // milisegundos por ciclo
  const scrambleDuration = 1200;  // duración total del efecto

  function scrambleText(element, duration = scrambleDuration) {
    const original = element.textContent;
    const length = original.length;
    let frame = 0;
    const totalFrames = Math.floor(duration / scrambleSpeed);

    const interval = setInterval(() => {
      let scrambled = '';
      for (let i = 0; i < length; i++) {
        if (i < (frame * length) / totalFrames) {
          scrambled += original[i];
        } else {
          scrambled += scrambleChars[Math.floor(Math.random() * scrambleChars.length)];
        }
      }
      element.textContent = scrambled;
      frame++;

      if (frame >= totalFrames) {
        clearInterval(interval);
        element.textContent = original;
      }
    }, scrambleSpeed);
  }

  el.addEventListener('pointerenter', () => {
    scrambleText(el);
  });
});
