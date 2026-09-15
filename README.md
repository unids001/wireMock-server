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

Run the Colombian restaurant server:

```bash
mvn exec:java -Dexec.args="--root=wiremock/colombian-restaurant"
```

Environment variables are also supported:

```bash
MOCK_SERVER_PORT=9090 mvn exec:java
```

To point the server at a different WireMock data root:

```bash
MOCK_SERVER_ROOT=/path/to/wiremock mvn exec:java
```

By default the server uses `wiremock/` as its data root. To run the Colombian restaurant mocks, point the root at `wiremock/colombian-restaurant`.

## Package a runnable JAR

```bash
mvn clean package
java -jar target/wiremock-server-1.0.0-SNAPSHOT.jar
```

## Shared endpoints (`wiremock/`)

These are the default stubs that live under `wiremock/mappings` and `wiremock/__files`.

- `GET /health`
- `GET /api/demo/hello`
- `POST /api/demo/echo`
- WireMock admin API: `http://localhost:<port>/__admin/`

## Colombian restaurant endpoints (`wiremock/colombian-restaurant/`)

Run these with:

```bash
mvn exec:java -Dexec.args="--root=wiremock/colombian-restaurant"
```

- `GET /api/restaurant/menu`
- `GET /api/restaurant/menu/categories`
- `GET /api/restaurant/menu/{id}`
- `POST /api/restaurant/cart`
- `POST /api/restaurant/orders`
- `GET /api/restaurant/orders/{id}`
- `GET /api/restaurant/orders/{id}/items`
- `POST /api/restaurant/orders/{id}/confirm`
- `POST /api/restaurant/orders/{id}/prepare`
- `POST /api/restaurant/orders/{id}/ready`
- `POST /api/restaurant/orders/{id}/deliver`
- `POST /api/restaurant/orders/{id}/cancel`
- `GET /api/restaurant/tables`

## Dynamic behavior

- Menu item paths vary by dish id.
- `POST /api/restaurant/cart` and `POST /api/restaurant/orders` vary by JSON body and return `400` or `422` for invalid payloads.
- Order lifecycle endpoints use scenario state for `confirm`, `prepare`, `ready`, `deliver`, and `cancel`.

## Call it from another project

```bash
curl http://localhost:8080/health
curl http://localhost:8080/api/demo/hello
curl -X POST http://localhost:8080/api/demo/echo
curl http://localhost:8080/api/restaurant/menu
curl http://localhost:8080/api/restaurant/menu/categories
curl -X POST http://localhost:8080/api/restaurant/orders
curl -X POST http://localhost:8080/api/restaurant/cart
```

## Add new mocks

For the shared server:

1. Add a new JSON mapping in `wiremock/mappings`.
2. If the response body is external, add the file in `wiremock/__files`.
3. Restart the server.

For the Colombian restaurant server:

1. Add a new JSON mapping in `wiremock/colombian-restaurant/mappings`.
2. If the response body is external, add the file in `wiremock/colombian-restaurant/__files`.
3. Restart the server with the matching `--root` value.

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
