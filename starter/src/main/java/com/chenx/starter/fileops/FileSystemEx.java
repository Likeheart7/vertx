package com.chenx.starter.fileops;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;
import io.vertx.core.file.OpenOptions;

import static com.chenx.starter.constant.Constants.RESOURCE_PATH;


public class FileSystemEx extends AbstractVerticle {

  public static void main(String[] args) {
    Vertx.vertx().deployVerticle(new FileSystemEx());
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    final String filePath = RESOURCE_PATH + "///example.txt";
    FileSystem fileSystem = vertx.fileSystem();
    fileSystem.createFile(filePath)   // 创建文件
      .compose(v -> fileSystem.open(filePath, new OpenOptions())) // 创建成功打开文件
      // 写入内容，读取内容并打印
      .compose(
        asyncFile -> asyncFile.write(Buffer.buffer("hello vertx."))
          .onSuccess(v -> asyncFile.read(Buffer.buffer(), 0, 0, 16).onSuccess(System.out::println))
      )
      .onFailure(event -> System.out.println(event.getMessage())); // 失败回调
  }
}
