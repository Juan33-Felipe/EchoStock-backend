# EchoStock — Contrato de API v1

Contrato compartido entre `EchoStock-backend` (servidor) y `EchoStock-WearApp` (reloj Wear OS).
Cualquier cambio aquí debe reflejarse en ambos repos.

- Base URL: `http://<host>:8080/api/v1` (desde el emulador Android el host es `10.0.2.2`).
- Formato: JSON UTF-8. Fechas en ISO-8601 UTC (`2026-11-09T18:30:00Z`).
- Autenticación: cabecera `X-Api-Key: <clave>` en todas las rutas `/api/**`.
  Clave de desarrollo: `echostock-dev-key`. Sin clave o clave incorrecta → `401`.
- Errores: cuerpo [RFC 7807](https://www.rfc-editor.org/rfc/rfc7807) (`application/problem+json`)
  con `type`, `title`, `status`, `detail`.

## Modelo

### Product

```json
{
  "sku": "CJA-001",
  "name": "Cajas tipo A",
  "locationCode": "A-01-03",
  "unit": "cajas",
  "systemQuantity": 50,
  "updatedAt": "2026-11-09T18:30:00Z"
}
```

- `sku`: único, mayúsculas, formato libre corto (`[A-Z0-9-]{3,20}`).
- `locationCode`: pasillo-estante-nivel, ej. `A-01-03`.
- `unit`: palabra en plural que se lee por voz ("cajas", "unidades", "rollos").

### Audit

```json
{
  "clientAuditId": "6f1c3e2a-7d0b-4c55-9a8e-1f2b3c4d5e6f",
  "sku": "CJA-001",
  "operatorCode": "OP-07",
  "deviceId": "wear-emulator-01",
  "systemQuantity": 50,
  "countedQuantity": 48,
  "result": "DISCREPANCY",
  "recordedAt": "2026-11-09T18:31:12Z"
}
```

- `clientAuditId`: UUID generado en el reloj. **Clave de idempotencia**: reenviar el mismo
  id nunca crea un duplicado (necesario para el modo offline, RF-006).
- `systemQuantity`: lo que el reloj leyó al operario (puede estar desactualizado si se
  auditó offline).
- `result`: `MATCH` si `countedQuantity == systemQuantity`, si no `DISCREPANCY`.
  El servidor lo recalcula y rechaza la auditoría si no cuadra (`422`).
- `recordedAt`: momento en que el operario confirmó en el reloj (no el de sincronización).

Campos que añade el servidor en las respuestas:

```json
{
  "id": 123,
  "status": "CONFIRMED",
  "receivedAt": "2026-11-09T18:35:00Z",
  "difference": -2
}
```

- `status`: `CONFIRMED` (coincide), `PENDING_REVIEW` (discrepancia sin revisar),
  `APPROVED` (supervisor aplicó el ajuste al stock), `REJECTED` (supervisor la descartó).
- `difference`: `countedQuantity - systemQuantity`.

## Endpoints

### `GET /products/search?q={texto}&limit={n}`

Búsqueda tolerante a transcripciones de voz (RF-001). `q` puede ser nombre, SKU o
código de ubicación tal como lo entendió el reconocedor de voz, por ejemplo:

| Dicho por el operario | Transcripción típica | Debe encontrar |
|---|---|---|
| "cajas tipo A" | `cajas tipo a` | `CJA-001` |
| "C J A cero cero uno" | `c j a cero cero uno` | `CJA-001` |
| "pasillo A uno tres" | `a 1 3` / `a uno tres` | productos en `A-01-03` |

Normalización (servidor): minúsculas, sin tildes, números en palabras → dígitos
("cero".."veinte", "treinta".."noventa", "cien"), letras sueltas unidas, guiones/espacios
ignorados al comparar códigos. Ranking: coincidencia exacta de SKU > ubicación exacta >
nombre por similitud.

- `limit`: 1–10, por defecto 5.
- `200` → `{ "query": "...", "normalized": "...", "results": [ { "product": Product, "score": 0.0-1.0, "matchedBy": "SKU|LOCATION|NAME" } ] }`
  ordenado por `score` descendente. Lista vacía si no hay nada (no `404`).

### `GET /products/{sku}`

- `200` → `Product`. `404` si no existe.

### `GET /products?page=&size=`

Listado paginado (para la lista de respaldo del reloj y para supervisión).
- `200` → `{ "content": [Product], "page": 0, "size": 20, "totalElements": 42, "totalPages": 3 }`

### `POST /audits`

Registro en tiempo real (RF-003, RF-004, RF-005). Cuerpo: `Audit` (campos del cliente).

- `201` → Audit completo (primera vez).
- `200` → Audit existente (mismo `clientAuditId` ya recibido; idempotente).
- `404` SKU inexistente · `422` validación (`countedQuantity < 0`, `result` incoherente…).

### `POST /audits/batch`

Sincronización offline (RF-006). Hasta 200 auditorías por petición.

```json
{ "audits": [ Audit, Audit ] }
```

- `200` →
```json
{
  "accepted":   ["uuid-1"],
  "duplicates": ["uuid-2"],
  "rejected":   [ { "clientAuditId": "uuid-3", "reason": "SKU no encontrado" } ]
}
```
El reloj borra de su cola local los `accepted` y `duplicates`; los `rejected` se marcan
como fallidos y no se reintentan.

### `GET /audits?sku=&status=&from=&to=&page=&size=`

Consulta para supervisión. Paginado como `/products`, ordenado por `recordedAt` desc.

### `POST /audits/{id}/approve` · `POST /audits/{id}/reject`

Solo para auditorías `PENDING_REVIEW` (si no, `409`).
- `approve`: fija `product.systemQuantity = countedQuantity` y `status = APPROVED`.
- `reject`: `status = REJECTED`, el stock no cambia.
- `200` → Audit actualizado.

### `GET /health` (sin API key)

`200` → `{ "status": "UP" }`. El reloj lo usa para saber si hay conexión con el servidor.
