package hr.abysalto.hiring.api.junior.data.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequest {
	private Long orderId;

	@NotNull(message = "Item ID is required")
	private Long itemId;

	@NotNull(message = "Quantity is required")
	@Positive(message = "Quantity must be positive")
	private Short quantity;

	@DecimalMin(value = "0", inclusive = true, message = "Snapshot price must be non-negative")
	private BigDecimal snapshotPrice;
}
