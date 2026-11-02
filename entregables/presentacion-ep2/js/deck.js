const slides = [...document.querySelectorAll(".slide")];
const progress = document.querySelector(".progress > span");
const counter = document.querySelector("[data-counter]");
const notesEl = document.querySelector(".speaker");
const notesSay = document.querySelector("[data-note-say]");
const notesHint = document.querySelector("[data-note-hint]");
const notesTime = document.querySelector("[data-note-time]");
const notesHintWrap = document.querySelector(".note-hint");
const chrome = document.querySelector(".chrome");
const sectionLabel = document.querySelector("[data-section-label]");
const dotsWrap = document.querySelector("[data-dots]");
const notesBtn = document.querySelector("[data-notes-toggle]");
const stage = document.querySelector(".stage");
const wrap = document.querySelector(".stage-wrap");
const lightbox = document.querySelector("[data-lightbox]");
const lightboxImg = lightbox.querySelector("img");
const lightboxCap = lightbox.querySelector("p");
let index = 0;
let leaving = false;

slides.forEach((_, i) => {
  const dot = document.createElement("button");
  dot.type = "button";
  dot.setAttribute("aria-label", `Ir a la lámina ${i + 1}`);
  dot.addEventListener("click", (event) => {
    event.stopPropagation();
    go(i);
  });
  dotsWrap.append(dot);
});

const dots = [...dotsWrap.querySelectorAll("button")];

function clamp(n) {
  return Math.max(0, Math.min(slides.length - 1, n));
}

function lightboxOpen() {
  return lightbox.classList.contains("is-on");
}

function closeLightbox() {
  lightbox.classList.remove("is-on");
  lightbox.hidden = true;
  lightboxImg.removeAttribute("src");
}

function shotChrome(shot) {
  const cap = shot.querySelector("figcaption");
  const bar = shot.querySelector(".frame-bar");
  const capHidden = !cap || getComputedStyle(cap).display === "none";
  const capH = capHidden ? 0 : Math.min(cap.offsetHeight, 56);
  const barH = bar ? bar.offsetHeight : 0;
  const gap = parseFloat(getComputedStyle(shot).rowGap || getComputedStyle(shot).gap) || 8;
  return barH + capH + gap + 2;
}

function clearShot(img) {
  img.style.width = "";
  img.style.height = "";
  img.style.maxWidth = "";
  img.style.maxHeight = "";
  const card = img.closest(".shot-card");
  const zoom = img.closest(".shot-zoom");
  if (card) {
    card.style.width = "";
    card.style.maxWidth = "";
  }
  if (zoom) {
    zoom.style.width = "";
    zoom.style.maxWidth = "";
  }
}

function slideBounds(slide) {
  const box = slide.getBoundingClientRect();
  const cs = getComputedStyle(slide);
  const padR = parseFloat(cs.paddingRight);
  const padB = parseFloat(cs.paddingBottom);
  const padL = parseFloat(cs.paddingLeft);
  return {
    right: box.right - padR,
    floor: box.bottom - padB,
    width: Math.max(1, slide.clientWidth - padL - padR),
  };
}

function sizeShot(img, maxW, maxH) {
  const card = img.closest(".shot-card");
  const zoom = img.closest(".shot-zoom");
  const scale = Math.min(1, maxW / img.naturalWidth, maxH / img.naturalHeight);
  if (!Number.isFinite(scale) || scale <= 0) return;
  const w = Math.max(1, Math.round(img.naturalWidth * scale));
  const h = Math.max(1, Math.round(img.naturalHeight * scale));
  img.style.width = `${w}px`;
  img.style.height = `${h}px`;
  img.style.maxWidth = "100%";
  img.style.maxHeight = `${h}px`;
  if (zoom) {
    zoom.style.width = "100%";
    zoom.style.maxWidth = "100%";
  }
  if (card) {
    card.style.width = "100%";
    card.style.maxWidth = "100%";
  }
}

