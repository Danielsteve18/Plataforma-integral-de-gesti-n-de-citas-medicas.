
export function createLogoLoop({
  containerSelector = '.logoloop',
  logos = [],
  speed = 120,
  direction = 'left',
  logoHeight = 108,
  gap = 32,
  pauseOnHover = true,
  fadeOut = false,
  scaleOnHover = false
}) {
  const container = document.querySelector(containerSelector);
  if (!container || logos.length === 0) return;

  container.classList.add('logoloop');
  if (fadeOut) container.classList.add('logoloop--fade');
  if (scaleOnHover) container.classList.add('logoloop--scale-hover');

  container.style.setProperty('--logoloop-gap', `${gap}px`);
  container.style.setProperty('--logoloop-logoHeight', `${logoHeight}px`);

  const track = document.createElement('div');
  track.className = 'logoloop__track';
  container.appendChild(track);

  const createLogoList = () => {
    const ul = document.createElement('ul');
    ul.className = 'logoloop__list';
    logos.forEach(logo => {
      const li = document.createElement('li');
      li.className = 'logoloop__item';
      const img = document.createElement('img');
      img.src = logo.src;
      img.alt = logo.alt || '';
      img.title = logo.title || '';
      img.loading = 'lazy';
      img.draggable = false;
      li.appendChild(img);
      ul.appendChild(li);
    });
    return ul;
  };

  const firstList = createLogoList();
  track.appendChild(firstList);

  const seqWidth = firstList.getBoundingClientRect().width;
  const containerWidth = container.clientWidth;
  const copiesNeeded = Math.ceil(containerWidth / seqWidth) + 2;

  for (let i = 1; i < copiesNeeded; i++) {
    track.appendChild(createLogoList());
  }

  let offset = 0;
  let velocity = 0;
  let isHovered = false;
  const targetVelocity = speed * (direction === 'left' ? 1 : -1);

  const animate = () => {
    const delta = 16 / 1000;
    const easing = 1 - Math.exp(-delta / 0.25);
    const target = pauseOnHover && isHovered ? 0 : targetVelocity;
    velocity += (target - velocity) * easing;
    offset = (offset + velocity * delta) % seqWidth;
    track.style.transform = `translate3d(${-offset}px, 0, 0)`;
    requestAnimationFrame(animate);
  };

  container.addEventListener('mouseenter', () => {
    if (pauseOnHover) isHovered = true;
  });

  container.addEventListener('mouseleave', () => {
    if (pauseOnHover) isHovered = false;
  });

  requestAnimationFrame(animate);
}
