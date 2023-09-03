# API Gateway Service

An API gateway is a software that connects clients and backend services, and performs various tasks such as routing, caching, authentication and authorization.
## Table of Content 
- #### [Features](#features) 
- #### [Building](#building)
  - #### [Prerequisites]()
  - #### [Installation]()
- #### [Running](#running)
  - #### [Prerequisites]()
  - #### [Configuration]()
  - #### [Installation]()
- #### [Dependencies](#dependencies)
- #### [Contributing](#contributing)
- #### [Licences](#licences)
## Features
- Caching the token and role to increase performance
- Routing the request to appropriate downstream services
- Performing authentication and authorization
## Building
 ### Prerequisite
Before you begin, ensure you have met the following requirements:
- Java Development Kit (JDK) version 17.0.2 installed
- Gradle version 8.2 installed
- Familiarity with Spring Cloud concepts
### Installation
1. Clone the repository.
  ```bash
  git clone https://gitlab.tma.com.vn/vs-path-tool/backend/api-gateway
  ```
2. Navigate to the project directory.
  ```bash
  cd api-gateway
  ```
3. Build the project using Gradle.

  ```bash
  gradle build
  ```
## Running
### Prerequisites 
- Java Runtime Environment (JRE) version 17.0.2 installed
- Redis version 7.0.12 installed
- Apache kafka version 2.12-3.5.0 installed on standalone server
### Configuration 
In application.properties file, change the configuration host and port  of the servers:

```properties
#Eruka server
eureka.client.service-url.defaultZone=http://127.0.0.1:8761/eureka
# Redis
spring.cache.redis.username=dba_redis
spring.cache.redis.password=123456
spring.cache.redis.host=127.0.0.1
spring.cache.redis.port=6379
# Kafka
spring.kafka.bootstrap-servers=127.0.0.1:9092
```


## Dependencies
| Name                                                                 | Version  |
|----------------------------------------------------------------------|----------|
| org.springdoc:springdoc-openapi-starter-webflux-ui                   | 2.0.4    |
| org.springframework.cloud:spring-cloud-starter-gateway               | 4.0.6    |
| org.springframework.cloud:spring-cloud-starter-netflix-eureka-client | 4.0.2    |
| redis.clients:jedis                                                  | 4.3.0    |
| com.auth0:java-jwt                                                   | 4.4.0    |
| org.springframework.kafka:spring-kafka                               | 3.0.9    |
| org.projectlombok:lombok                                             | 1.18.28  |
| org.springframework.boot:spring-boot-starter-test                    | 3.1.1    |
| org.springframework.cloud:spring-cloud-dependencies                  | 2022.0.3 |
## Contributing

Pull requests are welcome. \
For major changes, please open an issue first to discuss what you would like to change. \
Please make sure to update tests as appropriate. 

## License
[MIT](https://choosealicense.com/licenses/mit/)