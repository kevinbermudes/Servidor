package com.example.demo.server.services.funko;

import com.example.demo.common.models.Funko;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FunkoCacheImpl implements FunkoChache {

    private final Logger logger = LoggerFactory.getLogger(FunkoCacheImpl.class);
    private final int maxSize;
    private final Map<Long, Funko> cache;
    private final ScheduledExecutorService cleaner;

    public FunkoCacheImpl(int maxSize) {
        this.maxSize = maxSize;
        this.cache = new LinkedHashMap<Long, Funko>(maxSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Long, Funko> eldest) {
                return size() > maxSize;
            }
        };
        this.cleaner = Executors.newSingleThreadScheduledExecutor();
        this.cleaner.scheduleAtFixedRate(this::clear, 1, 1, TimeUnit.MINUTES);
    }
    @Override
    public Mono<Void> put(Long key, Funko value) {
        logger.debug("Añadiendo funko a cache con id: " + key + " y valor: " + value);
        return Mono.fromRunnable(() -> cache.put(key, value));
    }

    @Override
    public Mono<Funko> get(Long key) {
        logger.debug("Obteniendo funko de cache con id: " + key);
        return Mono.justOrEmpty(cache.get(key));
    }

    @Override
    public Mono<Void> remove(Long key) {
        logger.debug("Eliminando funko de cache con id: " + key);
        return Mono.fromRunnable(() -> cache.remove(key));
    }

    @Override
    public void clear() {
        cache.entrySet().removeIf(entry -> {
            boolean shouldRemove = entry.getValue().getUpdatedAt().plusMinutes(1).isBefore(LocalDateTime.now());
            if (shouldRemove) {
                logger.debug("Autoeliminando por caducidad funko de cache con id: " + entry.getKey());
            }
            return shouldRemove;
        });
    }

    @Override
    public void shutdown() {
        cleaner.shutdown();
    }
}
