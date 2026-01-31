package hr.abysalto.hiring.api.junior.data.model;

import java.math.BigDecimal;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("ORDER_ITEM")
public class OrderItem {
	@Id
	private Long id;

	@Column("ITEM_NUMBER")
	private Short itemNumber;

	@Column("NAME")
	private String name;

	@Column("QUANTITY")
	private Short quantity;

	@Column("PRICE")
	private BigDecimal price;

	@Column("ORDER_ID")
	private Long orderId;
}
