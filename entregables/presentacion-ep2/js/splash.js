(() => {
  const splash = document.querySelector("[data-splash]");
  if (!splash) return;
  const params = new URLSearchParams(location.search);
  const force = params.has("splash");
  const capture = document.documentElement.classList.contains("is-capture");
  if (capture || (navigator.webdriver && !force)) {
    splash.remove();
    return;
  }
  const reduced = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  let done = false;

  function replayActive() {
    const slide = document.querySelector(".slide.is-active");
    if (!slide) return;
    slide.querySelectorAll(".anim, .arch-chain > *, .arch-grid > *, .arch-under > *, .arch-note, .sg-bar, .arch-flow, .proof-packet, .proof-hop.is-fall .mark, .route-packet, .route-card").forEach((el) => {
      el.style.animation = "none";
      void el.offsetWidth;
      el.style.animation = "";
    });
  }

  function dismiss() {
    if (done) return;
    done = true;
    window.removeEventListener("keydown", onKey, true);
    splash.classList.add("is-out");
    replayActive();
    window.setTimeout(() => splash.remove(), 700);
  }

  function onKey(event) {
    event.preventDefault();
    event.stopImmediatePropagation();
    dismiss();
  }

  window.addEventListener("keydown", onKey, true);
  splash.addEventListener("click", (event) => {
    event.stopPropagation();
    dismiss();
  });

  let started = false;
  function run() {
    if (started) return;
    started = true;
    requestAnimationFrame(() => splash.classList.add("is-run"));
    window.setTimeout(dismiss, reduced ? 800 : 3400);
  }

  const logo = splash.querySelector(".splash-logo img");
  function whenPainted() {
    if (document.readyState === "complete") run();
    else window.addEventListener("load", run, { once: true });
  }
  if (logo && !logo.complete) {
    logo.addEventListener("load", whenPainted, { once: true });
    logo.addEventListener("error", whenPainted, { once: true });
  } else {
    whenPainted();
  }
  window.setTimeout(run, 4000);
})();
