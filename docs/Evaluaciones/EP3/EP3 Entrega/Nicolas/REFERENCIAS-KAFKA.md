# Referencias Kafka (profesor)

| Repo | URL |
| --- | --- |
| Básico | https://github.com/pauloberrios44/seccion-1-kafka-basico.git |
| Persistencia | https://github.com/pauloberrios44/kafka-persistencia-mysql-seccion-1.git |

```powershell
cd _refs
git clone https://github.com/pauloberrios44/seccion-1-kafka-basico.git ep3-kafka-basico
git clone https://github.com/pauloberrios44/kafka-persistencia-mysql-seccion-1.git ep3-kafka-mysql
```

**Curso compose:** `docs/Evaluaciones/EP3/EP3 Contexto/Trabajando-con-Kafka-extract.txt`

**Patrones a replicar en `ms-catalogo`:**

- `KafkaTemplate.send(topic, key, payload)`
- `@KafkaListener` + headers partición/offset/key
- `enable-auto-commit=false` + ack tras persistir
- `@Valid` en DTO del listener