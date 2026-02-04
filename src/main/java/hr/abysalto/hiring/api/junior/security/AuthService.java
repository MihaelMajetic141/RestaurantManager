package hr.abysalto.hiring.api.junior.security;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import hr.abysalto.hiring.api.junior.data.dto.response.JwtResponse;
import hr.abysalto.hiring.api.junior.data.dto.request.LoginRequest;
import hr.abysalto.hiring.api.junior.data.dto.request.RefreshTokenRequest;
import hr.abysalto.hiring.api.junior.data.dto.request.RegistrationRequest;
import hr.abysalto.hiring.api.junior.data.model.RefreshToken;
import hr.abysalto.hiring.api.junior.data.model.UserInfo;

import java.util.Optional;


@Service
@AllArgsConstructor
public class AuthService {

    private final UserInfoRepository userInfoRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public UserInfo register(RegistrationRequest request) {
        if (userInfoRepository.existsByUsername(request.getUsername()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error: Username is already taken!");
        if (userInfoRepository.existsByEmail(request.getEmail()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error: Email is already taken!");

        UserInfo user = UserInfo.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        return userInfoRepository.save(user);
    }

    public JwtResponse login(LoginRequest loginRequest) {
        Optional<UserInfo> user = userInfoRepository.findByUsername(loginRequest.getUsername());
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("Invalid user request!");
        }

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(), loginRequest.getPassword()));

        if (authentication.isAuthenticated()) {
            return JwtResponse.builder()
                    .accessToken(jwtService.generateToken(user.get().getEmail()))
                    .refreshToken(refreshTokenService.createRefreshToken(user.get().getEmail()).getToken())
                    .build();
        } else
            throw new UsernameNotFoundException("Invalid user request!");
    }

    public JwtResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenRequest.getToken()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error: Refresh token does not exist!"));

        RefreshToken validRefreshToken = refreshTokenService.verifyExpiration(refreshToken);
        UserInfo user = userInfoRepository.findById(validRefreshToken.getUserId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error: User does not exist!"));

        String accessToken = jwtService.generateToken(user.getEmail());
        return JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(validRefreshToken.getToken())
                .build();
    }


}
