package com.example.demo.server.repositories.funko;

import com.example.demo.common.models.Funko;
import com.example.demo.server.repositories.crud.crudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface FunkoRepository extends crudRepository<Funko,Long> {

    Flux<Funko> findByNombre(String nombre);

    Mono<Funko> findByUuid(UUID uuid);



}
