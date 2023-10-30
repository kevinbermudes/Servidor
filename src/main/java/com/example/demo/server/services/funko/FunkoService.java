package com.example.demo.server.services.funko;

import com.example.demo.common.models.Funko;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

interface FunkoService {
    Flux<Funko> findAll();

    Flux<Funko> findAllByNombre(String nombre);

    Mono<Funko> findById(long id);

    Mono<Funko> findByUuid(UUID uuid);

    Mono<Funko> save(Funko funko);

    Mono<Funko> update(Funko funko);

    Mono<Funko> deleteById(long id);

    Mono<Void> deleteAll();
}