function fitDeploy(deploy, bounds) {
  const shots = [...deploy.querySelectorAll(":scope > .shot")];
  const imgs = shots.map((shot) => shot.querySelector(".shot-zoom img"));
  if (!imgs.length || imgs.some((img) => !img?.naturalWidth)) return;

  const gap = parseFloat(getComputedStyle(deploy).rowGap || getComputedStyle(deploy).gap) || 16;
  const extras = shots.reduce((sum, shot) => sum + shotChrome(shot), 0);
  const availH = Math.max(40, bounds.floor - deploy.getBoundingClientRect().top - extras - gap * (shots.length - 1) - 4);
  const roomR = bounds.right - deploy.getBoundingClientRect().left;
  const availW = Math.max(1, Math.min(deploy.clientWidth, roomR));
  const wanted = imgs.map((img) => img.naturalHeight * Math.min(1, availW / img.naturalWidth));
  const total = wanted.reduce((sum, n) => sum + n, 0);
  const vScale = total > availH ? availH / total : 1;

  imgs.forEach((img, i) => {
    sizeShot(img, availW, Math.max(24, wanted[i] * vScale));
  });
}

function fitShots() {
  const slide = document.querySelector(".slide.is-active");
  if (!slide) return;
  const bounds = slideBounds(slide);

  slide.querySelectorAll(".shot-zoom img").forEach(clearShot);

  const deploy = slide.querySelector(".deploy-shots");
  if (deploy) fitDeploy(deploy, bounds);

  slide.querySelectorAll(".shot-zoom img").forEach((img) => {
    if (!img.naturalWidth || !img.naturalHeight) return;
    if (img.closest(".deploy-shots")) return;
    const zoom = img.closest(".shot-zoom");
    const card = img.closest(".shot-card");
    if (!zoom || !card) return;
    const pair = card.closest(".shot-pair");
    const jwt = card.closest(".slide-jwt");
    const shot = img.closest(".shot") || card;
    const cap = shot.querySelector("figcaption");
    const capHidden = !cap || getComputedStyle(cap).display === "none";
    const capH = capHidden ? 0 : Math.min(cap.offsetHeight, 56);
    const maxH = Math.max(40, bounds.floor - zoom.getBoundingClientRect().top - capH - 8);
    const host = pair ? shot : card;
    const roomR = bounds.right - host.getBoundingClientRect().left;
    const colW = Math.max(1, Math.min(host.clientWidth || bounds.width, roomR));
    const maxW = jwt ? Math.max(1, Math.min(bounds.width * 0.56, colW)) : colW;
    sizeShot(img, maxW, maxH);
  });
}

function scheduleFitShots() {
  requestAnimationFrame(() => {
    fitShots();
    requestAnimationFrame(fitShots);
  });
  window.setTimeout(fitShots, 80);
}

function fit() {
  const cs = getComputedStyle(wrap);
  const availW = Math.max(0, wrap.clientWidth - parseFloat(cs.paddingLeft) - parseFloat(cs.paddingRight));
  const availH = Math.max(0, wrap.clientHeight - parseFloat(cs.paddingTop) - parseFloat(cs.paddingBottom));
  const width = Math.max(0, Math.min(availW, (availH * 16) / 9));
  const height = (width * 9) / 16;
  stage.style.width = `${width}px`;
  stage.style.height = `${height}px`;
  stage.style.setProperty("--u", `${width / 1280}px`);
  scheduleFitShots();
}

function go(next) {
  if (leaving || lightboxOpen()) return;
  const target = clamp(next);
  if (target === index) return;
  const current = slides[index];
  current.classList.remove("is-active");
  current.classList.add("is-leaving");
  leaving = true;
  window.setTimeout(() => {
    current.classList.remove("is-leaving");
    index = target;
    show();
    leaving = false;
  }, 220);
}

function show() {
  slides.forEach((slide, i) => {
    const active = i === index;
    slide.classList.toggle("is-active", active);
    slide.setAttribute("aria-hidden", active ? "false" : "true");
    dots[i].classList.toggle("is-on", active);
    if (active) {
      slide.querySelectorAll(".anim").forEach((el) => {
        el.style.animation = "none";
        void el.offsetWidth;
        el.style.animation = "";
      });
    }
  });
  progress.style.width = `${((index + 1) / slides.length) * 100}%`;
  counter.textContent = `${String(index + 1).padStart(2, "0")} / ${String(slides.length).padStart(2, "0")}`;
  notesSay.textContent = slides[index].dataset.say || slides[index].dataset.notes || "";
  const who = slides[index].dataset.who;
  const time = slides[index].dataset.time;
  notesTime.textContent = ["Decir", who, time].filter(Boolean).join(" · ");
  const hint = slides[index].dataset.hint || "";
  notesHint.textContent = hint;
  notesHintWrap.hidden = !hint;
  sectionLabel.textContent = slides[index].dataset.section || "MesaTech";
  const dark = slides[index].classList.contains("cover")
    || slides[index].classList.contains("slide-agenda")
    || slides[index].classList.contains("slide-jwt")
    || slides[index].classList.contains("slide-checks");
  chrome.classList.toggle("is-dark", dark);
  if (location.hash !== `#${index + 1}`) location.hash = String(index + 1);
  scheduleFitShots();
}

