package hr.abysalto.hiring.api.junior.data.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
	private Long orderItemId;
	private Short itemNr;
	private String name;
	private Short quantity;
	private BigDecimal price;
}
