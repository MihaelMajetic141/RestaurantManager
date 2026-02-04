package hr.abysalto.hiring.api.junior.data.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("REFRESH_TOKEN")
public class RefreshToken {
    @Id
    private Long id;

    @Column("TOKEN")
    private String token;

    @Column("EXPIRY_DATE")
    private Instant expiryDate;

    @Column("USER_ID")
    private Long userId;
}
