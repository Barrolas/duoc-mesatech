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
    slide.querySelectorAll(".anim").forEach((el) => {
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
    window.setTimeout(dismiss, reduced ? 700 : 2400);
  }
  if (document.readyState === "complete") run();
  else window.addEventListener("load", run, { once: true });
  window.setTimeout(run, 3000);
})();
