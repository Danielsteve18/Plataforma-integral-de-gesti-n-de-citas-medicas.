function initFallingText() {
  const container = document.getElementById('falling-text-container');
  const textTarget = document.getElementById('falling-text-target');
  const canvasContainer = document.getElementById('falling-text-canvas');

  const { Engine, Render, World, Bodies, Runner, Body } = Matter;

  const words = textTarget.innerText.split(' ');
  textTarget.innerHTML = words.map(word => `<span class="word">${word}</span>`).join(' ');

  const wordSpans = textTarget.querySelectorAll('.word');
  const containerRect = container.getBoundingClientRect();

  const engine = Engine.create();
  engine.world.gravity.y = 0.6;

  const render = Render.create({
    element: canvasContainer,
    engine,
    options: {
      width: containerRect.width,
      height: containerRect.height,
      background: 'transparent',
      wireframes: false
    }
  });

  const floor = Bodies.rectangle(containerRect.width / 2, containerRect.height + 25, containerRect.width, 50, { isStatic: true });
  const walls = [
    Bodies.rectangle(-25, containerRect.height / 2, 50, containerRect.height, { isStatic: true }),
    Bodies.rectangle(containerRect.width + 25, containerRect.height / 2, 50, containerRect.height, { isStatic: true }),
    Bodies.rectangle(containerRect.width / 2, -25, containerRect.width, 50, { isStatic: true })
  ];

  const wordBodies = Array.from(wordSpans).map(span => {
    const rect = span.getBoundingClientRect();
    const x = rect.left - containerRect.left + rect.width / 2;
    const y = rect.top - containerRect.top + rect.height / 2;
    const body = Bodies.rectangle(x, y, rect.width, rect.height, {
      restitution: 0.8,
      frictionAir: 0.01
    });
    Body.setVelocity(body, { x: (Math.random() - 0.5) * 5, y: 0 });
    Body.setAngularVelocity(body, (Math.random() - 0.5) * 0.05);
    return { span, body };
  });

  World.add(engine.world, [floor, ...walls, ...wordBodies.map(wb => wb.body)]);

  const runner = Runner.create();
  Runner.run(runner, engine);
  Render.run(render);

  const updateLoop = () => {
    wordBodies.forEach(({ body, span }) => {
      span.style.left = `${body.position.x}px`;
      span.style.top = `${body.position.y}px`;
      span.style.transform = `translate(-50%, -50%) rotate(${body.angle}rad)`;
    });
    requestAnimationFrame(updateLoop);
  };
  updateLoop();
}

document.addEventListener('DOMContentLoaded', () => {
  const trigger = document.getElementById('falling-text-target');
  if (trigger) {
    trigger.addEventListener('click', initFallingText);
  } else {
    console.warn("Elemento #falling-text-target no encontrado");
  }
});
