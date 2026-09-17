"""Genera diagrama de despliegue EV1 (3 EC2) para informe / NIC-10."""

from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

W, H = 1600, 920
img = Image.new("RGB", (W, H), "#f1f5f9")
d = ImageDraw.Draw(img)


def font(size, bold=False):
    names = ["arialbd.ttf", "arial.ttf"] if bold else ["arial.ttf", "segoeui.ttf"]
    for name in names:
        try:
            return ImageFont.truetype(name, size)
        except OSError:
            continue
    return ImageFont.load_default()


F_T = font(26, True)
F_S = font(14)
F_B = font(13, True)
F_X = font(11)
F_XS = font(10)


def box(xy, title, lines, fill, border, title_fill="#0f172a"):
    x1, y1, x2, y2 = xy
    d.rounded_rectangle(xy, radius=16, fill=fill, outline=border, width=2)
    d.text((x1 + 14, y1 + 12), title, fill=title_fill, font=F_B)
    y = y1 + 38
    for line in lines:
        d.text((x1 + 14, y), line, fill="#334155", font=F_X)
        y += 18


# Header
d.text(
    (W // 2, 24),
    "MesaTech EV1 — Despliegue AWS (us-east-1)",
    fill="#0f172a",
    font=F_T,
    anchor="ma",
)
d.text(
    (W // 2, 54),
    "3 EC2 · Entra ID · API Gateway · BFF + MS + PostgreSQL Docker",
    fill="#475569",
    font=F_S,
    anchor="ma",
)

# Azure Entra
box(
    (40, 90, 320, 200),
    "Microsoft Entra ID",
    ["Login OAuth2 / OIDC", "JWT: issuer + audience", "Scope access_as_user"],
    "#fff7ed",
    "#ea580c",
    "#9a3412",
)

# React
box(
    (40, 230, 320, 340),
    "React + MSAL (local / host)",
    ["REACT_APP_API_BASE_URL = Gateway", "(no IP de microservicios)"],
    "#eff6ff",
    "#2563eb",
    "#1e40af",
)

# Gateway placeholder
box(
    (360, 90, 720, 220),
    "API Gateway HTTP API",
    [
        "Invoke URL: (Ninna — pendiente)",
        "ej. https://xxxx.execute-api.us-east-1.amazonaws.com",
        "JWT Authorizer + CORS",
    ],
    "#f5f3ff",
    "#7c3aed",
    "#5b21b6",
)

# AWS region frame
d.rounded_rectangle((340, 250, 1560, 860), radius=20, outline="#f59e0b", width=3)
d.text((360, 262), "AWS us-east-1 (Learner Lab)", fill="#b45309", font=F_B)

# EC2 BFF
box(
    (380, 300, 720, 480),
    "EC2 mesatech-ev1-bff",
    [
        "bff.jar :8080 (Resource Server)",
        "Public: 54.90.110.67",
        "Private: 172.31.20.75",
        "SG: SSH + 8080 (Mi IP / Gateway*)",
    ],
    "#ecfdf5",
    "#059669",
    "#065f46",
)

# EC2 MS
box(
    (760, 300, 1100, 500),
    "EC2 mesatech-ev1-ms",
    [
        "ms-solicitudes.jar :8081",
        "ms-catalogo.jar :8082",
        "Public: 54.227.0.33",
        "Private: 172.31.24.74",
        "SG: 8081-8082 solo desde BFF-SG",
    ],
    "#f0fdf4",
    "#16a34a",
    "#14532d",
)

# EC2 DB
box(
    (1140, 300, 1520, 480),
    "EC2 mesatech-ev1-db",
    [
        "Docker postgres:16",
        "Public: 54.90.194.247 (SSH admin)",
        "Private: 172.31.25.66 :5432",
        "SG: 5432 solo desde MS-SG",
    ],
    "#e0f2fe",
    "#0284c7",
    "#0c4a6e",
)

# Security note
box(
    (380, 540, 1520, 640),
    "Seguridad (evidencia)",
    [
        "NIC-07: curl BFF /v1/... sin token → 401 (54.90.110.67:8080)",
        "NIC-08: curl MS :8081 desde Internet → timeout",
        "React → solo Gateway · MS/DB no expuestos a 0.0.0.0/0",
    ],
    "#fef2f2",
    "#dc2626",
    "#991b1b",
)

# Arrows (simple)
def arrow(p1, p2, color):
    d.line([p1, p2], fill=color, width=3)
    x2, y2 = p2
    d.polygon([(x2, y2), (x2 - 10, y2 - 5), (x2 - 10, y2 + 5)], fill=color)


arrow((320, 280), (360, 280), "#2563eb")  # React -> GW
arrow((720, 150), (380, 390), "#7c3aed")  # GW -> BFF
arrow((720, 400), (760, 400), "#059669")  # BFF -> MS
arrow((1100, 390), (1140, 390), "#0284c7")  # MS -> DB
d.text((180, 268), "Bearer", fill="#2563eb", font=F_XS)
d.text((540, 130), "HTTP", fill="#7c3aed", font=F_XS)
d.text((735, 382), "8081/8082", fill="#059669", font=F_XS)
d.text((1118, 372), "5432", fill="#0284c7", font=F_XS)

d.text(
    (W // 2, 880),
    "* Cuando Ninna integre Gateway, puede requerir abrir 8080 del BFF al tráfico del integrador (coordinar SG).",
    fill="#64748b",
    font=F_XS,
    anchor="ma",
)

out = Path(__file__).resolve().parents[2] / "docs" / "assets" / "diagramas" / "arquitectura_mesatech_ev1_despliegue.png"
out.parent.mkdir(parents=True, exist_ok=True)
img.save(out, "PNG")
print("saved", out)
