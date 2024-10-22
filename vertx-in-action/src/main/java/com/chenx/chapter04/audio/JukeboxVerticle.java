package com.chenx.chapter04.audio;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class JukeboxVerticle extends AbstractVerticle {
    private static final Logger log = LoggerFactory.getLogger(JukeboxVerticle.class);
    // 用于维护http响应，跟踪所有当前的流媒体
    private final Set<HttpServerResponse> streamers = new HashSet<>();

    private enum State {PLAYING, PAUSE}

    private State currentMode = State.PAUSE;
    // 保存了接下来要播放的所有预定曲目
    private final Queue<String> playlist = new ArrayDeque<>();
    private final String dirPath = "src/main/resources/static/audio/";

    // 用于限制传输速率
    private AsyncFile currentFile;
    private long positionInFile;

    @Override
    public void start() throws Exception {
        EventBus bus = vertx.eventBus();
        // 事件总线上传输的命令
        bus.consumer("jukebox.list", this::list);
        bus.consumer("jukebox.schedule", this::schedule);
        bus.consumer("jukebox.play", this::play);
        bus.consumer("jukebox.pause", this::pause);

        vertx.createHttpServer()
                .requestHandler(this::httpHandler)
                .listen(9527);

        // 通过一个定时任务，定期发送数据，防止发送过快，某个客户端本地缓存很多，而新加入的客户端从很后面的位置开始接收
        // 将他们稳定在一个合适的误差之间
        vertx.setPeriodic(10000, this::streamAudioChunk);
    }

    private void streamAudioChunk(Long id) {
        if (currentMode == State.PAUSE) {
            return;
        }
        if (currentFile == null && playlist.isEmpty()) {
            currentMode = State.PAUSE;
            return;
        }
        if (currentFile == null) {
            openNextFile();
        }
        // Vert.x Buffer一旦被写入就不能重复使用
        currentFile.read(Buffer.buffer(32), 0, positionInFile, 32, ar -> {
            if (ar.succeeded()) {
                // 将数据复制到所有播放器
                processReadBuffer(ar.result());
            } else {
                log.error("Read failed", ar.cause());
                closeCurrentFile();
            }
        });

    }

    /**
     * 打开下个文件
     */
    private void openNextFile() {
        OpenOptions opts = new OpenOptions().setRead(true);
        Future<AsyncFile> currentFile = vertx.fileSystem().open(dirPath + playlist.poll(), opts);
        positionInFile = 0; // 重置位置
    }


    /**
     * 关闭文件
     */
    private void closeCurrentFile() {
        positionInFile = 0;
        currentFile.close();
        currentFile = null;
    }


    /**
     * 将数据复制到所有播放器
     */
    private void processReadBuffer(Buffer buffer) {
        positionInFile += buffer.length();
        // 到达文件末尾，读完了
        if (buffer.length() == 0) {
            closeCurrentFile();
            return;
        }
        for (HttpServerResponse streamer : streamers) {
            // back pressure，防止写入过快
            if (!streamer.writeQueueFull()) {
                streamer.write(buffer.copy());   // Vert.x 缓冲区一旦被写入就不能重复使用
            }
        }
    }

    private void httpHandler(HttpServerRequest request) {
        if ("/".equals(request.path())) {
            openAudioStream(request);
            return;
        }
        if (request.path().startsWith("/download/")) {
            // 防止传入类似/download/etc/xxx恶意读取文件
            String sanitizedPath = request.path().substring(10).replaceAll("/", "");
            download(sanitizedPath, request);
            return;
        }
        // 不匹配，响应404并结束流
        request.response().setStatusCode(404).end();
    }

    /**
     * 下载文件
     */
    private void download(String sanitizedPath, HttpServerRequest request) {
        String file = dirPath + sanitizedPath;
        // 除非在一个网络文件系统上，否则可能的阻塞时间微不足道，因此避免嵌套回调级别。（原文注释，不理解）
        if (!vertx.fileSystem().existsBlocking(file)) {
            request.response().setStatusCode(404).end();
        }
        OpenOptions opts = new OpenOptions().setRead(true);
        vertx.fileSystem().open(file, opts, ar -> {
            if (ar.succeeded()) {
                downloadFile(ar.result(), request);
            } else {
                log.error("Read failed", ar.cause());
                request.response().setStatusCode(500).end();
            }
        });
    }

    /**
     * 下载文件
     * 会通过vertx用背压(back pressure)来控制流的交互速率
     */
    private void downloadFile(AsyncFile file, HttpServerRequest request) {
        HttpServerResponse response = request.response()
                .setStatusCode(200)
                .putHeader("Content-Type", "audio/mpeg")
                .setChunked(true);
//        file.handler(buffer -> {
//            response.write(buffer);
//            // 对读取速度和写出速度的平衡
//            // 如果写满了，就暂停读入流
//            if (response.writeQueueFull()) {
//                file.pause();
//                // 写缓冲刷出去之后，恢复读入
//                response.drainHandler(v -> file.resume());
//            }
//        });
//        // 文件结束，结束响应
//        file.endHandler(v -> response.end());

        // 上面的逻辑是：暂停源且不丢失任何数据，可以直接用pipeTo
        // 在可暂停的 ReadStream 和 WriteStream 之间进行复制时，管道会处理背压。 它还管理源流的结束和两个流上的错误
        file.pipeTo(response);
    }

    private void openAudioStream(HttpServerRequest request) {
        HttpServerResponse response = request.response()
                .putHeader("Content-Type", "audio/mpeg")
                .setChunked(true); // 分块传输
        streamers.add(request.response());
        // 流退出时，取消跟踪
        response.endHandler(v -> {
            streamers.remove(response);
            log.info("A streamer left.");
        });
    }


    /**
     * 列出可用文件
     */
    private void list(Message<?> req) {
        vertx.fileSystem().readDir(dirPath, ".*mp3$", ar -> {
            if (ar.succeeded()) {
                // 对每个匹配的文件创建File对象并获取文件名
                List<String> files = ar.result()
                        .stream()
                        .map(File::new)
                        .map(File::getName)
                        .collect(Collectors.toList());
                // 构建响应json并回复
                JsonObject json = new JsonObject().put("files", new JsonArray(files));
                req.reply(json);
            } else {
                log.error("readDir failed", ar.cause());
                // 通过事件总线在请求-回复的通信模式中，发送失败代码和错误消息
                req.fail(500, ar.cause().getMessage());
            }
        });
    }

    /**
     * 添加播放列表
     */
    private void schedule(Message<JsonObject> req) {
        String file = req.body().getString("file");
        if (playlist.isEmpty() && currentMode == State.PAUSE) {
            currentMode = State.PLAYING;
        }
        playlist.offer(file);   // 向播放列表添加新歌曲
    }

    /**
     * 播放
     */
    private void play(Message<?> req) {
        currentMode = State.PLAYING;
    }

    /**
     * 暂停
     */
    private void pause(Message<?> req) {
        currentMode = State.PAUSE;
    }
}
