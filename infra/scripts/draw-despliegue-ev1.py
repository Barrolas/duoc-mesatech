# -*- coding: utf-8 -*-
"""Diagrama EV1: arquitectura implementada + despliegue (paleta MesaTech)."""
from pathlib import Path
import math

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[2]
OUTS = [
    ROOT / "docs" / "assets" / "diagramas" / "arquitectura_mesatech_ev1_despliegue.png",
    ROOT / "entregables" / "_inspect" / "arquitectura_v2.png",
    ROOT / "entregables" / "presentacion" / "img" / "arquitectura_mesatech_ev1_despliegue.png",
]

W, H = 2200, 1260
TEAL = "#0F4C5C"
TEAL_D = "#0A3843"
AQUA = "#2EC4B6"
MINT = "#E8FAF6"
SAND = "#F4F7F6"
INK = "#12353C"
MUTED = "#4A646C"
WHITE = "#FFFFFF"
LINE = "#B7D0CC"
ORANGE = "#C2410C"

img = Image.new("RGB", (W, H), SAND)
d = ImageDraw.Draw(img)


def font(size, bold=False):
    names = (
        ["segoeuib.ttf", "arialbd.ttf", "calibrib.ttf"]
        if bold
        else ["segoeui.ttf", "arial.ttf", "calibri.ttf"]
    )
    for name in names:
        try:
            return ImageFont.truetype(name, size)
        except OSError:
            continue
    return ImageFont.load_default()


FT = font(30, True)
FS = font(15)
FB = font(16, True)
FX = font(14)
FXX = font(12)
FNUM = font(13, True)


def rr(xy, fill, outline, width=2, radius=16):
    d.rounded_rectangle(xy, radius=radius, fill=fill, outline=outline, width=width)


def text(xy, s, fill, f, anchor="lt"):
    d.text(xy, s, fill=fill, font=f, anchor=anchor)


def wrap(s, f, max_w):
    words = s.split()
    lines, cur = [], ""
    for w in words:
        trial = (cur + " " + w).strip()
        if d.textlength(trial, font=f) <= max_w:
            cur = trial
        else:
            if cur:
                lines.append(cur)
            cur = w
    if cur:
        lines.append(cur)
    return lines or [s]


def lines_at(x, y, items, fill=MUTED, f=FX, gap=20, max_w=None):
    yy = y
    for s in items:
        chunk = wrap(s, f, max_w) if max_w else [s]
        for c in chunk:
            text((x, yy), c, fill, f)
            yy += gap
    return yy


def arrow(p1, p2, color=TEAL, width=3):
    d.line([p1, p2], fill=color, width=width)
    x1, y1 = p1
    x2, y2 = p2
    ang = math.atan2(y2 - y1, x2 - x1)
    L = 12
    a1 = (x2 - L * math.cos(ang - 0.42), y2 - L * math.sin(ang - 0.42))
    a2 = (x2 - L * math.cos(ang + 0.42), y2 - L * math.sin(ang + 0.42))
    d.polygon([p2, a1, a2], fill=color)


def badge(cx, cy, n):
    r = 14
    d.ellipse((cx - r, cy - r, cx + r, cy + r), fill=AQUA, outline=TEAL_D, width=1)
    text((cx, cy + 1), str(n), TEAL_D, FNUM, anchor="mm")


# Header
text((48, 28), "MESATECH CLOUD  ·  Arquitectura implementada EV1", TEAL, FT)
lines_at(
    48,
    68,
    ["React + MSAL  →  Entra ID  →  API Gateway  →  BFF :8080  →  microservicios  →  PostgreSQL 16"],
    MUTED,
    FS,
)
text((W - 48, 32), "DSY1107  ·  us-east-1", AQUA, FB, anchor="rt")
text((W - 48, 58), "Learner Lab  ·  3 instancias EC2", MUTED, FXX, anchor="rt")

# Entra (above React)
rr((280, 100, 700, 236), WHITE, "#E8A87C", 2)
text((298, 112), "Microsoft Entra ID", ORANGE, FB)
lines_at(
    298,
    142,
    [
        "SPA React + API api-cloud-native",
        "Scope access_as_user  ·  token v2",
        "Roles: cliente · operador · administrador",
        "Issuer y audience: Gateway y BFF",
    ],
    MUTED,
    FXX,
    20,
    max_w=380,
)

# Actors
rr((48, 288, 250, 560), WHITE, LINE)
text((66, 304), "Actores", TEAL, FB)
lines_at(
    66,
    340,
    [
        "Cliente",
        "crea y ve las suyas",
        "Operador",
        "cola y estados",
        "Administrador",
        "catálogo y visión global",
    ],
    INK,
    FX,
    30,
)

# React
rr((280, 288, 700, 560), WHITE, TEAL, 3)
text((298, 304), "Frontend  ·  React + MSAL", TEAL, FB)
lines_at(
    298,
    340,
    [
        "Login y logout con Entra ID",
        "Muestra nombre, correo y rol",
        "Axios solo hacia el Gateway",
        "No llama a :8081 ni :8082",
        "localhost:3000 en desarrollo",
        "Prohibido: React → IP de EC2",
    ],
    MUTED,
    FX,
    32,
    max_w=380,
)

