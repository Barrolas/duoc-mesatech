# Borrador para Nicolás — `ms-catalogo` Kafka (EP3 MesaTech)

Implementación completa de productor y consumidor Kafka para `ms-catalogo`, alineada con:
- `ARQUITECTURA-ACUERDOS.md` (Alternativa 1)
- `docs/Evaluaciones/EP3/EP3 Entrega/Giannina/Contrato-eventos-EP3.md` (§2.3 y §3)
- `docs/Evaluaciones/EP3/EP3 Entrega/Nicolas/BACKLOG.md`
- Mismo patrón técnico que `ms-solicitudes` (Spring Boot 4.1.1, Jackson 3 `JsonMapper`, validación `@Valid` + `DefaultErrorHandler` + DLT `mesatech.dlq.eventos`, ack manual post-save, idempotencia por `eventId`, log estructurado `operacion.jsonl`).

> Este diff **no toca** `env.example` ni `infra/`, por lo que aplica sin conflictos junto al parche de Skarlett (`borrador_skarlett.diff` sobre `fb26b69` o `borrador_skarlett_sobre_giannina.diff` sobre `561dcd3`).

---

## 1. Qué incluye `ms-catalogo-kafka.diff`

| Archivo | Propósito |
| --- | --- |
| `ms-catalogo/pom.xml` | Agrega `spring-boot-starter-kafka`, `spring-boot-starter-validation`, `spring-boot-starter-test`, `spring-boot-starter-kafka-test`. |
| `application.properties` | Configura Kafka (`acks=all`, `retries=3`, `group-id=mesatech-catalogo-kafka`, `enable-auto-commit=false`, `ack-mode=manual`, `KAFKA_LISTENER_CONCURRENCY` unificada, tópicos `mesatech.catalogo.prioridad` y `mesatech.dlq.eventos`). Mantiene intacto RabbitMQ EP2. |
| `logback-spring.xml` | Logger `OPERACION_JSONL` escribiendo en `OPERACION_LOG_PATH` (default `logs/operacion.jsonl`). |
| `AuditoriaPrioridad.java` + `AuditoriaPrioridadRepository.java` | Entidad/tabla `auditoria_prioridad` en `mesatech_catalogo` con `event_id` único para idempotencia. |
| `PrioridadRenombradaEvent.java` | Record DTO con validación `@Pattern` UUID v4, `@NotBlank`, `@Positive`, `@Min(1)`/`@Max(5)` opcional en `nivel`. |
| `PrioridadKafkaProducer.java` | Publica `catalogo.prioridad.renombrada` en `mesatech.catalogo.prioridad` con key `prioridad-{id}` y callback en `operacion.jsonl`. |
| `AuditoriaPrioridadService.java` | Normaliza (`trim` + colapsar espacios + `equalsIgnoreCase`) y clasifica `CORRECCION_FORMATO` vs `REVISION_SUSTANTIVA`; ignora duplicados por `eventId`; rechaza nombres idénticos literales. |
| `PrioridadRenombradaKafkaListener.java` | `@KafkaListener` con `@Valid`, persistencia en `auditoria_prioridad`, `acknowledgment.acknowledge()` **después** de guardar y log en `operacion.jsonl`. |
| `KafkaConfig.java` | `LocalValidatorFactoryBean`, `StringJacksonJsonMessageConverter`, `DefaultErrorHandler` con `DeadLetterPublishingRecoverer` hacia `mesatech.dlq.eventos` (sin reintentos en errores de contrato/validación, 2 reintentos de 1 s en fallos transitorios). |
| `CatalogoController.java` | Hook en `PUT /v1/catalogo/prioridades/{id}`: guarda y publica evento si cambió `nombre` (valida `nivel` 1–5). `POST /v1/catalogo/categorias` (RabbitMQ EP2) intacto. |
| 4 clases de tests | `PrioridadEventosValidationTest`, `AuditoriaPrioridadServiceTest`, `PrioridadRenombradaKafkaListenerTest`, `CatalogoControllerTest`. |

---

## 2. Cómo aplicarlo y probarlo

```bash
git fetch origin
git checkout -b feature/nico-ep3-kafka origin/dev
git apply /ruta/a/ms-catalogo-kafka.diff

cd ms-catalogo
mvn test
```

En local, asegúrate de tener `KAFKA_BOOTSTRAP_SERVERS=localhost:9092` y el tópico `mesatech.catalogo.prioridad` (3 particiones) + `mesatech.dlq.eventos` (3 particiones).  
En EC2 A (`infra/ec2/ms-catalogo.env`), agregar:

```bash
export KAFKA_BOOTSTRAP_SERVERS="<IP_PRIVADA_EC2_D>:9092"
export KAFKA_LISTENER_CONCURRENCY="3"
```

### Prueba de pauta (inválido → DLQ → válido con misma key)

1. Publicar `docs/Evaluaciones/EP3/EP3 Entrega/Giannina/contratos-json/prioridad-invalido.json` (`nombreNuevo: ""`) con key `prioridad-2` en `mesatech.catalogo.prioridad`.
2. Verificar en `auditoria_prioridad` que **no** se insertó fila, y en `logs/operacion.jsonl` / `mesatech.dlq.eventos` que se rechazó con `nombreNuevo: es obligatorio`.
3. Publicar `prioridad-valido.json` (`"Alta"` → `"Crítica operacional"`) con **la misma key `prioridad-2`**.
4. Verificar fila en `auditoria_prioridad` con `tipo_cambio = 'REVISION_SUSTANTIVA'`, misma partición y offset siguiente.
