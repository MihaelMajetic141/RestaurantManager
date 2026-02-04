package hr.abysalto.hiring.api.junior.security;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import hr.abysalto.hiring.api.junior.data.model.RefreshToken;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {

    @Query("SELECT * FROM REFRESH_TOKEN WHERE TOKEN = :token FETCH FIRST 1 ROW ONLY")
    Optional<RefreshToken> findByToken(@Param("token") String token);

    @Query("SELECT * FROM REFRESH_TOKEN WHERE USER_ID = :userId FETCH FIRST 1 ROW ONLY")
    Optional<RefreshToken> findByUserId(@Param("userId") Long userId);
}