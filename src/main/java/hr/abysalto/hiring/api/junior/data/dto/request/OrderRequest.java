package hr.abysalto.hiring.api.junior.data.dto.request;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
	@NotNull(message = "Buyer id is required")
	private Long buyerId;

	@NotBlank(message = "Order status is required")
	@Size(max = 32)
	private String orderStatus;

	@NotBlank(message = "Order time is required")
	private String orderTime;

	@NotBlank(message = "Payment option is required")
	@Size(max = 32)
	private String paymentOption;

	@NotNull(message = "Delivery address is required")
	@Valid
	private AddressRequest deliveryAddress;

	@Size(max = 100)
	private String contactNumber;

	@Size(max = 500)
	private String orderNote;

	@Valid
	private List<OrderItemRequest> orderItems;

	@Size(max = 50)
	private String currency;

	private BigDecimal totalPrice;
}
