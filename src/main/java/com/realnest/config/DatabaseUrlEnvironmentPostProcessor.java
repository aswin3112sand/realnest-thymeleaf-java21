package com.realnest.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.util.StringUtils;

/**
 * Allows the application to derive {@code SPRING_DATASOURCE_*} properties from common
 * single-string database URLs (e.g. {@code mysql://user:pass@host:3306/db}).
 * This is helpful on Render/Railway/Heroku style platforms where a JDBC URL is
 * supplied as one environment variable.
 */
public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

  private static final Logger log = LoggerFactory.getLogger(DatabaseUrlEnvironmentPostProcessor.class);
  private static final String PROPERTY_SOURCE_NAME = "databaseUrlOverrides";

  private static final List<String> URL_PROPERTY_CANDIDATES =
      List.of("SPRING_DATASOURCE_URL", "JDBC_DATABASE_URL", "DATABASE_URL",
          "CLEARDB_DATABASE_URL", "JAWSDB_URL", "PLANETSCALE_DATABASE_URL", "MYSQL_URL");

  @Override
  public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
    if (hasText(environment.getProperty("SPRING_DATASOURCE_URL"))) {
      return;
    }

    Optional<DatabaseCredentials> credentials =
        URL_PROPERTY_CANDIDATES.stream()
            .map(environment::getProperty)
            .filter(this::hasText)
            .map(this::parseDatabaseUrl)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();

    if (credentials.isEmpty()) {
      return;
    }

    DatabaseCredentials parsed = credentials.get();
    Map<String, Object> overrides = new LinkedHashMap<>();
    overrides.put("SPRING_DATASOURCE_URL", parsed.jdbcUrl());

    if (!hasText(environment.getProperty("SPRING_DATASOURCE_USERNAME")) && hasText(parsed.username())) {
      overrides.put("SPRING_DATASOURCE_USERNAME", parsed.username());
    }
    if (!hasText(environment.getProperty("SPRING_DATASOURCE_PASSWORD")) && parsed.password() != null) {
      overrides.put("SPRING_DATASOURCE_PASSWORD", parsed.password());
    }

    MutablePropertySources sources = environment.getPropertySources();
    sources.addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, overrides));
    log.info("Mapped database URL from environment into SPRING_DATASOURCE_* properties");
  }

  private Optional<DatabaseCredentials> parseDatabaseUrl(String rawUrl) {
    if (!hasText(rawUrl)) {
      return Optional.empty();
    }

    String value = rawUrl.trim();
    if (value.startsWith("jdbc:")) {
      return Optional.of(new DatabaseCredentials(value, null, null));
    }

    URI uri;
    try {
      uri = new URI(value);
    } catch (URISyntaxException ex) {
      log.warn("Skipping invalid database URL '{}': {}", value, ex.getMessage());
      return Optional.empty();
    }

    String scheme = uri.getScheme();
    if (scheme == null) {
      return Optional.empty();
    }

    String normalizedScheme = scheme.toLowerCase(Locale.ROOT);
    if (!normalizedScheme.equals("mysql") && !normalizedScheme.equals("mariadb")) {
      return Optional.empty();
    }

    String host = uri.getHost();
    if (!hasText(host)) {
      return Optional.empty();
    }
    int port = uri.getPort() > 0 ? uri.getPort() : 3306;
    String database = Optional.ofNullable(uri.getPath())
        .filter(path -> !path.isBlank() && !Objects.equals(path, "/"))
        .map(path -> path.startsWith("/") ? path.substring(1) : path)
        .orElse("");

    Map<String, String> parameters = parseQueryParameters(uri.getQuery());
    parameters.putIfAbsent("useSSL", "true");
    parameters.putIfAbsent("allowPublicKeyRetrieval", "true");
    parameters.putIfAbsent("serverTimezone", "UTC");

    String paramString = parameters.entrySet().stream()
        .map(entry -> entry.getKey() + "=" + entry.getValue())
        .collect(Collectors.joining("&"));

    StringBuilder jdbcUrl = new StringBuilder("jdbc:mysql://")
        .append(host)
        .append(":")
        .append(port)
        .append("/");

    jdbcUrl.append(database);
    if (StringUtils.hasText(paramString)) {
      jdbcUrl.append("?").append(paramString);
    }

    String username = null;
    String password = null;
    if (uri.getUserInfo() != null) {
      String[] parts = uri.getUserInfo().split(":", 2);
      username = decode(parts[0]);
      if (parts.length > 1) {
        password = decode(parts[1]);
      }
    }

    return Optional.of(new DatabaseCredentials(jdbcUrl.toString(), username, password));
  }

  private Map<String, String> parseQueryParameters(String query) {
    Map<String, String> parameters = new LinkedHashMap<>();
    if (!hasText(query)) {
      return parameters;
    }

    String[] pairs = query.split("&");
    for (String pair : pairs) {
      if (!pair.isEmpty()) {
        String[] keyValue = pair.split("=", 2);
        String key = decode(keyValue[0]);
        String value = keyValue.length > 1 ? decode(keyValue[1]) : "";
        if (StringUtils.hasText(key)) {
          parameters.put(key, value);
        }
      }
    }
    return parameters;
  }

  private String decode(String value) {
    return URLDecoder.decode(value, StandardCharsets.UTF_8);
  }

  private boolean hasText(String value) {
    return StringUtils.hasText(value);
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }

  private record DatabaseCredentials(String jdbcUrl, String username, String password) {
  }
}
