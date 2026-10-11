# Hospital Vida Salud - T2 (Microservicios reactivos + CI)

Proyecto academico (datos ficticios) de la Segunda Evaluacion T2 - Cibertec, curso 4698 Desarrollo de Aplicaciones Web II.

## Arquitectura

```
Cliente (Postman / curl)
        |
        v
  API GATEWAY :8080  ----- consulta ----->  EUREKA :8761
        |   (rutas lb://nombre-servicio)         ^ (registro)
        |                                        |
  +-----+------------+-------------+-------------+--------+
  |                  |             |                      |
Pacientes :8081   Citas :8082   Notificaciones :8083   Auditoria :8084
(pacientes_db)    (citas_db)    (notificaciones_db)    (auditoria_db)
  ^                  |  \_ HTTP sincrono (WebClient) _/ 
  |__________________|
                     |  evento CitaEvento
                     v
              RabbitMQ  (citas.exchange, topic)
              cita.* -> notificaciones.cita.queue  -> Notificaciones
              cita.* -> auditoria.cita.queue       -> Auditoria

Persistencia: PostgreSQL + Spring Data R2DBC (una BD por servicio)
```

| Servicio | Puerto | Responsabilidad | Datos propios |
|---|---|---|---|
| eureka-server | 8761 | Registro y descubrimiento | - |
| api-gateway | 8080 | Unica entrada, enruta por nombre de servicio | - |
| pacientes-service | 8081 | Registro/consulta/actualizacion de pacientes, evita duplicados | pacientes_db.paciente |
| citas-service | 8082 | Programar citas y controlar sus estados | citas_db.cita |
| notificaciones-service | 8083 | Avisos simulados (BD) al cambiar una cita | notificaciones_db.notificacion |
| auditoria-service | 8084 | Trazabilidad de cambios de estado | auditoria_db.auditoria_evento |

Los clientes solo usan el puerto 8080: los puertos internos pueden cambiar o haber varias instancias sin que el cliente lo note.

### Comunicacion
- **Sincrona**: al crear una cita, Citas llama a `GET /api/pacientes/{id}` de Pacientes (WebClient + Eureka). Timeout 3 s. Paciente inexistente -> 404; Pacientes caido -> 503.
- **Asincrona (RabbitMQ)**: Citas publica `CitaEvento` en `citas.exchange` (topic) con routing key `cita.<estado>` (ej. `cita.confirmada`). Notificaciones y Auditoria tienen cada una su cola enlazada a `cita.*`. Citas no conoce a los consumidores.

### Reglas de negocio
- Paciente unico por (tipo_documento, numero_documento): duplicado -> 409 (ademas hay restriccion UNIQUE en BD).
- Cita: paciente existente y ACTIVO, fecha futura, un profesional no puede tener dos citas activas en el mismo horario.
- Estados de cita: `PENDIENTE -> CONFIRMADA | CANCELADA`, `CONFIRMADA -> ATENDIDA | CANCELADA | AUSENTE`. ATENDIDA, CANCELADA y AUSENTE son finales (una cita cancelada no se confirma -> 409).

### Supuestos y limitaciones
- Agenda simplificada: se valida solo coincidencia exacta de profesional + fecha/hora.
- Si RabbitMQ esta caido, el cambio de estado se guarda igual y se registra un warning (mejora futura: patron Outbox).
- Atenciones, Laboratorio, Hospitalizacion, Farmacia y Pagos no se implementan (el enunciado exige un flujo extremo a extremo, no todos los modulos).
- Los consumidores usan `block()` dentro del hilo del listener AMQP (no es un hilo reactivo) para confirmar el mensaje solo tras persistir.

## Requisitos
- JDK 21, Maven 3.9+, Docker (para PostgreSQL y RabbitMQ).
- Si usas otra version de Java, cambia `java.version` en el `pom.xml` raiz y `java-version` en `.github/workflows/ci.yml`.

## Ejecutar

