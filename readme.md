# #Internet Banking Gateway Service

---

## Configurations property

#### #Redis configurations

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      timeout: 60000
```

#### #swagger configurations

```yaml
springdoc:
  api-docs:
    enabled: true
  swagger-ui:
    enabled: true
```

[http://localhost:7030/webjars/swagger-ui/index.html](http://localhost:7030/webjars/swagger-ui/index.html)

---

## Build

#### 1.Maven

```shell
mvn clean package -Dmaven.test.skip=true
```