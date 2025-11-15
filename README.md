# RealNest – Thymeleaf Starter (Java 21)

Run:
1) Install Java 21 & Maven
2) `mvn spring-boot:run`
Open http://localhost:9090

Uses H2 for quick start. Switch to MySQL by editing `application.yml`.

Default admin login: `admin3112@gmail.com` / `nextnext`.

## Deploying on Render

1. Install the [Render CLI](https://render.com/docs/cli) and log in (`render login`).
2. From the project root run `render blueprint deploy render.yaml`.
3. The provided `render.yaml` builds the Docker image, runs `java -jar app.jar --server.port=$PORT`, and exposes the HTTP port Render assigns.
4. Customize environment variables in `render.yaml` (for example `SPRING_PROFILES_ACTIVE=prod` and any database secrets) before deploying.

Render automatically redeploys on pushes when `autoDeploy` is enabled in the blueprint.
