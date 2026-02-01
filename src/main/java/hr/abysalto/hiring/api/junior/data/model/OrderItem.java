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

	@Column("SNAPSHOT_PRICE")
	private BigDecimal snapshotPrice;
	
	@Column("QUANTITY")
	private Short quantity;

	@Column("ITEM_ID") 
	private Long itemId;

	@Column("ORDER_ID")
	private Long orderId;
}
