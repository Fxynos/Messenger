FROM openjdk:17-alpine
EXPOSE ${SERVER_PORT}
WORKDIR /app
COPY . /app/
RUN chmod +x ./gradlew && ./gradlew bootJar --full-stacktrace
CMD ["/bin/sh", "-c", "./gradlew update bootRun --full-stacktrace --args=\"--server.address=${SERVER_HOST} --server.port=${SERVER_PORT} --jwt.aes.key=${JWT_AES} --client.address=${CLIENT_HOST} --client.port=${CLIENT_PORT}\""]
