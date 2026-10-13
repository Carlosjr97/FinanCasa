# Uma única imagem com o site inteiro: o Spring Boot serve a API (/api) e também os arquivos do Angular.

# Etapa 1: gera os arquivos estáticos do Angular (HTML, JS e CSS).
FROM node:24-slim AS frontend
WORKDIR /app
COPY frontend/package.json frontend/package-lock.json ./
# npm install (e não npm ci): o package-lock gerado no Windows não lista algumas dependências opcionais do Linux, e o npm ci recusa instalar.
RUN npm install --no-audit --no-fund
COPY frontend/ ./
RUN npm run build

# Etapa 2: empacota o Spring Boot em um .jar, com o Angular dentro da pasta static.
FROM maven:3.9-eclipse-temurin-25 AS backend
WORKDIR /app
COPY backend/pom.xml ./
RUN mvn -B -q dependency:go-offline
COPY backend/src ./src
COPY --from=frontend /app/dist/frontend/browser ./src/main/resources/static
RUN mvn -B -q package -DskipTests

# Etapa 3: imagem final, só com o Java e o .jar.
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=backend /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
