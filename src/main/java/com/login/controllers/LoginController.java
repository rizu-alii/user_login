package com.login.controllers;
//import com.login.dao.ActiveTokenRepository;
import com.login.dao.UsersRepo;
//import com.login.entities.ActiveToken;
import com.login.entities.Users;
import com.login.exceptions.UserAlreadyExistsException;
import com.login.exceptions.UserNotFoundException;
import com.login.security.JWTService;
import com.login.security.MyUserDetailService;
import com.login.services.GetClientIpAddress;
import com.login.services.VpnDetectionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.management.relation.RoleNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/api/auth")
//@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}, allowCredentials = "true")
public class LoginController {

    @Autowired
    private VpnDetectionService vpnDetectionService;
    @Autowired
private MyUserDetailService myUserDetailService;
    @Autowired
    private JWTService jwtService;
    @Autowired
    private AuthenticationManager authenticationManager;
//    @Autowired
//    private ActiveTokenRepository activeTokenRepository;
    @Autowired
    private GetClientIpAddress getClientIpAddress;

    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(12);
    @Autowired
    private UsersRepo usersRepo;

    @PostMapping("/register")
    public ResponseEntity<Users> register(@Valid @RequestBody Users users) throws RoleNotFoundException {
        // Check if the username already exists
        if (usersRepo.findByUsername(users.getUsername()) == null) {
            System.out.println("registering");
            // Encrypt and set the user's password
            users.setPassword(bCryptPasswordEncoder.encode(users.getPassword()));
            // Save the user
            Users savedUser = usersRepo.saveAndFlush(users);
            // Return the response
            return ResponseEntity.status(HttpStatus.CREATED)
                    .header("Move to this page", "http://localhost:8080/meditrack/login")
                    .body(savedUser);
        } else {
            System.out.println("throwing register exception");
            throw new UserAlreadyExistsException("User with username " + users.getUsername() + " already exists.");
        }
    }
    //localhost:8080/api/auth/login---post....rizu....sp23bcs126
    @PostMapping("/login")
    @Transactional
    public ResponseEntity<String> login(
            @RequestBody Users users,
            HttpServletRequest request
    ) {
        // Step 1: Check if the username exists in the database
        Optional<Users> existingUser = Optional.ofNullable(usersRepo.findByUsername(users.getUsername()));
        if (existingUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Username not found.");
        }

        Users user = existingUser.get();

        // Step 2: Check if the user is banned
        if (user.getIsBanned()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Your account is banned.");
        }

        // Step 3: Validate the password using BCrypt
        boolean isPasswordValid = bCryptPasswordEncoder.matches(users.getPassword(), user.getPassword());
        if (!isPasswordValid) {
            // Increment the failed login attempt count
            int count = user.getBan_count() + 1;
            user.setBan_count(count);

            // Ban the user if the failed attempt count reaches 3
            if (count >= 3) {
                user.setIsBanned(true);
            }

            // Save the updated user information
            try {
                usersRepo.save(user);
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update user after failed login attempt.");
            }

            System.out.println("Incorrect password attempt. Current ban count: " + count);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password.");
        }

        // Step 4: Check for VPN usage
        boolean isVpn = vpnDetectionService.isVpn(getPublicIpAddress());
        if (isVpn) {
            user.setIsBanned(true);  // Ban the user if VPN is detected
            try {
                usersRepo.save(user);
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save user after VPN detection.");
            }
            return ResponseEntity.status(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS).body("Your account has been banned due to VPN detection.");
        }

        // Step 5: Generate JWT token after successful login
        String jwtToken = jwtService.generateToken(user.getUsername());

        // Reset the failed login attempt count
        user.setBan_count(0);
        usersRepo.save(user);

        // Step 6: Construct the JSON response
        String jsonResponse = String.format(
                "{\"jwtToken\": \"Bearer %s\", \"message\": \"Logged in successfully\", \"nextUrl\": \"http://localhost:8080/api/auth/welcome\"}",
                jwtToken
        );

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonResponse);
    }

    @GetMapping("/welcome")
    public ResponseEntity<String> welcome() {
//        Map<String, String> temp = new HashMap<>();
//        temp.put("name" , token.getPrincipal().getAttribute("name"));
//        temp.put("email" , token.getPrincipal().getAttribute("email"));
//        temp.put("password" , token.getPrincipal().getAttribute("password"));
//        temp.put("picture" , token.getPrincipal().getAttribute("picture"));
        return ResponseEntity.status(HttpStatus.OK).body("Hello");
    }
    @GetMapping("/welcome2")
    public ResponseEntity<String> welcome2() {
        return ResponseEntity.status(HttpStatus.OK).body("Hello2");
    }


    private String getPublicIpAddress() {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject("https://api.ipify.org", String.class);
    }
}
