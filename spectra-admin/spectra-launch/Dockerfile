# =========================================
# Stage 1: Builder
# =========================================
FROM eclipse-temurin:25-jdk AS builder

WORKDIR /app

# 复制 Spring Boot jar
ARG JAR_FILE
COPY target/${JAR_FILE} app.jar

# 提前验证 JAR 文件存在
RUN if [ ! -f app.jar ]; then echo "ERROR: app.jar not found!" >&2; exit 1; fi

# 使用 layertools 分层解压
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher

# 验证解压结果
RUN for dir in dependencies spring-boot-loader snapshot-dependencies application; do \
        if [ ! -d "$dir" ]; then echo "ERROR: $dir not found after extract!" >&2; exit 1; fi; \
    done && echo "Builder stage: All layers exist ✔"

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
