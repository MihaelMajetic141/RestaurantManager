package hr.abysalto.hiring.api.junior.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hr.abysalto.hiring.api.junior.components.PatchUtils;
import hr.abysalto.hiring.api.junior.components.mapper.OrderItemMapper;
import hr.abysalto.hiring.api.junior.data.dto.OrderItemRequest;
import hr.abysalto.hiring.api.junior.data.model.OrderItem;
import hr.abysalto.hiring.api.junior.service.OrderItemService;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order-item")
@AllArgsConstructor
public class OrderItemController {

	private final OrderItemService orderItemService;
	private final ObjectMapper objectMapper;
	private final PatchUtils patchUtils;
	private final Validator validator;

	@GetMapping("/getAll")
	public ResponseEntity<?> getOrderItems(
			@RequestParam(required = false) Long orderId,
			@RequestParam(required = false) Long itemId,
			@PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable
	) {
		Page<OrderItem> page = orderItemService.getAllOrderItems(orderId, itemId, pageable);
		return ResponseEntity.ok(page.getContent());
	}

	@GetMapping("/get/{id}")
	public ResponseEntity<?> getOrderItemById(@PathVariable Long id) {
		OrderItem orderItem = orderItemService.getOrderItemById(id);
		return ResponseEntity.ok(OrderItemMapper.toOrderItemResponse(orderItem));
	}

	@PostMapping("/create")
	public ResponseEntity<?> createOrderItem(@Valid @RequestBody OrderItemRequest request) {
		OrderItem newOrderItem = orderItemService.createOrderItem(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(newOrderItem);
	}

	@PutMapping("/update/{id}")
	public ResponseEntity<?> updateOrderItem(@PathVariable Long id, @Valid @RequestBody OrderItemRequest request) {
		OrderItem updatedOrderItem = orderItemService.updateOrderItem(id, request);
		return ResponseEntity.ok(OrderItemMapper.toOrderItemResponse(updatedOrderItem));
	}

	@PatchMapping(path = "/patch/{id}", consumes = "application/merge-patch+json")
	public ResponseEntity<?> patchOrderItem(
			@PathVariable Long id,
			@RequestBody JsonNode patchNode
	) throws JsonProcessingException, IllegalArgumentException {
		OrderItem existingOrderItem = orderItemService.getOrderItemById(id);

		JsonNode existingNode = objectMapper.valueToTree(existingOrderItem);
		JsonNode merged = patchUtils.merge(existingNode, patchNode);
		OrderItemRequest itemPatch = objectMapper.treeToValue(merged, OrderItemRequest.class);
		validator.validate(itemPatch);

		OrderItem savedOrderItem = orderItemService.patchOrderItem(id, itemPatch);
		return ResponseEntity.ok(OrderItemMapper.toOrderItemResponse(savedOrderItem));
	}

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<?> deleteOrderItem(@PathVariable Long id) {
		orderItemService.deleteOrderItem(id);
		return ResponseEntity.noContent().build();
	}
}
