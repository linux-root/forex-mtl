# A local proxy for Forex rates


## Run locally

### 1. Start OneFrame service on port 8081
```shell
docker run -p 8081:8080 paidyinc/one-frame
```
### 2. Start FOREX rate serice on port 8080
```shell
sbt run
```
### 3. Send a sample request
```shell
curl --location 'http://localhost:8080/rates?from=USD&to=SGD'
```
## Run all Unit Tests, Property-based Tests
```shell
sbt test
```
