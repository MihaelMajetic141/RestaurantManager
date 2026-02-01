package hr.abysalto.hiring.api.junior.controller;


import hr.abysalto.hiring.api.junior.components.PatchUtils;
import hr.abysalto.hiring.api.junior.components.mapper.OrderMapper;
import hr.abysalto.hiring.api.junior.data.dto.OrderRequest;
import hr.abysalto.hiring.api.junior.data.model.Order;
import hr.abysalto.hiring.api.junior.service.OrderService;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


@RestController
@RequestMapping("/api/order")
@AllArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final ObjectMapper objectMapper;
    private final PatchUtils patchUtils;
    private final Validator validator;


    @GetMapping("/getAll")
    public ResponseEntity<?> getOrders(
        @RequestParam(required = false) String orderStatus,
        @RequestParam(required = false) String paymentOption,
        @RequestParam(required = false) String currency,
        @PageableDefault(
            sort = "orderTime",
            direction = Sort.Direction.DESC
        ) Pageable pageable
    ) {
        Page<Order> page = orderService.getAllOrders(orderStatus, paymentOption, currency, pageable);
        return ResponseEntity.ok(page.getContent());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        return ResponseEntity.ok(OrderMapper.toOrderResponse(order));
    }

    @PostMapping("/create")
    public ResponseEntity<?> createOrder(
        @Valid @RequestBody OrderRequest order
    ) {
        Order newOrder = orderService.createOrder(order);
        // messagingTemplate.convertAndSend("/topic/orders", newOrder);
        return ResponseEntity.status(HttpStatus.CREATED).body(newOrder);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateOrder(@PathVariable Long id, @Valid @RequestBody OrderRequest order) {
        Order updatedOrder = orderService.updateOrder(id, order);
        // messagingTemplate.convertAndSend("/topic/orders", updatedOrder);
        return ResponseEntity.ok(OrderMapper.toOrderResponse(updatedOrder));
    }

    @PatchMapping(path = "/patch/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<?> patchOrder(
            @PathVariable Long id,
            @RequestBody JsonNode patchNode
    ) throws JsonProcessingException, IllegalArgumentException {
        Order existingOrder = orderService.getOrderById(id);

        JsonNode existingNode = objectMapper.valueToTree(existingOrder);
        JsonNode merged = patchUtils.merge(existingNode, patchNode);
        OrderRequest orderPatch = objectMapper.treeToValue(merged, OrderRequest.class);
        validator.validate(orderPatch);

        Order savedOrder = orderService.patchOrder(id, orderPatch);
        // messagingTemplate.convertAndSend("/topic/orders", savedOrder);
        return ResponseEntity.ok(OrderMapper.toOrderResponse(savedOrder));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        // messagingTemplate.convertAndSend("/topic/orders", id);
        return ResponseEntity.noContent().build();
    }
}
