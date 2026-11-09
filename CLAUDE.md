# EchoStock — Backend

Servidor central (ERP/WMS simplificado) para la app de smartwatch EchoStock. Requisitos en `README.md`,
contrato de API en `docs/api-contract.md` (compartido con el repo `EchoStock-WearApp`).

## Stack
- Java 17, Spring Boot 3, Maven (wrapper `./mvnw`).
- Perfil por defecto `dev`: H2 en archivo, funciona sin Docker. Perfil `postgres`: PostgreSQL vía `docker compose`.
- Flyway para el esquema, springdoc-openapi para Swagger UI en `/swagger-ui.html`.

## Comandos
- Compilar y probar: `./mvnw verify`
- Ejecutar: `./mvnw spring-boot:run` (puerto 8080)
- Con PostgreSQL: `docker compose up -d` y `./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres`

## Reglas
- Cada cambio es un commit, imprescindible. Mensajes en español con Conventional Commits.
- Autor: Juan33-Felipe. Nunca añadir `Co-Authored-By` ni "Generated with Claude Code".
- Push solo cuando se pida.
