# Ktor Sample App

Ktor sample app to follow a modified/personal version of a Kotlin Training Track, with the goal of verifying the solution development.

## ✅ Tasks
### Assignment 1
- [x] Set up the project
- [x] Add ktor server dependencies (core & engine)
- [x] Create a health check endpoint
- [x] Define Gradle task to build a Docker image

### Assignment 2
- [x] Type-safe application configuration
- [ ] Type-safe content negotiation / serializers for health check

## 📘 Quick Start

### Local Development

#### Running

1. Run Postgres through `docker-compose`
2. Start the application through Gradle

```shell
docker compose -f docker-compose.yml -f docker-compose.local.yml up -d
./gradlew run
```

### Running the application in Docker

1. Run Postgres through `docker-compose`
2. Start the application inside a Docker container

```shell
docker compose -f docker-compose.yml -f docker-compose.local.yml up -d
./gradlew runDocker
```

#### Building

Builds a project's Docker image to a tarball. This task generates a jib-image.tar file in the build directory:

```shell
./gradlew buildImage
```

More info. [here](https://ktor.io/docs/docker.html#tasks)