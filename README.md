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
### Run test
```shell
sbt test
```