window.addEventListener("resize", fit);
window.visualViewport?.addEventListener("resize", fit);
if (typeof ResizeObserver === "function") {
  new ResizeObserver(fit).observe(wrap);
}
window.addEventListener("load", () => {
  fit();
  document.querySelectorAll(".shot-zoom img").forEach((img) => {
    img.addEventListener("load", scheduleFitShots);
  });
});
fit();

document.querySelector("[data-prev]").addEventListener("click", (event) => {
  event.stopPropagation();
  go(index - 1);
});
document.querySelector("[data-next]").addEventListener("click", (event) => {
  event.stopPropagation();
  go(index + 1);
});
notesBtn.addEventListener("click", (event) => {
  event.stopPropagation();
  notesEl.classList.toggle("is-on");
  notesBtn.classList.toggle("is-on", notesEl.classList.contains("is-on"));
});
document.querySelector("[data-full]").addEventListener("click", (event) => {
  event.stopPropagation();
  if (!document.fullscreenElement) document.documentElement.requestFullscreen();
  else document.exitFullscreen();
});

document.querySelectorAll("[data-zoom]").forEach((btn) => {
  btn.addEventListener("click", (event) => {
    event.preventDefault();
    event.stopPropagation();
    const img = btn.querySelector("img");
    const cap = btn.closest("figure")?.querySelector("figcaption");
    lightboxImg.src = btn.dataset.zoom;
    lightboxImg.alt = img?.alt || "";
    lightboxCap.textContent = cap?.textContent || "";
    lightbox.hidden = false;
    lightbox.classList.add("is-on");
  });
});

lightbox.addEventListener("click", (event) => {
  if (event.target === lightbox || event.target.closest("[data-lightbox-close]")) {
    closeLightbox();
  }
});

document.addEventListener("keydown", (event) => {
  if (lightboxOpen()) {
    if (event.key === "Escape") closeLightbox();
    return;
  }
  if (["ArrowRight", "ArrowDown", "PageDown", " ", "Enter"].includes(event.key)) {
    event.preventDefault();
    go(index + 1);
  }
  if (["ArrowLeft", "ArrowUp", "PageUp", "Backspace"].includes(event.key)) {
    event.preventDefault();
    go(index - 1);
  }
  if (event.key === "Home") go(0);
  if (event.key === "End") go(slides.length - 1);
  if (event.key.toLowerCase() === "n") {
    notesEl.classList.toggle("is-on");
    notesBtn.classList.toggle("is-on", notesEl.classList.contains("is-on"));
  }
  if (event.key.toLowerCase() === "f") {
    if (!document.fullscreenElement) document.documentElement.requestFullscreen();
    else document.exitFullscreen();
  }
  if (event.key === "Escape") {
    notesEl.classList.remove("is-on");
    notesBtn.classList.remove("is-on");
  }
});

let touchX = 0;
document.addEventListener("touchstart", (event) => {
  touchX = event.changedTouches[0].screenX;
});
document.addEventListener("touchend", (event) => {
  if (lightboxOpen()) return;
  const dx = event.changedTouches[0].screenX - touchX;
  if (dx < -50) go(index + 1);
  if (dx > 50) go(index - 1);
});

window.addEventListener("hashchange", () => {
  const n = Number.parseInt(location.hash.replace("#", ""), 10);
  if (!Number.isFinite(n)) return;
  const target = clamp(n - 1);
  if (target !== index) go(target);
});

const fromHash = Number.parseInt(location.hash.replace("#", ""), 10);
index = Number.isFinite(fromHash) ? clamp(fromHash - 1) : 0;
show();
