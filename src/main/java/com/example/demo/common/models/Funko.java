package com.example.demo.common.models;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
@Data
@Builder

public class Funko {


    @Builder.Default
    private long id = IdGenerator.getInstance().generateId();

    @Builder.Default
    private UUID cod = UUID.randomUUID();
    private String nombre;
    private Modelo modelo;
    private double precio;
    private LocalDate fechaLanzamiento;
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();



    public enum Modelo {
        MARVEL, DISNEY, ANIME, OTROS
    }

}
