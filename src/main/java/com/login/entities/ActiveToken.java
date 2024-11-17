//package com.login.entities;
//
//import jakarta.persistence.*;
//import lombok.Data;
//import lombok.Getter;
//import lombok.Setter;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "active_tokens")
//
//@Getter
//@Setter
//public class ActiveToken {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false, unique = true)
//    private String username;
//
//    @Column(nullable = false)
//    private String token;
//
//    @Column(name = "created_at")
//    private LocalDateTime createdAt;
////
//    public ActiveToken(String username, String jwtToken, LocalDateTime now) {
//    }
//
//    public ActiveToken() {
//
//    }
//
//    // Getters and Setters
//}
