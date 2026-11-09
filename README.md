# EchoStock

## Descripción del Proyecto 
**EchoStock** es una aplicación de software diseñada específicamente para dispositivos SmartWatch, dirigida a trabajadores de grandes almacenes y centros de distribución. 

---

## Requisitos Funcionales (FR)

* **RF-001 - Búsqueda por Comandos de Voz:** El sistema debe permitir al usuario dictar el nombre de un producto, SKU o código de ubicación, convirtiendo el audio a texto para buscar el ítem en la base de datos.
* **RF-002 - Lectura de Estado por Audio (TTS):** El sistema debe sintetizar a voz (Text-to-Speech) la cantidad registrada en el sistema ("Sistema indica 50 unidades de cajas tipo A") para que el operario no tenga que mirar la pantalla.
* **RF-003 - Confirmación Rápida (Tap):** La interfaz gráfica debe mostrar un botón primario de pantalla completa o deslizable (Swipe) para confirmar que el conteo físico coincide con el sistema.
* **RF-004 - Registro de Discrepancias:** Si hay diferencia de inventario, la interfaz gráfica debe mostrar selectores rápidos (+ / -) u opciones numéricas grandes para registrar la cantidad real.
* **RF-005 - Sincronización con Backend (ERP/WMS):** El reloj debe enviar en tiempo real la confirmación o discrepancia al servidor central mediante una API REST/GraphQL.
* **RF-006 - Modo Offline :** El sistema debe almacenar las auditorías localmente en el reloj si se pierde la conexión WiFi en áreas aisladas del almacén, y sincronizar automáticamente al recuperar señal.

---

## Requisitos No Funcionales (NFR)

* **RNF-001 - Usabilidad (Micro-interacciones):** Ninguna interacción táctil debe requerir más de 2 toques (taps) en la pantalla. La interfaz gráfica no debe usar teclados QWERTY virtuales bajo ninguna circunstancia.
* **RNF-002 - Accesibilidad Visual:** La interfaz debe operar con un contraste alto adaptado a condiciones de baja iluminación o deslumbramiento en almacenes, usando códigos de color universales (ej. Verde = Correcto, Rojo = Discrepancia).
* **RNF-003 - Retroalimentación Háptica:** Cada confirmación de voz entendida o botón presionado debe generar un patrón de vibración específico en el reloj para confirmar el éxito de la acción sin mirar.
* **RNF-004 - Tolerancia al Ruido (Voice-to-Text):** El procesamiento de voz debe ser capaz de filtrar ruido de fondo moderado típico de un almacén (maquinaria, carretillas).
* **RNF-005 - Eficiencia de Batería:** La aplicación debe gestionar el encendido de pantalla y el uso del micrófono para garantizar que el smartwatch no consuma más del 10% de batería por hora de auditoría activa.

---

## Backend (este repositorio)

Servidor central (ERP/WMS simplificado) al que se conecta el reloj Wear OS (`EchoStock-WearApp`).
El contrato de la API, compartido con la app, está en [`docs/api-contract.md`](docs/api-contract.md).

### Arquitectura

- **Stack:** Java 17, Spring Boot 3.5 (Web, Validation, Data JPA), Flyway, H2 / PostgreSQL 16,
  springdoc-openapi (Swagger UI). Construcción con Maven (`./mvnw`).
- **Paquetes** (`com.echostock`):
  - `product`: entidad `Product`, listado paginado y detalle por SKU.
  - `product.search`: búsqueda tolerante a voz (RF-001), lógica pura en Java y sin funciones
    específicas de ninguna base de datos:
    - `VoiceQueryNormalizer`: minúsculas, sin tildes, números en palabras → dígitos
      ("treinta y dos" → 32, "ciento dos" → 102, "dos mil quinientos" → 2500; "cero cero uno" → `0 0 1`),
      nombres de letras → letras tal como los transcribe Vosk ("se jota a" → `c j a`; "de" → `d` solo
      dentro de un deletreo, "guantes de nitrilo" se mantiene), letras sueltas unidas ("c j a" → `cja`) y palabras de relleno ignoradas al comparar códigos
      (pasillo, ubicación, estante, nivel, rack, sku, código, referencia, ref, producto, número, guion).
    - `ProductSearchScorer`: ranking SKU exacto (1.0) > SKU por segmentos (0.97) > ubicación exacta (0.9)
      > prefijo de SKU/ubicación > nombre por similitud (≤ 0.85, solapamiento de tokens con Levenshtein
      + trigramas). El catálogo se puntúa en memoria.
  - `audit`: auditorías de inventario. Idempotencia por `clientAuditId` (restricción única en BD,
    también ante peticiones simultáneas), recálculo de `result` en el servidor (422 si no cuadra),
    sincronización por lotes y revisión por supervisor (aprobar/rechazar).
  - `config`: filtro de API key (`X-Api-Key`), CORS, OpenAPI y reloj inyectable.
  - `common`: errores RFC 7807 (`application/problem+json`) y respuesta paginada.
  - `health`: `GET /api/v1/health` sin API key (comprueba también la base de datos).
- **Base de datos:** esquema y catálogo inicial (32 productos de almacén) en migraciones Flyway
  (`src/main/resources/db/migration`), SQL portable entre H2 y PostgreSQL.

### Cómo ejecutar

Requisitos: JDK 17 (`JAVA_HOME` apuntando a él). Maven no hace falta: se usa el wrapper.

```bash
# Compilar y pasar todas las pruebas (unitarias + integración con H2)
./mvnw verify

# Arrancar con el perfil por defecto "dev": H2 en archivo bajo ./data, sin Docker
./mvnw spring-boot:run
```

