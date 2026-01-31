package hr.abysalto.hiring.api.junior.data.model;

import lombok.Data;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("ITEM")
public class Item {
	@Id
	private Long id;

	@Column("ITEM_NUMBER")
	private Short itemNumber;

	@Column("NAME")
	private String name;

	@Column("PRICE")
	private BigDecimal price;

}

