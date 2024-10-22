package com.chenx.starter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel ;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;

public class NioEx {

    public static final HashMap<SocketChannel, Context> contexts = new HashMap<>();
    public static final Pattern QUIT = Pattern.compile("(\\r)?(\\n)?/quit$");

    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(8080));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT); // 注册accept事件
        while (true) {
            selector.select();
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            // 有事件产生
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                // 针对接收、可读、可写事件的处理
                if (key.isAcceptable()) {
                    newConnection(selector, key);
                } else if (key.isReadable()) {
                    echo(key);
                } else if (key.isWritable()) {
                    continueEcho(selector, key);
                }
                iter.remove(); // 需要手动移除键，否则下次循环他们会再次可用
            }
        }
    }

    private static void continueEcho(Selector selector, SelectionKey key) throws IOException{
        SocketChannel socketChannel = (SocketChannel) key.channel();
        Context context = contexts.get(socketChannel);
        try {
            int remainingBytes = context.buffer.limit() - context.buffer.position();
            int count = socketChannel.write(context.buffer);
            if (count == remainingBytes) {
                context.buffer.clear();
                key.cancel();
                if (context.terminating) {
                    cleanup(socketChannel);
                } else {
                    socketChannel.register(selector, SelectionKey.OP_READ);
                }
            }
        } catch (IOException err) {
            err.printStackTrace();
            cleanup(socketChannel);
        }
    }

    private static void echo(SelectionKey key) throws IOException{
        SocketChannel socketChannel = (SocketChannel) key.channel();
        Context context = contexts.get(socketChannel);
        try {
            socketChannel.read(context.buffer);
            context.buffer.flip();  // 翻转
            context.currentLine = context.currentLine + Charset.defaultCharset().decode(context.buffer);
            if (QUIT.matcher(context.currentLine).find()) {
                context.terminating = true;
            } else if (context.currentLine.length() > 16) {
                context.currentLine = context.currentLine.substring(8);
            }
            context.buffer.flip();
            int count = socketChannel.write(context.buffer);
            if (count < context.buffer.limit()) {
                key.cancel();
                socketChannel.register(key.selector(), SelectionKey.OP_WRITE);
            } else {
                context.buffer.clear();
                if (context.terminating) {
                    cleanup(socketChannel);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            cleanup(socketChannel);
        }
    }

    private static void cleanup(SocketChannel socketChannel) throws IOException {
        socketChannel.close();
        contexts.remove(socketChannel);
    }

    private static void newConnection(Selector selector, SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false)
            .register(selector, SelectionKey.OP_READ);
        contexts.put(socketChannel, new Context()); // 将连接状态放入map中维护
    }

    /**
     * 用于保持和TCP链接处理相关的状态
     */
    private static class Context {
        private final ByteBuffer buffer = ByteBuffer.allocate(512);
        private String currentLine = "";
        private boolean terminating = false;
    }
}
