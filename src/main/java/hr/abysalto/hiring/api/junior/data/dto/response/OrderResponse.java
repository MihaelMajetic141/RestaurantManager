package hr.abysalto.hiring.api.junior.data.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import hr.abysalto.hiring.api.junior.data.enums.OrderStatus;
import hr.abysalto.hiring.api.junior.data.enums.PaymentOption;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
	private String buyerName;
	private OrderStatus orderStatus;
	private LocalDateTime paymentTime;
	private PaymentOption paymentOption;
	private String deliveryAddress;
	private String contactNumber;
	private String orderNote;
	private List<OrderItemResponse> orderItems;
	private BigDecimal totalPrice;
	private String currency;
}
