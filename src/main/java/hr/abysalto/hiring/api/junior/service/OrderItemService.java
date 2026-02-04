package hr.abysalto.hiring.api.junior.service;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import hr.abysalto.hiring.api.junior.data.dto.request.OrderItemRequest;
import hr.abysalto.hiring.api.junior.data.model.Item;
import hr.abysalto.hiring.api.junior.data.model.OrderItem;
import hr.abysalto.hiring.api.junior.repository.ItemRepository;
import hr.abysalto.hiring.api.junior.repository.OrderItemRepository;
import hr.abysalto.hiring.api.junior.repository.OrderRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class OrderItemService {

	private final OrderItemRepository orderItemRepository;
	private final OrderRepository orderRepository;
	private final ItemRepository itemRepository;

	public Page<OrderItem> getAllOrderItems(Long orderId, Long itemId, Pageable pageable) {
		List<OrderItem> all = new ArrayList<>();
		orderItemRepository.findAll().forEach(all::add);

		var stream = all.stream();
		if (orderId != null) {
			stream = stream.filter(oi -> orderId.equals(oi.getOrderId()));
		}
		if (itemId != null) {
			stream = stream.filter(oi -> itemId.equals(oi.getItemId()));
		}
		List<OrderItem> filtered = stream.toList();

		int start = (int) pageable.getOffset();
		int end = Math.min(start + pageable.getPageSize(), filtered.size());
		List<OrderItem> pageContent = start < filtered.size() ? filtered.subList(start, end) : List.of();

		return new PageImpl<>(pageContent, pageable, filtered.size());
	}

	public OrderItem getOrderItemById(Long id) {
		if (id == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order item ID is required");
		}
		return orderItemRepository.findById(id)
				.orElseThrow(
						() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order item not found for id = " + id));
	}

	@Transactional
	public OrderItem createOrderItem(OrderItemRequest request) {
		validateOrderExists(request.getOrderId());
		Item item = itemRepository.findById(request.getItemId())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Item not found for id = " + request.getItemId()));

		if (request.getQuantity() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity is required");
		}
		OrderItem orderItem = new OrderItem();
		orderItem.setOrderId(request.getOrderId());
		orderItem.setItemId(request.getItemId());
		orderItem.setQuantity(request.getQuantity());
		orderItem.setSnapshotPrice(item.getPrice());
		return orderItemRepository.save(orderItem);
	}

	public OrderItem updateOrderItem(Long id, OrderItemRequest request) {
		OrderItem orderItem = orderItemRepository.findById(id)
				.orElseThrow(
						() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order item not found for id = " + id));
		validateOrderExists(request.getOrderId());
		Item item = validateItemExists(request.getItemId());
		if (request.getQuantity() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity is required");
		}

		orderItem.setOrderId(request.getOrderId());
		orderItem.setItemId(request.getItemId());
		orderItem.setQuantity(request.getQuantity());
		orderItem.setSnapshotPrice(item.getPrice());
		return orderItemRepository.save(orderItem);
	}

	@Transactional
	public OrderItem patchOrderItem(Long id, OrderItemRequest request, JsonNode patchNode) {
		OrderItem orderItem = orderItemRepository.findById(id)
				.orElseThrow(
						() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order item not found with ID: " + id));

		if (patchNode.has("orderId") && request.getOrderId() != null) {
			validateOrderExists(request.getOrderId());
			orderItem.setOrderId(request.getOrderId());
		}
		if (patchNode.has("itemId") && request.getItemId() != null) {
			Item item = validateItemExists(request.getItemId());
			orderItem.setItemId(request.getItemId());
			if (item.getPrice() != null) {
				orderItem.setSnapshotPrice(item.getPrice());
			}
		}
		if (patchNode.has("quantity") && request.getQuantity() != null) {
			orderItem.setQuantity(request.getQuantity());
		}
		return orderItemRepository.save(orderItem);
	}

	public void deleteOrderItem(Long id) {
		orderItemRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order item not found for id = " + id));
		orderItemRepository.deleteById(id);
	}

	private void validateOrderExists(Long orderId) {
		if (orderId == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order ID is required");
		}
		if (orderRepository.findById(orderId).isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found for id = " + orderId);
		}
	}

	private Item validateItemExists(Long itemId) {
		if (itemId == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item ID is required");
		}
		return itemRepository.findById(itemId)
				.orElseThrow(
						() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found for id = " + itemId));
	}
}
