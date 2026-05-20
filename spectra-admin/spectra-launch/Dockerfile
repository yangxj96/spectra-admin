# =========================================
# Stage 1: Builder
# =========================================
FROM eclipse-temurin:25-jdk AS builder

WORKDIR /app

# 复制 Spring Boot jar
ARG JAR_FILE
COPY target/${JAR_FILE} app.jar

# 使用 layertools 分层解压
RUN java -Djarmode=layertools -jar app.jar extract

# =========================================
# Stage 2: Minimal Runner
# =========================================
#FROM eclipse-temurin:25-jre-jammy
FROM eclipse-temurin:25-jre

WORKDIR /app

# 设置时区
ENV TZ=Asia/Shanghai
ENV JAVA_TOOL_OPTIONS="-Djava.awt.headless=true"

## 安装必要字体（AWT headless 渲染验证码需要）
#RUN apt-get update && \
#    apt-get install -y --no-install-recommends \
#        fonts-dejavu-core \
#        fonts-dejavu-extra \
#        fontconfig \
#    && rm -rf /var/lib/apt/lists/*
#
## 删除不必要的 JRE 文件，进一步减小镜像
#RUN rm -rf /usr/lib/jvm/*/demo \
#           /usr/lib/jvm/*/man \
#           /usr/lib/jvm/*/sample \
#           /usr/lib/jvm/*/lib/missioncontrol \
#           /usr/lib/jvm/*/lib/visualvm \
#           /usr/lib/jvm/*/lib/src.zip \
#           /usr/share/doc \
#           /usr/share/man

# 复制 Spring Boot 分层
COPY --from=builder /app/dependencies/ ./dependencies/
COPY --from=builder /app/spring-boot-loader/ ./spring-boot-loader/
COPY --from=builder /app/snapshot-dependencies/ ./snapshot-dependencies/
COPY --from=builder /app/application/ ./application/

# 暴露端口
EXPOSE 8888

# 启动命令
#ENTRYPOINT ["java","-Dspring.profiles.active=prod","-cp","spring-boot-loader/*:dependencies/*:snapshot-dependencies/*:application/","org.springframework.boot.loader.JarLauncher"]
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "org.springframework.boot.loader.JarLauncher"]
