package com.example.demo.server.services.funko;

import com.example.demo.Client.Excepciones.ClienteExcepciones;
import com.example.demo.common.models.Funko;
import com.example.demo.server.repositories.funko.FunkoRepository;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;

import java.util.UUID;


public class FunkoServiceImpl implements FunkoService {

    private static final int CACHE_SIZE = 10;
    private static  FunkoServiceImpl instance ;
    private final FunkoChache cache;

    private FunkoRepository funkoRepository;
    private final Logger logger= (Logger) LoggerFactory.getLogger(FunkoServiceImpl.class);
    private FunkoServiceImpl(FunkoRepository funkoRepository) {
        this.cache = new FunkoCacheImpl(CACHE_SIZE);
        this.funkoRepository = funkoRepository;

    }
    public static FunkoServiceImpl getInstance(FunkoRepository funkoRepository) {
        if (instance == null) {
            instance = new FunkoServiceImpl(funkoRepository);
        }
        return instance;
    }
    @Override
    public Flux<Funko> findAll() {
        logger.debug("Buscando todos los funkos");
        return funkoRepository.findAll();
    }

    @Override
    public Flux<Funko> findAllByNombre(String nombre) {
        logger.debug("Buscando todos los funkos por nombre");
        return funkoRepository.findByNombre(nombre);
    }

    @Override
    public Mono<Funko> findById(long id) {
        logger.debug("Buscando funko por id: " + id);
        return cache.get(id)
                .switchIfEmpty(funkoRepository.findById(id)
                        .flatMap(funko -> cache.put(funko.getId(),funko)
                                .then(Mono.just(funko)))
                        .switchIfEmpty(Mono.error(new ClienteExcepciones("Funko no encontrado"))));
    }

    @Override
    public Mono<Funko> findByUuid(UUID uuid) {
        logger.debug("Buscando funko por uuid: " + uuid);
        return funkoRepository.findByUuid(uuid)
                .flatMap(funko -> cache.put(funko.getId(),funko)
                        .then(Mono.just(funko)))
                .switchIfEmpty(Mono.error(new ClienteExcepciones("Funko no encontrado")));
    }

    @Override
    public Mono<Funko> save(Funko funko) {
        logger.debug("Guardando funko: " + funko);
        return funkoRepository.save(funko)
                .flatMap(save ->findByUuid(save.getCod()));
    }

    @Override
    public Mono<Funko> update(Funko funko) {
        logger.debug("Actualizando funko: " + funko);
        return funkoRepository.findById(funko.getId())
                .switchIfEmpty(Mono.error(new ClienteExcepciones("Funko no encontrado con id :"+funko.getId())))
                .flatMap(exists -> funkoRepository.update(funko)
                        .flatMap(update -> cache.put(update.getId(),update)
                                .thenReturn(update)));
    }

    @Override
    public Mono<Funko> deleteById(long id) {
        logger.debug("Borrando funko por id: " + id);
        return funkoRepository.findById(id)
                .switchIfEmpty(Mono.error(new ClienteExcepciones("Funko no encontrado con id :"+id)))
                .flatMap(funko -> cache.remove(funko.getId())
                        .then(funkoRepository.deleteById(funko.getId())))
                        .thenReturn(funkoRepository.findById(id).block());
    }

    @Override
    public Mono<Void> deleteAll() {
        logger.debug("Borrando todos los funkos");
        cache.clear();
        return funkoRepository.deleteAll()
                .then(Mono.empty());
    }
}
