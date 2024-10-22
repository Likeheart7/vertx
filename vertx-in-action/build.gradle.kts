plugins {
    java
}

group = "com.chenx"
version = "1.0.0"

repositories {
    // 镜像源
    maven { url = uri("https://maven.aliyun.com/repository/public/") }
    maven { url = uri("https://maven.aliyun.com/repositories/jcenter") }
    mavenCentral()
}

dependencies {
    implementation("io.vertx:vertx-core:4.5.10")    // vertx核心包
    implementation("ch.qos.logback:logback-classic:1.2.3") // 提供SLF4J和logback实现， 1.4.12的版本出现打印不出日志的情况
    // 尝试集群部署需要添加集群管理器依赖
    implementation("io.vertx:vertx-hazelcast:4.5.10")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.create<JavaExec>("run") {
    // 确保vert.x本身也使用slf4j记录日志
    systemProperties["vertx.logger-delegate-factory-class-name"] = "io.vertx.core.logging.SLF4JLogDelegateFactory"
}