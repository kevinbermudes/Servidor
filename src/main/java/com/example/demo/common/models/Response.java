package com.example.demo.common.models;

public record Response(Status status, String content, String createdAt) {
    public enum Status {
        OK, ERROR, BYE, TOKEN
    }
}

