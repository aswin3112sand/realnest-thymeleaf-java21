# RealNest – Spring Boot Real Estate App

- Spring Boot + Maven + Thymeleaf
- Browse properties, add listings, admin dashboard, etc.
- Ready to deploy on Render (Java runtime).

## Run locally

```bash
mvn spring-boot:run
```

### Build

```bash
mvn clean package
```

## Profiles & database configuration

- `default` profile (active when `SPRING_PROFILES_ACTIVE` is unset or set to `default`) runs entirely on an in-memory H2 database, so the app boots even without an external JDBC URL.
- `prod` profile targets MySQL. Provide `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD` (for example: `jdbc:mysql://host:3306/realnest?allowPublicKeyRetrieval=true&useSSL=true&serverTimezone=UTC`) before starting the JVM. The helper `DatabaseUrlEnvironmentPostProcessor` also maps common single-string environment variables such as `DATABASE_URL`, `JAWSDB_URL`, or `CLEARDB_DATABASE_URL` into the required Spring properties automatically.  
  `SPRING_PROFILES_ACTIVE=prod SPRING_DATASOURCE_URL=... SPRING_DATASOURCE_USERNAME=... SPRING_DATASOURCE_PASSWORD=... java -jar target/realnest-0.0.1-SNAPSHOT.jar`
- Both profiles share the same Cloudinary and Swagger configuration; override `CLOUDINARY_*` env vars in production.
- If you need persistent data on Render or any other host, make sure to switch to the `prod` profile and point the datasource to a managed MySQL instance.

### Why the app reports `Communications link failure`

- This error means the container cannot reach the MySQL host specified by `SPRING_DATASOURCE_URL`. `localhost` or `127.0.0.1` never point to your Render database from inside a container.
- Ensure your MySQL provider allows inbound traffic from the Render service (for Render private services, use the `<service-name>` hostname; for hosted providers like Planetscale or RDS, allow public access or a dedicated ingress IP).
- Set the three `SPRING_DATASOURCE_*` variables (or one of the supported `DATABASE_URL` style variables) in the Render dashboard before deploying. You can also test locally with:  
  `SPRING_PROFILES_ACTIVE=prod SPRING_DATASOURCE_URL='jdbc:mysql://your-host:3306/realnest?allowPublicKeyRetrieval=true&useSSL=true&serverTimezone=UTC' SPRING_DATASOURCE_USERNAME=youruser SPRING_DATASOURCE_PASSWORD=yourpass mvn spring-boot:run`
- When the variables point to a reachable DB, the boot logs will no longer show SQLState `08S01`.

## Deploy on Render

1. Commit and push the latest code to GitHub.
2. Configure Render environment variables for database access, Cloudinary media keys, and any other secrets.
3. **Build Command**
   - Preferred: `./mvnw clean package -DskipTests`
   - Fallback (without the wrapper): `mvn clean package -DskipTests`
4. **Start Command**
   - `java -jar target/realnest-0.0.1-SNAPSHOT.jar`
5. If the artifact version changes, adjust the jar name above to match the value generated in `target/`.
