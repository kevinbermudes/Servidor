package com.example.demo.server.repositories.crud;

import com.example.demo.common.models.Funko;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface crudRepository <T, ID>{
    Flux<Funko> findAll();

    // Buscar por ID
    Mono<Funko> findById(ID id);

    // Guardardamos
    Mono<Funko> save(T t);

    // Actualizamos
    Mono<Funko> update(T t);

    // Borrar por ID
    Mono<Boolean> deleteById(ID id);

    // Borrar todos
    Mono<Void> deleteAll();
}
