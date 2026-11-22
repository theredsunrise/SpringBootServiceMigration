package com.example.servicea.shared.util;

import org.springframework.util.function.ThrowingSupplier;

import java.util.concurrent.locks.ReentrantLock;

public final class Utils {

    private Utils() {
        throw new UnsupportedOperationException("Utility class.");
    }

    public static <O> O sync(ReentrantLock lock, ThrowingSupplier<O> closure) {
        try {
            lock.lock();
            return closure.get();
        } finally {
            lock.unlock();
        }
    }
}



