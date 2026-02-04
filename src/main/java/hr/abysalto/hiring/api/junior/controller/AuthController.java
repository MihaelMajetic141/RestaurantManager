package hr.abysalto.hiring.api.junior.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import hr.abysalto.hiring.api.junior.data.dto.response.JwtResponse;
import hr.abysalto.hiring.api.junior.data.dto.request.LoginRequest;
import hr.abysalto.hiring.api.junior.data.dto.request.RefreshTokenRequest;
import hr.abysalto.hiring.api.junior.data.dto.request.RegistrationRequest;
import hr.abysalto.hiring.api.junior.data.model.RefreshToken;
import hr.abysalto.hiring.api.junior.security.AuthService;
import hr.abysalto.hiring.api.junior.security.RefreshTokenService;

import java.util.Optional;


@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    AuthService authService;
    RefreshTokenService refreshTokenService;

    @Operation(security = {})
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @Valid @RequestBody RegistrationRequest registrationRequest
    ) {
        authService.register(registrationRequest);
        return ResponseEntity.ok("User registered successfully!");
    }

    @Operation(security = {})
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest loginRequest
    ) {
        JwtResponse jwtResponse = authService.login(loginRequest);
        return ResponseEntity.ok().body(jwtResponse);
    }


    @Operation(security = {})
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestBody String refreshToken
    ) {
        Optional<RefreshToken> deletedRefreshToken = refreshTokenService.deleteRefreshToken(refreshToken);
        if (deletedRefreshToken.isPresent())
            return ResponseEntity.ok().body("You've been signed out!");
        else return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refreshToken");
    }

    @Operation(security = {})
    @PostMapping("/refreshToken")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        JwtResponse jwtResponse = authService.refreshToken(refreshTokenRequest);
        return ResponseEntity.ok(jwtResponse);
    }

}
