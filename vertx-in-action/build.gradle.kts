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
    ext {
        set("vertxVersion", "4.5.10")
        set("junitVersion", "5.10.2")
    }
    implementation("io.vertx:vertx-core:${ext.get("vertxVersion")}")    // vertx核心包
    implementation("io.vertx:vertx-web-client:${ext.get("vertxVersion")}")  // vertx web依赖包
    implementation("io.vertx:vertx-rx-java:${ext.get("vertxVersion")}")   // 集成RxJava
    // vertx生成代理类的依赖
    implementation("io.vertx:vertx-service-proxy:${ext.get("vertxVersion")}")
    implementation("io.vertx:vertx-codegen:${ext.get("vertxVersion")}")
    // 尝试集群部署需要添加集群管理器依赖
    implementation("io.vertx:vertx-hazelcast:${ext.get("vertxVersion")}")
    implementation("ch.qos.logback:logback-classic:1.2.3") // 提供SLF4J和logback实现， 1.4.12的版本出现打印不出日志的情况
    // 注解处理的范围
    annotationProcessor("io.vertx:vertx-service-proxy:${ext.get("vertxVersion")}")
    annotationProcessor("io.vertx:vertx-codegen:${ext.get("vertxVersion")}:processor")
    // 测试
    testImplementation("io.vertx:vertx-junit5:${ext.get("vertxVersion")}")
    testImplementation("org.assertj:assertj-core:3.11.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${ext.get("junitVersion")}")

}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.create<JavaExec>("run") {
    // 确保vert.x本身也使用slf4j记录日志
    systemProperties["vertx.logger-delegate-factory-class-name"] = "io.vertx.core.logging.SLF4JLogDelegateFactory"
}

// 定义代理文件生成的位置，之后编译Java时，vertx-service-proxy会生成代理类
tasks.getByName<JavaCompile>("compileJava") {
    options.annotationProcessorGeneratedSourcesDirectory = File("$projectDir/src/main/java/com/chenx/chapter06/generated")
}

// 在Gradle中启用junit5支持
tasks.named<Test>("test") {
    useJUnitPlatform()
}