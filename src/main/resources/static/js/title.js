export function createTextType({
  targetSelector,
  text = 'Escribiendo...',
  typingSpeed = 50,
  initialDelay = 0,
  pauseDuration = 2000,
  deletingSpeed = 30,
  loop = true,
  showCursor = true,
  hideCursorWhileTyping = false,
  cursorCharacter = '|',
  cursorBlinkDuration = 500,
  textColors = [],
  variableSpeed,
  reverseMode = false
}) {
  const container = document.querySelector(targetSelector);
  if (!container) {
    console.warn(`Elemento no encontrado: ${targetSelector}`);
    return;
  }

  const textArray = Array.isArray(text) ? text : [text];
  let currentTextIndex = 0;
  let currentCharIndex = 0;
  let isDeleting = false;
  let displayedText = '';

  const contentSpan = document.createElement('span');
  contentSpan.className = 'text-type__content';
  container.innerHTML = '';
  container.appendChild(contentSpan);

  let cursorSpan;
  if (showCursor) {
    cursorSpan = document.createElement('span');
    cursorSpan.className = 'text-type__cursor';
    cursorSpan.textContent = cursorCharacter;
    container.appendChild(cursorSpan);

    setInterval(() => {
      cursorSpan.style.opacity = cursorSpan.style.opacity === '0' ? '1' : '0';
    }, cursorBlinkDuration);
  }

  const getRandomSpeed = () => {
    if (!variableSpeed) return typingSpeed;
    const { min, max } = variableSpeed;
    return Math.random() * (max - min) + min;
  };

  const getCurrentTextColor = () => {
    if (textColors.length === 0) return '#000';
    return textColors[currentTextIndex % textColors.length];
  };

  const typeLoop = () => {
    const currentText = textArray[currentTextIndex];
    const processedText = reverseMode ? currentText.split('').reverse().join('') : currentText;

    if (isDeleting) {
      if (displayedText === '') {
        isDeleting = false;
        currentTextIndex = (currentTextIndex + 1) % textArray.length;
        currentCharIndex = 0;
        setTimeout(typeLoop, pauseDuration);
        return;
      } else {
        displayedText = displayedText.slice(0, -1);
        contentSpan.textContent = displayedText;
        setTimeout(typeLoop, deletingSpeed);
        return;
      }
    }

    if (currentCharIndex < processedText.length) {
      displayedText += processedText[currentCharIndex];
      contentSpan.textContent = displayedText;
      contentSpan.style.color = getCurrentTextColor();
      currentCharIndex++;
      if (hideCursorWhileTyping && cursorSpan) {
        cursorSpan.style.display = 'none';
      }
      setTimeout(typeLoop, variableSpeed ? getRandomSpeed() : typingSpeed);
    } else {
      if (loop || currentTextIndex < textArray.length - 1) {
        isDeleting = true;
        if (cursorSpan) cursorSpan.style.display = '';
        setTimeout(typeLoop, pauseDuration);
      }
    }
  };

  setTimeout(typeLoop, initialDelay);
}
