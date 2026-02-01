package hr.abysalto.hiring.api.junior.components.mapper;

import org.springframework.stereotype.Component;
import hr.abysalto.hiring.api.junior.data.dto.OrderResponse;
import hr.abysalto.hiring.api.junior.data.model.Order;

@Component
public class OrderMapper {
    public static OrderResponse toOrderResponse(Order order) {
        return OrderResponse.builder()
            .buyerName(order.getBuyer().getFirstName() + " " + order.getBuyer().getLastName())
            .orderStatus(order.getOrderStatus())
            .paymentTime(order.getOrderTime())
            .paymentOption(order.getPaymentOption())
            .deliveryAddress(
                order.getDeliveryAddress().getStreet() + " " 
                + order.getDeliveryAddress().getHomeNumber() + ", " 
                + order.getDeliveryAddress().getCity()
            )
            .contactNumber(order.getContactNumber())
            .orderNote(order.getOrderNote())
            .orderItems(order.getOrderItems().stream()
                .map(OrderItemMapper::toOrderItemResponse)
                .toList()
            )
            .totalPrice(order.getTotalPrice())
            .currency(order.getCurrency())
            .build();
    }
}