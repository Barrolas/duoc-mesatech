# Consumer lag — procedimiento Skarlett + Ari

**Objetivo:** Lag por partición antes / durante / después; comparar concurrencia 1 vs 3.

## Roles

- **Skarlett:** ejecuta prueba, lote de publicación, tiempos, correlación RDS.
- **Ari:** broker estable, `kafka-consumer-groups --describe`, capturas CLI/UI.
- **Nico (opcional):** ayuda a generar carga en tópico prioridad si se acuerda.

## Pasos

1. **Línea base:** anotar por partición CURRENT-OFFSET, LOG-END-OFFSET, lag = END − CURRENT.
2. **Incidente:** en **un** tópico (solicitud), publicar lote grande mientras consumidor va lento (concurrency 1 o pausa controlada). Los otros dos tópicos deben seguir procesando.
3. **Medición durante:** capturar lag máximo por partición.
4. **Recuperación:** subir concurrency a 3; medir tiempo hasta lag ≈ 0.
5. **Comparar:** repetir lote equivalente (misma distribución de keys) con concurrency 1 y 3.
6. **Interpretación:** tabla volumen, asignaciones, tasa, qué revisaría operador si una partición sigue alta.

## Comando útil (EC2 D)

```bash
docker exec -it kafka kafka-consumer-groups --bootstrap-server localhost:9092 \
  --describe --group <nombre-grupo>
```

Registrar en `PRUEBAS-EP3.md` y evidencias `Evidencias/`.