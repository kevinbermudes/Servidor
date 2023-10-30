package com.example.demo.common.models;



import java.util.concurrent.atomic.AtomicLong;

public class IdGenerator {

    private static final IdGenerator instance = new IdGenerator();

    private AtomicLong currentId = new AtomicLong(0);

    private IdGenerator() {}

    public static IdGenerator getInstance() {
        return instance;
    }

    public long generateId() {
        return currentId.incrementAndGet();
    }
}
