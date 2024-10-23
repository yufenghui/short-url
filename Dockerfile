# 一阶段构建
FROM maven:3.5.0-jdk-8-alpine AS builder

WORKDIR /project

# 复制目录下所有源码到镜像中/project目录下
COPY . /project


# 打包应用
# COPY settings.xml /usr/share/maven/conf/
RUN mvn package -Dmaven.test.skip=true


# 二阶段构建
FROM openjdk:8-jre

VOLUME /tmp

# 应用路径
ENV APP_PATH=/project/short-url-application/target/short-url-application.jar

# 设置时区
ENV TZ=Asia/Shanghai
RUN set -eux; \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime; \
    echo $TZ > /etc/timezone

# 更换源
RUN sed -i s/deb.debian.org/mirrors.aliyun.com/g /etc/apt/sources.list

# 安装基础软件
RUN apt-get update \
&&  apt-get install -y \
    vim procps wget curl \
    telnet inetutils-ping iproute2 net-tools

# 添加应用jar
COPY --from=builder $APP_PATH /app.jar

CMD exec java $JAVA_OPTS -jar /app.jar
