package com.example.servicea.shared.util;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public final class UUIDv7Generator {

    private static final AtomicLong LAST_TIMESTAMP = new AtomicLong(0L);
    private static final SecureRandom RANDOM = new SecureRandom();

    private UUIDv7Generator() {
        throw new UnsupportedOperationException("Utility class.");
    }

    public static UUID randomUUID() {
        long timestamp = LAST_TIMESTAMP.updateAndGet(last -> {
            long now = Instant.now().toEpochMilli();
            return (now > last) ? now : last + 1;
        });

        byte[] randomBytes = new byte[10];
        RANDOM.nextBytes(randomBytes);

        long msb = (timestamp << 16)
                | 0x7000L
                | ((randomBytes[0] & 0x0FL) << 8)
                | (randomBytes[1] & 0xFFL);

        long lsb = 0x8000000000000000L
                | ((randomBytes[2] & 0x3FL) << 56)
                | ((randomBytes[3] & 0xFFL) << 48)
                | ((randomBytes[4] & 0xFFL) << 40)
                | ((randomBytes[5] & 0xFFL) << 32)
                | ((randomBytes[6] & 0xFFL) << 24)
                | ((randomBytes[7] & 0xFFL) << 16)
                | ((randomBytes[8] & 0xFFL) << 8)
                | (randomBytes[9] & 0xFFL);

        return new UUID(msb, lsb);
    }
}