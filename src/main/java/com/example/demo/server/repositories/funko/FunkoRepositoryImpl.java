package com.example.demo.server.repositories.funko;

import com.example.demo.common.models.Funko;
import com.example.demo.server.services.services.database.DatabaseManager;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class FunkoRepositoryImpl implements FunkoRepository {
    private static FunkoRepositoryImpl instance;

    private final Logger logger = LoggerFactory.getLogger(FunkoRepositoryImpl.class);
    private final ConnectionPool connectionFactory;

    private FunkoRepositoryImpl(DatabaseManager databaseManager) {
        this.connectionFactory = databaseManager.getConnectionPool();
    }


    public static FunkoRepositoryImpl getInstance(DatabaseManager db) {
        if (instance == null) {
            instance = new FunkoRepositoryImpl(db);
        }
        return instance;
    }

    private static Funko getFunko(Row row) {
        return Funko.builder()
                .id(row.get("id", Long.class))
                .cod(UUID.fromString(row.get("uuid", String.class)))
                .nombre(row.get("nombre", String.class))
                .modelo(Funko.Modelo.valueOf(row.get("modelo", String.class)))
                .precio(row.get("precio", Double.class))
                .fechaLanzamiento(row.get("fechaLanzamiento", LocalDate.class))
                .createdAt(row.get("createdAt", LocalDateTime.class))
                .updatedAt(row.get("updatedAt", LocalDateTime.class))
                .build();

    }

    @Override
    public Flux<Funko> findAll() {
        logger.debug("Buscando todos los funkos");
        String sql = "SELECT * FROM FUNKOS";
        return Flux.usingWhen(
                connectionFactory.create(),
                connection -> Flux.from(connection.createStatement(sql).execute())
                        .flatMap(result -> result.map((row, rowMetadata) ->
                                getFunko(row)
                        )),
                Connection::close
        );

    }


    @Override
    public Mono<Funko> findByUuid(UUID uuid) {
        logger.debug("Buscando funkos por uuid: " + uuid);
        String sql = "SELECT * FROM FUNKOS WHERE uuid = ?";
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> Mono.from(connection.createStatement(sql)
                        .bind(0, uuid)
                        .execute()
                ).flatMap(result -> Mono.from(result.map((row, rowMetadata) ->
                        getFunko(row)
                ))),
                Connection::close
        );
    }

    @Override
    public Flux<Funko> findByNombre(String name) {
        logger.debug("Buscando todos los funkos por nombre");
        String sql = "SELECT * FROM FUNKOS WHERE nombre LIKE ?";
        return Flux.usingWhen(
                connectionFactory.create(),
                connection -> Flux.from(connection.createStatement(sql)
                        .bind(0, "%" + name + "%")
                        .execute()
                ).flatMap(result -> result.map((row, rowMetadata) ->
                        getFunko(row)
                )),
                Connection::close

        );
    }

    @Override
    public Mono<Funko> findById(Long id) {
        logger.debug("Buscando funkos por id: " + id);
        String sql = "SELECT * FROM FUNKOS WHERE id = ?";
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> Mono.from(connection.createStatement(sql)
                        .bind(0, id)
                        .execute()
                ).flatMap(result -> Mono.from(result.map((row, rowMetadata) ->
                        getFunko(row)
                ))),
                Connection::close
        );
    }


    @Override
    public Mono<Funko> save(Funko funko) {
        logger.debug("Guardando funko: " + funko);
        String sql = "INSERT INTO FUNKOS (uuid, nombre, modelo, precio, fechaLanzamiento) VALUES (?, ?, ?, ?, ?)";
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> Mono.from(connection.createStatement(sql)
                        .bind(0, funko.getCod())
                        .bind(1, funko.getNombre())
                        .bind(2, funko.getModelo().toString())
                        .bind(3, funko.getPrecio())
                        .bind(4, funko.getFechaLanzamiento())
                        .execute()
                ).flatMap(result -> Mono.from(result.map((row, rowMetadata) ->
                        getFunko(row)
                ))),
                Connection::close
        );
    }

    @Override
    public Mono<Funko> update(Funko funko) {
        logger.debug("Actualizando funko: " + funko);
        String query = "UPDATE FUNKOS SET nombre = ?, modelo = ?, precio = ?, fechaLanzamiento = ? WHERE uuid = ?";
        funko.setUpdatedAt(LocalDateTime.now());
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> Mono.from(connection.createStatement(query)
                        .bind(0, funko.getNombre())
                        .bind(1, funko.getModelo().toString())
                        .bind(2, funko.getPrecio())
                        .bind(3, funko.getFechaLanzamiento())
                        .bind(4, funko.getCod())
                        .execute()
                ).then(Mono.just(funko)),
                Connection::close
        );
    }

    @Override
    public Mono<Boolean> deleteById(Long id) {
        logger.debug("Borrando funko por id: " + id);
        String sql = "DELETE FROM ALUMNOS WHERE id = ?";
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> Mono.from(connection.createStatement(sql)
                                .bind(0, id)
                                .execute()
                        ).flatMapMany(result -> result.getRowsUpdated())
                        .hasElements(),
                Connection::close
        );
    }

    @Override
    public Mono<Void> deleteAll() {
        logger.debug("Borrando todos los funkos");
        String sql = "DELETE FROM FUNKOS";
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> Mono.from(connection.createStatement(sql)
                        .execute()
                ).then(),
                Connection::close
        );
    }

}
