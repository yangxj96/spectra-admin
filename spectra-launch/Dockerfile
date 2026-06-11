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

# 设置时区[不设置时区,让他默认UTC+0.项目中做适配]
#ENV TZ=Asia/Shanghai
#RUN ln -sf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 复制 Spring Boot 分层
COPY --from=builder /layers/app/dependencies/ ./
COPY --from=builder /layers/app/spring-boot-loader/ ./
COPY --from=builder /layers/app/snapshot-dependencies/ ./
COPY --from=builder /layers/app/application/ ./

# 暴露端口
EXPOSE 8888

RUN ls -R

# 启动命令
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "org.springframework.boot.loader.launch.JarLauncher"]
