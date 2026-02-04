package hr.abysalto.hiring.api.junior.security;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import hr.abysalto.hiring.api.junior.data.model.UserInfo;

import java.util.Optional;

@Repository
public interface UserInfoRepository extends CrudRepository<UserInfo, Long> {

    @Query("SELECT * FROM USER_INFO WHERE USERNAME = :username FETCH FIRST 1 ROW ONLY")
    Optional<UserInfo> findByUsername(@Param("username") String username);

    @Query("SELECT * FROM USER_INFO WHERE EMAIL = :email FETCH FIRST 1 ROW ONLY")
    Optional<UserInfo> findByEmail(@Param("email") String email);

    @Query("SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END FROM USER_INFO WHERE USERNAME = :username")
    Boolean existsByUsername(@Param("username") String username);

    @Query("SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END FROM USER_INFO WHERE EMAIL = :email")
    Boolean existsByEmail(@Param("email") String email);
}
