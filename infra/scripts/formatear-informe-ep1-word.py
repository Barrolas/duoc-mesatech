"""Genera informe EP1 MesaTech (narrativa + evidencias) desde Screenshots EP1 + PNG.

Salida por defecto: MesaTech_EV1_Informe.docx en la raíz del repo.

  python infra/scripts/formatear-informe-ep1-word.py
  python infra/scripts/formatear-informe-ep1-word.py "Screenshots EP1.docx" "C:\\...\\Screenshots"
"""

from generar_informe_mesatech import DEFAULT_ASSETS, DEFAULT_SRC, OUT_NAME, build_informe
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]


def main():
    source = Path(sys.argv[1]).resolve() if len(sys.argv) > 1 else DEFAULT_SRC
    assets = Path(sys.argv[2]).resolve() if len(sys.argv) > 2 else DEFAULT_ASSETS
    out = REPO_ROOT / OUT_NAME
    try:
        build_informe(source, assets, out)
        print("Generado:", out)
    except PermissionError:
        alt = out.with_name(OUT_NAME.replace(".docx", "_v2.docx"))
        build_informe(source, assets, alt)
        print("Generado:", alt)


if __name__ == "__main__":
    main()
