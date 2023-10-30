package com.example.demo.common.models;

public record User(long id, String username, String password, Role role) {
    public enum Role {
        ADMIN, USER
    }
}