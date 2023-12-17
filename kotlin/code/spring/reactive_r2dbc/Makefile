COMPOSE=docker-compose.yml

.PHONY: db
db:
	docker-compose -f ${COMPOSE} up --wait -d db

.PHONY: run
run:
	./gradlew bootRun --stacktrace
