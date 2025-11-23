# 基础镜像：使用Java 25的JRE（文档技术栈要求Java 25，）
FROM eclipse-temurin:25-jre

# 设置工作目录
WORKDIR /app

# 将本地JAR包复制到容器中（注意：JAR包名称需与打包生成的一致，若不同需修改）
COPY  target/catalog-service-0.0.1-SNAPSHOT.jar  app.jar

# 暴露端口（文档要求catalog-service运行在8081端口，）
EXPOSE 8081

# 容器启动命令（执行JAR包）
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
# --spring.profiles.active=prod：指定使用prod环境配置（避免用本地开发配置，后续需在application-prod.yml中配置Docker环境的数据库连接）