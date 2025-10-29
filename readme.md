Java Migration Tool (API + CLI)

Quick start

1) Build all modules
   - mvn -q -DskipTests package

2) Run the API server (Spring Boot)
   - cd server && mvn spring-boot:run
   - Swagger UI: http://localhost:8080/api/v1/docs
   - OpenAPI JSON: http://localhost:8080/api/v1/openapi.json

3) Use the CLI
   - java -jar cli/target/cli-0.1.0-SNAPSHOT-shaded.jar analyze --source-path ./my-app --target-path ./out --target-java 17
   - java -jar cli/target/cli-0.1.0-SNAPSHOT-shaded.jar plan --source ./my-app --target ./out --target-java 17
   - java -jar cli/target/cli-0.1.0-SNAPSHOT-shaded.jar migrate --source ./my-app --target ./migrated --target-java 17 --backup true

Notes
- Source/target folders are mandatory for analysis and migration (no git operations).
- Deterministic rule engine with example rules: HttpURLConnection→HttpClient, Spring Boot property key updates.