package hr.abysalto.hiring.api.junior.data.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemRequest {
	private Long orderId;

	@NotNull(message = "Item ID is required")
	private Long itemId;

	@NotNull(message = "Quantity is required")
	@Positive(message = "Quantity must be positive")
	private Short quantity;

}
