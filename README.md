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

## Deploy on Render

1. Commit and push the latest code to GitHub.
2. Configure Render environment variables for database access, Cloudinary media keys, and any other secrets.
3. **Build Command**
   - Preferred: `./mvnw clean package -DskipTests`
   - Fallback (without the wrapper): `mvn clean package -DskipTests`
4. **Start Command**
   - `java -jar target/realnest-0.0.1-SNAPSHOT.jar`
5. If the artifact version changes, adjust the jar name above to match the value generated in `target/`.
