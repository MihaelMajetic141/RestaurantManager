package hr.abysalto.hiring.api.junior.data.dto.response;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class JwtResponse {
    private String accessToken;
    private String refreshToken;
}
