package com.ifsp.userservice.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CodigoCacheService {

    private static final long EXPIRATION_MILLIS = 5 * 60 * 1000L;
    private final Map<String, CodeEntry> cache = new ConcurrentHashMap<>();

    public void putCode(String email, String code) {
        cache.put(email, new CodeEntry(code, Instant.now().toEpochMilli()));
    }

    public Optional<String> getValidCode(String email) {
        CodeEntry entry = cache.get(email);
        if (entry == null) {
            return Optional.empty();
        }
        if (isExpired(entry)) {
            cache.remove(email);
            return Optional.empty();
        }
        return Optional.of(entry.code());
    }

    public void removeCode(String email) {
        cache.remove(email);
    }

    @Scheduled(fixedRate = 60000)
    public void purgeExpired() {
        cache.entrySet().removeIf(entry -> isExpired(entry.getValue()));
    }

    private boolean isExpired(CodeEntry entry) {
        return Instant.now().toEpochMilli() - entry.createdAt() > EXPIRATION_MILLIS;
    }

    private record CodeEntry(String code, long createdAt) {}
}