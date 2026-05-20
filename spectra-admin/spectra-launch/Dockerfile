# =========================================
# Stage 1: Builder
# =========================================
FROM eclipse-temurin:25-jdk AS builder

WORKDIR /app

# 复制 Spring Boot jar
ARG JAR_FILE
COPY target/${JAR_FILE} app.jar

# 使用 layertools 分层解压
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --dest .

# 验证分成解压结果
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --dest . && \
    test -d dependencies && \
    test -d spring-boot-loader && \
    test -d application && \
    test -f application/BOOT-INF/classes/com/devops00/spectra/launch/LaunchApplication.class

# =========================================
# Stage 2: Minimal Runner
# =========================================
#FROM eclipse-temurin:25-jre-jammy
FROM eclipse-temurin:25-jre

WORKDIR /app

# 设置时区
ENV TZ=Asia/Shanghai
ENV JAVA_TOOL_OPTIONS="-Djava.awt.headless=true"

# 复制 Spring Boot 分层
COPY --from=builder /app/dependencies/ ./dependencies/
COPY --from=builder /app/spring-boot-loader/ ./spring-boot-loader/
COPY --from=builder /app/snapshot-dependencies/ ./snapshot-dependencies/
COPY --from=builder /app/application/ ./application/

# 暴露端口
EXPOSE 8888

# 启动命令
#ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "org.springframework.boot.loader.JarLauncher"]
ENTRYPOINT ["java","-cp","spring-boot-loader/*:dependencies/*:snapshot-dependencies/*:application/","org.springframework.boot.loader.JarLauncher","-Dspring.profiles.active=prod"]
