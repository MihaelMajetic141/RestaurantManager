package hr.abysalto.hiring.api.junior.data.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.AccessType;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import hr.abysalto.hiring.api.junior.data.enums.OrderStatus;
import hr.abysalto.hiring.api.junior.data.enums.PaymentOption;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("ORDERS")
@AccessType(AccessType.Type.PROPERTY)
public class Order {

	@Id
	private Long id;

	@Column("BUYER_ID")
	private Long buyerId;

	@Transient
	private Buyer buyer;

	@Transient
	private OrderStatus orderStatus;

	@Column("ORDER_STATUS")
	public String getStringOrderStatus() {
		return this.orderStatus != null ? this.orderStatus.toString() : null;
	}

	public void setStringOrderStatus(String orderStatusString) {
		this.orderStatus = OrderStatus.fromString(orderStatusString);
	}

	@Column("ORDER_TIME")
	private LocalDateTime orderTime;

	@Transient
	private PaymentOption paymentOption;

	@Column("PAYMENT_OPTION")
	public String getStringPaymentOption() {
		return this.paymentOption != null ? this.paymentOption.toString() : null;
	}

	public void setStringPaymentOption(String paymentOptionString) {
		this.paymentOption = PaymentOption.fromString(paymentOptionString);
	}

	@Column("DELIVERY_ADDRESS_ID")
	private Long deliveryAddressId;

	@Transient
	private BuyerAddress deliveryAddress;

	@Column("CONTACT_NUMBER")
	private String contactNumber;

	@Column("ORDER_NOTE")
	private String orderNote;

	@MappedCollection(idColumn = "ORDER_ID")
	private Set<OrderItem> orderItems;

	@Column("TOTAL_PRICE")
	private BigDecimal totalPrice;

	@Column("CURRENCY")
	private String currency;

	public void setBuyer(Buyer buyer) {
		this.buyer = buyer;
		this.buyerId = buyer != null ? buyer.getId() : null;
	}

	public void setDeliveryAddress(BuyerAddress deliveryAddress) {
		this.deliveryAddress = deliveryAddress;
		this.deliveryAddressId = deliveryAddress != null ? deliveryAddress.getId() : null;
	}
}
