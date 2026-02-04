package hr.abysalto.hiring.api.junior.data.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("USER_INFO")
public class UserInfo {

    @Id
    private Long id;

    @Column("USERNAME")
    private String username;

    @Column("EMAIL")
    private String email;

    @Column("PASSWORD")
    private String password;

}