1. Variables (no se suben a Git): copia `.env.example` a `.env` y cambia las claves.
2. Infraestructura:
   ```
   docker compose up -d
   ```
   (crea las 4 bases de datos; RabbitMQ panel: http://localhost:15672)
3. Define las variables en cada terminal donde levantes un servicio.
   PowerShell:
   ```
   $env:DB_PASSWORD="tu_clave_db"; $env:RABBIT_PASSWORD="tu_clave_rabbit"
   ```
   Bash:
   ```
   export DB_PASSWORD=tu_clave_db RABBIT_PASSWORD=tu_clave_rabbit
   ```
   (`DB_USER` por defecto `postgres`, `RABBIT_USER` por defecto `hospital`.)
4. Levanta en este orden (una terminal por servicio, desde la raiz):
   ```
   mvn -pl eureka-server spring-boot:run
   mvn -pl api-gateway spring-boot:run
   mvn -pl pacientes-service spring-boot:run
   mvn -pl citas-service spring-boot:run
   mvn -pl notificaciones-service spring-boot:run
   mvn -pl auditoria-service spring-boot:run
   ```
5. Panel Eureka: http://localhost:8761

## Probar el flujo (siempre por el Gateway :8080)

```
# 1. Registrar paciente
curl -X POST http://localhost:8080/api/pacientes -H "Content-Type: application/json" -d '{"tipoDocumento":"DNI","numeroDocumento":"70000001","nombres":"Ana","apellidos":"Prueba Ficticia","fechaNacimiento":"1990-05-20","telefono":"999000111","correo":"ana@ejemplo.test"}'

# 2. Consultar paciente
curl http://localhost:8080/api/pacientes/1

# 3. Duplicado (repetir el paso 1) -> 409

# 4. Registrar cita (cambia la fecha a una futura)
curl -X POST http://localhost:8080/api/citas -H "Content-Type: application/json" -d '{"pacienteId":1,"especialidad":"Cardiologia","fechaHora":"2026-12-15T10:00:00","profesionalCodigo":"MED-001","motivo":"Control administrativo"}'

# 5. Confirmar cita -> publica el evento CitaConfirmada
curl -X PATCH http://localhost:8080/api/citas/1/estado -H "Content-Type: application/json" -d '{"estado":"CONFIRMADA"}'

# 6. Ver los efectos del evento
curl http://localhost:8080/api/notificaciones
curl http://localhost:8080/api/auditoria?recursoId=1

# 7. Transicion invalida: cancelar y luego intentar confirmar -> 409
```

## Pruebas automatizadas

```
mvn clean verify
```
Las pruebas no necesitan PostgreSQL ni RabbitMQ: usan H2 (R2DBC) y mocks. Cubren: paciente valido y duplicado, registro y consulta de cita, transiciones de estado invalidas, publicacion y consumo de eventos (componentes de mensajeria), recurso inexistente (404) y datos invalidos (400).

## Integracion continua (GitHub Actions)

`.github/workflows/ci.yml` se ejecuta en `push` y `pull_request` hacia `main`: checkout, JDK 21 con cache de Maven y `mvn --batch-mode verify` sobre todos los modulos. Si compila o una prueba falla, el job queda en rojo y no se debe integrar el cambio.

Fallo controlado (para la evidencia):
1. `git checkout -b prueba-fallo-ci`
2. Rompe una prueba a proposito (ej. cambia `"ACTIVO"` por `"INACTIVO"` en `PacienteServiceTest`), commit y push.
3. Abre un Pull Request hacia `main`: la pestana Actions muestra el CI en rojo (captura).
4. Corrige la prueba, commit y push: el CI pasa a verde (captura).
5. Fusiona el PR.

Nota: no se incluye Maven Wrapper; el runner `ubuntu-latest` ya trae Maven. Si quieres usarlo: `mvn wrapper:wrapper` y cambia el comando del workflow por `./mvnw --batch-mode verify`.

## Seguridad
Todos los datos son ficticios. No hay credenciales en el repositorio: se leen de variables de entorno (`DB_PASSWORD`, `RABBIT_PASSWORD`). El archivo `.env` esta en `.gitignore`. Los logs solo registran identificadores, no datos personales.
