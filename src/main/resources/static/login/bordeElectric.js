export function createElectricBorder({
  selector = '.electric-border',
  color = '#5227FF',
  speed = 1,
  chaos = 1,
  thickness = 2
}) {
  const root = document.querySelector(selector);
  if (!root) return;

  const filterId = `turbulent-displace-${Math.random().toString(36).slice(2)}`;

  root.style.setProperty('--electric-border-color', color);
  root.style.setProperty('--eb-border-width', `${thickness}px`);

  const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
  svg.classList.add('eb-svg');
  svg.setAttribute('aria-hidden', 'true');
  svg.setAttribute('focusable', 'false');

  svg.innerHTML = `
    <defs>
      <filter id="${filterId}" color-interpolation-filters="sRGB" x="-200%" y="-200%" width="500%" height="500%">
        <feTurbulence type="turbulence" baseFrequency="0.02" numOctaves="10" result="noise1" seed="1" />
        <feOffset in="noise1" dx="0" dy="0" result="offsetNoise1">
          <animate attributeName="dy" values="700; 0" dur="6s" repeatCount="indefinite" calcMode="linear" />
        </feOffset>
        <feTurbulence type="turbulence" baseFrequency="0.02" numOctaves="10" result="noise2" seed="1" />
        <feOffset in="noise2" dx="0" dy="0" result="offsetNoise2">
          <animate attributeName="dy" values="0; -700" dur="6s" repeatCount="indefinite" calcMode="linear" />
        </feOffset>
        <feTurbulence type="turbulence" baseFrequency="0.02" numOctaves="10" result="noise3" seed="2" />
        <feOffset in="noise3" dx="0" dy="0" result="offsetNoise3">
          <animate attributeName="dx" values="490; 0" dur="6s" repeatCount="indefinite" calcMode="linear" />
        </feOffset>
        <feTurbulence type="turbulence" baseFrequency="0.02" numOctaves="10" result="noise4" seed="2" />
        <feOffset in="noise4" dx="0" dy="0" result="offsetNoise4">
          <animate attributeName="dx" values="0; -490" dur="6s" repeatCount="indefinite" calcMode="linear" />
        </feOffset>
        <feComposite in="offsetNoise1" in2="offsetNoise2" result="part1" />
        <feComposite in="offsetNoise3" in2="offsetNoise4" result="part2" />
        <feBlend in="part1" in2="part2" mode="color-dodge" result="combinedNoise" />
        <feDisplacementMap in="SourceGraphic" in2="combinedNoise" scale="${30 * chaos}" xChannelSelector="R" yChannelSelector="B" />
      </filter>
    </defs>
  `;
  document.body.appendChild(svg);

  const layers = document.createElement('div');
  layers.className = 'eb-layers';
  layers.innerHTML = `
    <div class="eb-stroke" style="filter: url(#${filterId})"></div>
    <div class="eb-glow-1"></div>
    <div class="eb-glow-2"></div>
    <div class="eb-background-glow"></div>
  `;
  root.appendChild(layers);
}
