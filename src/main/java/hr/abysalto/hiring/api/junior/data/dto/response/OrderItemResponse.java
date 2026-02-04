package hr.abysalto.hiring.api.junior.data.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {
	private Long orderId;
	private Long itemId;
	private BigDecimal snapshotPrice;
	private Short quantity;
}
