//package com.login.dao;
//
//import com.login.entities.ActiveToken;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.Optional;
//
//public interface ActiveTokenRepository extends JpaRepository<ActiveToken, Long> {
//    Optional<ActiveToken> findByUsername(String username);
//    void deleteByUsername(String username);
//}