El servidor escucha en `0.0.0.0:8080`; desde el emulador Android se accede con `http://10.0.2.2:8080`.
Para volver al catálogo inicial basta con borrar la carpeta `./data`.

Con PostgreSQL 16 (Docker):

```bash
docker compose up -d
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

Configuración por variables de entorno:

| Variable | Por defecto | Uso |
|---|---|---|
| `ECHOSTOCK_API_KEY` | `echostock-dev-key` | Clave exigida en `X-Api-Key` |
| `ECHOSTOCK_DB_URL` | `jdbc:postgresql://localhost:5432/echostock` | Solo perfil `postgres` |
| `ECHOSTOCK_DB_USER` / `ECHOSTOCK_DB_PASSWORD` | `echostock` / `echostock` | Solo perfil `postgres` |

### Documentación interactiva

- Swagger UI: <http://localhost:8080/swagger-ui.html> (botón *Authorize* → `echostock-dev-key`)
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>
- Consola H2 (perfil `dev`): <http://localhost:8080/h2-console> (JDBC URL `jdbc:h2:file:./data/echostock`, usuario `sa`)

### Endpoints

Base: `/api/v1`. Todas las rutas exigen `X-Api-Key` salvo `/health`.

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/health` | Estado del servidor (sin API key) | 200 `{"status":"UP"}` · 503 |
| GET | `/products/search?q=&limit=` | Búsqueda tolerante a voz (`limit` 1–10, por defecto 5) | 200 (lista vacía si no hay nada) · 400 |
| GET | `/products/{sku}` | Producto por SKU | 200 · 404 |
| GET | `/products?page=&size=` | Listado paginado ordenado por SKU | 200 · 400 |
| POST | `/audits` | Registrar auditoría (idempotente por `clientAuditId`) | 201 nueva · 200 ya existía · 404 · 422 |
| POST | `/audits/batch` | Sincronización offline (máx. 200) | 200 `{accepted, duplicates, rejected}` · 422 |
| GET | `/audits?sku=&status=&from=&to=&page=&size=` | Consulta para supervisión (por `recordedAt` desc) | 200 · 400 · 422 |
| GET | `/audits/{id}` | Auditoría por id | 200 · 404 |
| POST | `/audits/{id}/approve` | Aplica `countedQuantity` al stock | 200 · 404 · 409 |
| POST | `/audits/{id}/reject` | Descarta la discrepancia | 200 · 404 · 409 |

Estados de una auditoría: `CONFIRMED` (coincide), `PENDING_REVIEW` (discrepancia sin revisar),
`APPROVED` y `REJECTED`. Los errores siguen RFC 7807 con `type`, `title`, `status` y `detail`.

### Ejemplos con curl

```bash
# Health (sin clave)
curl http://localhost:8080/api/v1/health

# Búsqueda por voz: "C J A cero cero uno" → CJA-001
curl -H "X-Api-Key: echostock-dev-key" -G http://localhost:8080/api/v1/products/search \
     --data-urlencode "q=c j a cero cero uno"

# Producto por SKU
curl -H "X-Api-Key: echostock-dev-key" http://localhost:8080/api/v1/products/CJA-001

# Registrar una discrepancia (repetirla devuelve 200 con la misma auditoría)
curl -X POST http://localhost:8080/api/v1/audits \
     -H "X-Api-Key: echostock-dev-key" -H "Content-Type: application/json" \
     -d '{"clientAuditId":"6f1c3e2a-7d0b-4c55-9a8e-1f2b3c4d5e6f","sku":"CJA-001","operatorCode":"OP-07",
          "deviceId":"wear-emulator-01","systemQuantity":50,"countedQuantity":48,
          "result":"DISCREPANCY","recordedAt":"2026-11-09T18:31:12Z"}'

# Sincronizar un lote offline
curl -X POST http://localhost:8080/api/v1/audits/batch \
     -H "X-Api-Key: echostock-dev-key" -H "Content-Type: application/json" \
     -d '{"audits":[{"clientAuditId":"0e464e67-1042-4109-b7a7-9ee2bddb81ff","sku":"FLM-001",
          "operatorCode":"OP-07","deviceId":"wear-emulator-01","systemQuantity":64,
          "countedQuantity":64,"result":"MATCH","recordedAt":"2026-11-09T18:40:00Z"}]}'

# Discrepancias pendientes y aprobación por el supervisor
curl -H "X-Api-Key: echostock-dev-key" "http://localhost:8080/api/v1/audits?status=PENDING_REVIEW"
curl -X POST -H "X-Api-Key: echostock-dev-key" http://localhost:8080/api/v1/audits/1/approve
```

### Decisiones de interpretación del contrato

- `result` es opcional en la petición: si falta, lo calcula el servidor; si llega y no cuadra, 422.
  Un valor distinto de `MATCH`/`DISCREPANCY` también es 422.
- Cuerpo JSON ilegible → 400; cuerpo válido con datos incorrectos → 422; parámetros de consulta
  inválidos (`limit` fuera de 1–10, `status` desconocido, fecha mal formada) → 400.
- `POST /audits` devuelve además la cabecera `Location: /api/v1/audits/{id}` (se añadió `GET /audits/{id}`).
- `clientAuditId` se compara sin distinguir mayúsculas; en el lote los ids se devuelven tal como se enviaron.
  Si el mismo id aparece dos veces en un lote, la primera se acepta y la segunda va a `duplicates`.
- Lote vacío → 200 con las tres listas vacías; más de 200 auditorías o sin `audits` → 422.
- En `GET /audits`, `from` es inclusivo y `to` exclusivo (sobre `recordedAt`).
