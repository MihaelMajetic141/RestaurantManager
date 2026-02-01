package hr.abysalto.hiring.api.junior.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import hr.abysalto.hiring.api.junior.data.dto.OrderItemRequest;
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
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order item not found for id = " + id));
	}

	public OrderItem createOrderItem(OrderItemRequest request) {
		validateOrderExists(request.getOrderId());
		Item item = validateItemExists(request.getItemId());
		if (request.getQuantity() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity is required");
		}

		OrderItem orderItem = new OrderItem();
		orderItem.setOrderId(request.getOrderId());
		orderItem.setItemId(request.getItemId());
		orderItem.setQuantity(request.getQuantity());
		orderItem.setSnapshotPrice(request.getSnapshotPrice() != null ? request.getSnapshotPrice() : item.getPrice());
		return orderItemRepository.save(orderItem);
	}

	public OrderItem updateOrderItem(Long id, OrderItemRequest request) {
		OrderItem orderItem = orderItemRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order item not found for id = " + id));
		validateOrderExists(request.getOrderId());
		validateItemExists(request.getItemId());
		if (request.getQuantity() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity is required");
		}

		orderItem.setOrderId(request.getOrderId());
		orderItem.setItemId(request.getItemId());
		orderItem.setQuantity(request.getQuantity());
		orderItem.setSnapshotPrice(request.getSnapshotPrice());
		return orderItemRepository.save(orderItem);
	}

	public OrderItem patchOrderItem(Long id, OrderItemRequest request) {
		OrderItem orderItem = orderItemRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order item not found for id = " + id));
		if (request.getOrderId() != null) {
			validateOrderExists(request.getOrderId());
			orderItem.setOrderId(request.getOrderId());
		}
		if (request.getItemId() != null) {
			validateItemExists(request.getItemId());
			orderItem.setItemId(request.getItemId());
		}
		if (request.getQuantity() != null) {
			orderItem.setQuantity(request.getQuantity());
		}
		if (request.getSnapshotPrice() != null) {
			orderItem.setSnapshotPrice(request.getSnapshotPrice());
		}
		return orderItemRepository.save(orderItem);
	}

	public void deleteOrderItem(Long id) {
		if (orderItemRepository.findById(id).isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order item not found for id = " + id);
		}
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
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found for id = " + itemId));
	}
}
