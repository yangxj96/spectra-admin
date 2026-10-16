# =========================================
# Stage 1: 构建
# =========================================
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /layers
# 复制 Spring Boot jar
ARG JAR_FILE
COPY target/${JAR_FILE} app.jar
# 使用 layertools 分层解压
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher

# =========================================
# Stage 2: 轻量运行时
# =========================================
FROM eclipse-temurin:25-jre

WORKDIR /app

# 复制 Spring Boot 分层
COPY --from=builder /layers/app/dependencies/ ./
COPY --from=builder /layers/app/spring-boot-loader/ ./
COPY --from=builder /layers/app/snapshot-dependencies/ ./
COPY --from=builder /layers/app/application/ ./

# 暴露端口（与 application-dev.yml / application-prod.yml 的 server.port=4004 一致）
EXPOSE 4004

# 启动命令
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "org.springframework.boot.loader.launch.JarLauncher"]
