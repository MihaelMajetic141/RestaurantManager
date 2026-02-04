package hr.abysalto.hiring.api.junior.components.mapper;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import hr.abysalto.hiring.api.junior.data.dto.request.AddressRequest;
import hr.abysalto.hiring.api.junior.data.dto.request.OrderItemRequest;
import hr.abysalto.hiring.api.junior.data.dto.request.OrderRequest;
import hr.abysalto.hiring.api.junior.data.dto.response.OrderResponse;
import hr.abysalto.hiring.api.junior.data.model.Order;
import hr.abysalto.hiring.api.junior.data.model.OrderItem;

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

    public static OrderRequest toOrderRequest(Order order) {
        OrderRequest req = new OrderRequest();
        if (order.getBuyer() != null) {
            req.setBuyerId(order.getBuyerId());
        }
        if (order.getOrderStatus() != null) {
            req.setOrderStatus(order.getOrderStatus().name());
        }
        if (order.getOrderTime() != null) {
            req.setOrderTime(order.getOrderTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        if (order.getPaymentOption() != null) {
            req.setPaymentOption(order.getPaymentOption().name());
        }
        if (order.getDeliveryAddress() != null) {
            req.setDeliveryAddress(new AddressRequest(
                order.getDeliveryAddress().getCity(),
                order.getDeliveryAddress().getStreet(),
                order.getDeliveryAddress().getHomeNumber()
            ));
        }
        req.setContactNumber(order.getContactNumber());
        req.setOrderNote(order.getOrderNote());
        req.setCurrency(order.getCurrency());
        req.setTotalPrice(order.getTotalPrice());
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            List<OrderItemRequest> items = order.getOrderItems().stream()
                .map(OrderMapper::toOrderItemRequest)
                .collect(Collectors.toList());
            req.setOrderItems(items);
        }
        return req;
    }

    public static OrderItemRequest toOrderItemRequest(OrderItem orderItem) {
        return OrderItemRequest.builder()
            .orderId(orderItem.getOrderId())
            .itemId(orderItem.getItemId())
            .quantity(orderItem.getQuantity())
            .build();
    }
}