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
