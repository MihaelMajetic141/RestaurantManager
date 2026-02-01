package hr.abysalto.hiring.api.junior.data.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
	private String buyerName;
	private String orderStatus;
	private String orderTime;
	private String paymentOption;
	private AddressRequest deliveryAddress;
	private String contactNumber;
	private String orderNote;
	private List<OrderItemRequest> orderItems;
	private String currency;
}
