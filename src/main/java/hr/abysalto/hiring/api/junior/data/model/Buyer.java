package hr.abysalto.hiring.api.junior.data.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("BUYER")
public class Buyer {
	@Id
	private Long id;

	@Column("FIRST_NAME")
	private String firstName;

	@Column("LAST_NAME")
	private String lastName;

	@Column("TITLE")
	private String title;
}
