### Start PostgreSQL
docker compose up -d

### Run the complete test suite
./mvnw clean verify

### Start the application
./mvnw spring-boot:run

### Stop PostgreSQL
docker compose down

### Stop process port
kill -9 $(lsof -t -i:8080)