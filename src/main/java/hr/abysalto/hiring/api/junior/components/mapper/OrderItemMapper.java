package hr.abysalto.hiring.api.junior.components.mapper;

import org.springframework.stereotype.Component;

import hr.abysalto.hiring.api.junior.data.dto.response.OrderItemResponse;
import hr.abysalto.hiring.api.junior.data.model.OrderItem;

@Component
public class OrderItemMapper {

    public static OrderItemResponse toOrderItemResponse(OrderItem orderItem) {
        return OrderItemResponse.builder()
            .orderId(orderItem.getOrderId())
            .itemId(orderItem.getItemId())
            .snapshotPrice(orderItem.getSnapshotPrice())
            .quantity(orderItem.getQuantity())
            .build();
    }
}
