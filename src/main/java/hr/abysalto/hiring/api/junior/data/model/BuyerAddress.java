package hr.abysalto.hiring.api.junior.data.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("BUYER_ADDRESS")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BuyerAddress {
	@Id
	private Long id;

	@Column("CITY")
	private String city;

	@Column("STREET")
	private String street;

	@Column("HOME_NUMBER")
	private String homeNumber;

	@Column("BUYER_ID")
	private Long buyerId;
}
