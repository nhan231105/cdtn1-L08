FROM node:24-alpine AS frontend-build
WORKDIR /frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

FROM nginx:1.31.8-alpine AS frontend
COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=frontend-build /frontend/dist /usr/share/nginx/html
EXPOSE 3000

FROM maven:3.9.16-eclipse-temurin-21-alpine AS backend-build
WORKDIR /workspace/backend
COPY backend/mvnw backend/pom.xml ./
COPY backend/.mvn/ .mvn/
RUN chmod +x mvnw && ./mvnw -B -DskipTests dependency:go-offline
COPY backend/src/ src/
RUN ./mvnw -B -DskipTests package

FROM backend-build AS backend-test
CMD ["./mvnw", "-B", "test"]

FROM eclipse-temurin:21-jre-alpine AS backend
WORKDIR /app
COPY --from=backend-build /workspace/backend/target/backend-0.0.1-SNAPSHOT.jar /app/app.jar
USER 10001:10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
