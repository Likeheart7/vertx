package com.chenx.starter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Multiplexing {
    // 使用eventLoop事件循环
    public static void main(String[] args) {
        EventLoop eventLoop = new EventLoop();
        new Thread(() -> {
            for (int i = 0; i < 6; i++) {
                delay(1000);
                eventLoop.dispatch(new EventLoop.Event("tick", i));
            }
            eventLoop.dispatch(new EventLoop.Event("stop", null));
        }).start();
        new Thread(()->{
            delay(2500);
            eventLoop.dispatch(new EventLoop.Event("hello", "美丽新世界"));
            delay(800);
            eventLoop.dispatch(new EventLoop.Event("hello", "beautiful universe"));
        }).start();
        eventLoop.dispatch(new EventLoop.Event("hello", "world"));
        eventLoop.dispatch(new EventLoop.Event("foo", "bar"));
        eventLoop
            .on("hello", s-> System.out.println("hello " + s))
            .on("tick", n-> System.out.println("tick #" + n))
            .on("stop", v->eventLoop.stop())
            .run();
        System.out.println("Bye!!");
    }

    private static void delay(int mills) {
        try {
            TimeUnit.MILLISECONDS.sleep(mills);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static final class EventLoop {
        private final ConcurrentLinkedDeque<Event> events = new ConcurrentLinkedDeque<>();
        private final ConcurrentHashMap<String, Consumer<Object>> handlers = new ConcurrentHashMap<>();

        public EventLoop on(String key, Consumer<Object> handler) {
            handlers.put(key, handler);
            return this;
        }

        public void dispatch(Event event) {
            events.add(event);
        }

        public void stop() {
            Thread.currentThread().interrupt();
        }

        public void run() {
            while (!(events.isEmpty() && Thread.interrupted())) {
                if (!events.isEmpty()) {
                    Event event = events.pop();
                    if (handlers.containsKey(event.key)) {
                        handlers.get(event.key).accept(event.data);
                    } else {
                        System.err.println("No handler for key " + event.key);
                    }
                }
            }
        }

        public static final class Event {
            private final String key;
            private final Object data;

            public Event(String key, Object data) {
                this.key = key;
                this.data = data;
            }
        }
    }
}