# AWS
rr((730, 100, 2152, 900), "#F7FBFA", TEAL, 3, radius=22)
text((752, 118), "AWS  ·  us-east-1", TEAL, FB)
text(
    (980, 122),
    "Entrada pública: HTTP API. MS y PostgreSQL no abiertos a Internet.",
    MUTED,
    FXX,
)

# Gateway
rr((752, 158, 1368, 470), WHITE, AQUA, 3)
text((772, 176), "API Gateway  ·  HTTP API", TEAL, FB)
lines_at(
    772,
    214,
    [
        "mesatech-ev1",
        "7gqw4633sg.execute-api.us-east-1.amazonaws.com",
        "JWT Authorizer  ·  CORS + Authorization",
        "Rutas /v1 y /v2  ·  /api/usuario",
        "Integración HTTP → BFF :8080",
        "Sin JWT → 401 (no llega al BFF)",
    ],
    MUTED,
    FX,
    28,
    max_w=560,
)

# BFF
rr((1390, 158, 2130, 470), WHITE, TEAL, 2)
text((1410, 176), "EC2 BFF  ·  mesatech-ev1-bff", TEAL, FB)
lines_at(
    1410,
    214,
    [
        "54.90.110.67  /  172.31.20.75  ·  :8080",
        "Spring Security Resource Server",
        "Revalida JWT  ·  403 según el rol",
        "Sin JPA  ·  no toca la base",
        "Proxy hacia :8081 y :8082",
    ],
    MUTED,
    FX,
    28,
    max_w=680,
)

# Notes
rr((752, 500, 1368, 868), MINT, AQUA, 1)
text((772, 520), "Regla de negocio y recorte EV1", TEAL, FB)
lines_at(
    772,
    562,
    [
        "RESUELTA solo desde EN_PROCESO. Si no, 409.",
        "CERRADA solo desde RESUELTA.",
        "React nunca apunta a IPs de microservicio.",
        "Fuera de alcance: Lambda, ECS, Cognito, S3, ALB, RDS Aurora.",
        "Persistencia: PostgreSQL 16 en Docker, sobre EC2.",
    ],
    INK,
    FX,
    28,
    max_w=560,
)

# MS
rr((1390, 500, 1758, 868), WHITE, TEAL, 2)
text((1410, 518), "EC2 microservicios", TEAL, FB)
lines_at(
    1410,
    560,
    [
        "mesatech-ev1-ms",
        "172.31.24.74",
        "ms-solicitudes :8081",
        "ms-catalogo :8082",
        "SG: solo desde el BFF",
        "Timeout desde Internet",
    ],
    MUTED,
    FX,
    28,
    max_w=320,
)

# DB
rr((1780, 500, 2130, 868), WHITE, "#3D7A8C", 2)
text((1800, 518), "EC2 datos", TEAL, FB)
lines_at(
    1800,
    560,
    [
        "mesatech-ev1-db",
        "PostgreSQL 16 Docker",
        "172.31.25.66 :5432",
        "mesatech_solicitudes",
        "mesatech_catalogo",
        "Solo escriben los MS",
    ],
    MUTED,
    FX,
    28,
    max_w=300,
)

# Arrows
arrow((250, 424), (280, 424), TEAL)
arrow((400, 236), (400, 288), AQUA)
badge(400, 262, 1)
arrow((580, 288), (580, 236), AQUA)
badge(580, 262, 2)
arrow((700, 414), (752, 414), TEAL)
badge(726, 382, 3)
arrow((1368, 314), (1390, 314), TEAL)
badge(1379, 296, 4)
arrow((1574, 470), (1574, 500), TEAL)
badge(1596, 485, 5)
arrow((1758, 684), (1780, 684), TEAL)
badge(1769, 666, 6)

text((752, 878), "7–10. JSON de vuelta: MS → BFF → Gateway → React", MUTED, FXX)

# Legend
rr((48, 924, 2152, 1224), WHITE, LINE, 1, radius=18)
text((70, 944), "Leyenda del flujo (guía EP1 §10)", TEAL, FB)
legend = [
    "1  React redirige a Entra (loginRedirect).",
    "2  Entra entrega ID Token y Access Token (scope access_as_user, claim roles).",
    "3  React llama al Invoke URL con Authorization: Bearer. Sin token → 401 en el Gateway.",
    "4  Authorizer OK: Gateway reenvía al BFF :8080. El BFF vuelve a validar el JWT y aplica 403 por rol.",
    "5–6  El BFF llama al microservicio; el MS lee o escribe PostgreSQL. Ni el BFF ni React tocan la base.",
    "7–10  La respuesta vuelve a la UI. El cliente ve lo suyo; el operador, la cola; el admin, el catálogo.",
]
lines_at(70, 984, legend, INK, FX, 26, max_w=2000)
text(
    (2152 - 28, 1196),
    "Informe Formal v2  ·  coincidente con el recorrido de capítulos",
    MUTED,
    FXX,
    anchor="rb",
)

for out in OUTS:
    out.parent.mkdir(parents=True, exist_ok=True)
    img.save(out, "PNG", optimize=True)
    print("saved", out)
