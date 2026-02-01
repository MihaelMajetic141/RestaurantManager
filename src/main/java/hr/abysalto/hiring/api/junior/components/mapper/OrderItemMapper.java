package hr.abysalto.hiring.api.junior.components.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import hr.abysalto.hiring.api.junior.data.dto.OrderItemResponse;
import hr.abysalto.hiring.api.junior.data.model.Item;
import hr.abysalto.hiring.api.junior.data.model.OrderItem;
import hr.abysalto.hiring.api.junior.repository.ItemRepository;

@Component
public class OrderItemMapper {

    @Autowired
    private static ItemRepository itemRepository;
    
    public static OrderItemResponse toOrderItemResponse(OrderItem orderItem) {
        Item item = itemRepository.findById(orderItem.getItemId())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Item not found for id = " + orderItem.getItemId()));

        return OrderItemResponse.builder()
            .orderId(orderItem.getOrderId())
            .itemId(orderItem.getItemId())
            .snapshotPrice(orderItem.getSnapshotPrice()) // use snapshot so it does not change when Item price changes
            .quantity(orderItem.getQuantity())
            .build();
    }
}
