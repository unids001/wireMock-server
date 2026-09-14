# Standalone WireMock Server

Standalone WireMock-based mock server that can be run independently and consumed by other demo projects over HTTP.

## Requirements

- Java 17+
- Maven 3.9+

## Run it

```bash
mvn exec:java
```

Or pass a port:

```bash
mvn exec:java -Dexec.args="--port=9090"
```

Environment variables are also supported:

```bash
MOCK_SERVER_PORT=9090 mvn exec:java
```

To point the server at a different WireMock data root:

```bash
MOCK_SERVER_ROOT=/path/to/wiremock mvn exec:java
```

## Package a runnable JAR

```bash
mvn clean package
java -jar target/wiremock-server-1.0.0-SNAPSHOT.jar
```

## Available endpoints

- `GET /health`
- `GET /api/demo/hello`
- `POST /api/demo/echo`
- WireMock admin API: `http://localhost:<port>/__admin/`

## Call it from another project

```bash
curl http://localhost:8080/health
curl http://localhost:8080/api/demo/hello
curl -X POST http://localhost:8080/api/demo/echo
```

## Add new mocks

1. Add a new JSON mapping in `wiremock/mappings`.
2. If the response body is external, add the file in `wiremock/files`.
3. Restart the server.

Example mapping:

```json
{
  "request": { "method": "GET", "urlPath": "/api/demo/example" },
  "response": {
    "status": 200,
    "headers": { "Content-Type": "application/json" },
    "bodyFileName": "example.json"
  }
}
```
