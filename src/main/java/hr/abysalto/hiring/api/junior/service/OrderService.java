package hr.abysalto.hiring.api.junior.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import hr.abysalto.hiring.api.junior.data.dto.request.AddressRequest;
import hr.abysalto.hiring.api.junior.data.dto.request.OrderItemRequest;
import hr.abysalto.hiring.api.junior.data.dto.request.OrderRequest;
import hr.abysalto.hiring.api.junior.data.enums.OrderStatus;
import hr.abysalto.hiring.api.junior.data.enums.PaymentOption;
import hr.abysalto.hiring.api.junior.data.model.Buyer;
import hr.abysalto.hiring.api.junior.data.model.BuyerAddress;
import hr.abysalto.hiring.api.junior.data.model.Item;
import hr.abysalto.hiring.api.junior.data.model.Order;
import hr.abysalto.hiring.api.junior.data.model.OrderItem;
import hr.abysalto.hiring.api.junior.repository.BuyerAddressRepository;
import hr.abysalto.hiring.api.junior.repository.BuyerRepository;
import hr.abysalto.hiring.api.junior.repository.ItemRepository;
import hr.abysalto.hiring.api.junior.repository.OrderItemRepository;
import hr.abysalto.hiring.api.junior.repository.OrderRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class OrderService {

	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;
	private final ItemRepository itemRepository;
	private final BuyerRepository buyerRepository;
	private final BuyerAddressRepository buyerAddressRepository;

	public Page<Order> getAllOrders(String orderStatus, String paymentOption, String currency, Pageable pageable) {
		List<Order> allOrders = new ArrayList<>();
		orderRepository.findAll().forEach(allOrders::add);
		populateOrderBuyerAndAddress(allOrders);

		var stream = allOrders.stream();
		if (orderStatus != null && !orderStatus.isBlank()) {
			stream = stream.filter(o ->
				orderStatus.equalsIgnoreCase(o.getOrderStatus() != null ? o.getOrderStatus().name() : null));
		}
		if (paymentOption != null && !paymentOption.isBlank()) {
			stream = stream.filter(o -> paymentOption.equalsIgnoreCase(
					o.getPaymentOption() != null ? o.getPaymentOption().name() : null));
		}
		if (currency != null && !currency.isBlank()) {
			stream = stream.filter(o -> currency.equalsIgnoreCase(o.getCurrency()));
		}
		List<Order> filtered = stream.toList();

		int start = (int) pageable.getOffset();
		int end = Math.min(start + pageable.getPageSize(), filtered.size());
		List<Order> pageContent = start < filtered.size() ? filtered.subList(start, end) : List.of();

		return new PageImpl<>(pageContent, pageable, filtered.size());
	}

	public Order getOrderById(Long id) {
		if (id == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order ID is required");
		}
		Order order = orderRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found for id = " + id));
		populateOrderBuyerAndAddress(List.of(order));
		return order;
	}

	@Transactional
	public Order createOrder(OrderRequest request) {
		Buyer buyer = buyerRepository.findById(request.getBuyerId())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Buyer not found for id = " + request.getBuyerId()));
		BuyerAddress deliveryAddress = saveBuyerDeliveryAddress(buyer.getId(), request.getDeliveryAddress());

		Order order = Order.builder()
				.orderStatus(OrderStatus.valueOf(request.getOrderStatus()))
				.orderTime(LocalDateTime.parse(
                    request.getOrderTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))
				.paymentOption(PaymentOption.valueOf(request.getPaymentOption()))
				.contactNumber(request.getContactNumber())
				.orderNote(request.getOrderNote())
				.currency(request.getCurrency())
				.orderItems(Set.of())
				.totalPrice(BigDecimal.ZERO)
				.build();
		order.setBuyer(buyer);
		order.setDeliveryAddress(deliveryAddress);

		order = orderRepository.save(order);
		List<OrderItem> orderItems = createOrderItemsFromRequest(order.getId(), request.getOrderItems());
		BigDecimal totalPrice = calculateTotalPrice(orderItems);
		order.setOrderItems(new HashSet<>(orderItems));
		order.setTotalPrice(totalPrice);
		order.setBuyer(buyer);
		order.setDeliveryAddress(deliveryAddress);

		return orderRepository.save(order);
	}

	@Transactional
	public Order updateOrder(Long id, OrderRequest request) {
		Order existingOrder = orderRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found for id = " + id));

        BuyerAddress deliveryAddress = saveBuyerDeliveryAddress(request.getBuyerId(), request.getDeliveryAddress());

        List<OrderItem> orderItems = createOrderItemsFromRequest(id, request.getOrderItems());

        orderItemRepository.findByOrderId(id).forEach(orderItemRepository::delete);
        orderItems.forEach(orderItemRepository::save);

        Buyer buyer = buyerRepository.findById(request.getBuyerId())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "No buyer found for ID:" + request.getBuyerId()));

        existingOrder.setBuyer(buyer);
		existingOrder.setOrderStatus(OrderStatus.valueOf(request.getOrderStatus()));
		existingOrder.setOrderTime(LocalDateTime.parse(request.getOrderTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
		existingOrder.setPaymentOption(PaymentOption.valueOf(request.getPaymentOption()));
		existingOrder.setDeliveryAddress(deliveryAddress);
		existingOrder.setContactNumber(request.getContactNumber());
		existingOrder.setOrderNote(request.getOrderNote());
		existingOrder.setCurrency(request.getCurrency());

		BigDecimal totalPrice = calculateTotalPrice(orderItems);
		existingOrder.setOrderItems(new HashSet<>(orderItems));
		existingOrder.setTotalPrice(totalPrice);
		Order saved = orderRepository.save(existingOrder);
		populateOrderBuyerAndAddress(List.of(saved));
		return saved;
	}

	@Transactional
	public Order patchOrder(Long id, OrderRequest request, JsonNode patchNode) {
		Order existingOrder = orderRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found with ID: " + id));

		if (patchNode.has("buyerId") && request.getBuyerId() != null) {
            Buyer existingBuyer = buyerRepository.findById(request.getBuyerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Buyer not found for ID: " + request.getBuyerId()));
			existingOrder.setBuyer(existingBuyer);
		}
		if (patchNode.has("orderStatus") && request.getOrderStatus() != null) {
			existingOrder.setOrderStatus(OrderStatus.valueOf(request.getOrderStatus()));
		}
		if (patchNode.has("orderTime") && request.getOrderTime() != null) {
			existingOrder.setOrderTime(LocalDateTime.parse(
					request.getOrderTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
		}
		if (patchNode.has("paymentOption") && request.getPaymentOption() != null) {
			existingOrder.setPaymentOption(PaymentOption.valueOf(request.getPaymentOption()));
		}
		if (patchNode.has("deliveryAddress") && request.getDeliveryAddress() != null) {
			BuyerAddress deliveryAddress = saveBuyerDeliveryAddress(
					existingOrder.getBuyer().getId(), request.getDeliveryAddress());
            existingOrder.setDeliveryAddress(deliveryAddress);
		}
		if (patchNode.has("contactNumber")) {
			existingOrder.setContactNumber(request.getContactNumber());
		}
		if (patchNode.has("orderNote")) {
			existingOrder.setOrderNote(request.getOrderNote());
		}
		if (patchNode.has("currency")) {
			existingOrder.setCurrency(request.getCurrency());
		}
		if (patchNode.has("orderItems") && request.getOrderItems() != null) {
            List<OrderItem> orderItems = createOrderItemsFromRequest(id, request.getOrderItems());
            orderItems.forEach(orderItemRepository::save);
            existingOrder.setOrderItems(new HashSet<>(orderItems));
		}
		if (patchNode.has("totalPrice")) {
			JsonNode totalNode = patchNode.get("totalPrice");
			if (totalNode != null && !totalNode.isNull()) {
				BigDecimal value = totalNode.isNumber()
						? totalNode.decimalValue()
						: new BigDecimal(totalNode.asText());
				existingOrder.setTotalPrice(value);
			}
		} else if (patchNode.has("orderItems")) {
			existingOrder.setTotalPrice(calculateTotalPrice(existingOrder.getOrderItems()));
		}

		Order saved = orderRepository.save(existingOrder);
		populateOrderBuyerAndAddress(List.of(saved));
		return saved;
	}

	public void deleteOrder(Long id) {
		Order order = orderRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found for id = " + id));
		populateOrderBuyerAndAddress(List.of(order));
		orderItemRepository.deleteAll(order.getOrderItems());
		orderRepository.deleteById(id);
	}

	public BigDecimal calculateTotalPrice(Collection<OrderItem> items) {
		if (items == null || items.isEmpty()) {
			return BigDecimal.ZERO;
		}
		return items.stream()
				.filter(item -> item.getSnapshotPrice() != null && item.getQuantity() != null)
				.map(item -> item.getSnapshotPrice()
						.multiply(BigDecimal.valueOf(item.getQuantity())))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private BuyerAddress saveBuyerDeliveryAddress(Long buyerId, AddressRequest shippingAddress) {
        Buyer buyer = buyerRepository.findById(buyerId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No buyer found for ID:" + buyerId));

        BuyerAddress existing = null;
        if (buyer.getAddresses() != null) {
            for (BuyerAddress a : buyer.getAddresses()) {
                if (shippingAddress.getCity().equals(a.getCity())
                    && shippingAddress.getStreet().equals(a.getStreet())
                    && (shippingAddress.getHomeNumber() == null ? a.getHomeNumber() == null
                        : shippingAddress.getHomeNumber().equals(a.getHomeNumber()))) {
                    existing = a;
                    break;
                }
            }
        }
        if (existing != null) {
            return existing;
        }
        BuyerAddress buyerAddress = BuyerAddress.builder()
            .buyerId(buyerId)
            .city(shippingAddress.getCity())
            .street(shippingAddress.getStreet())
            .homeNumber(shippingAddress.getHomeNumber())
            .build();
        return buyerAddressRepository.save(buyerAddress);
    }

	private void populateOrderBuyerAndAddress(List<Order> orders) {
        orders.forEach(order -> {
            if (order.getBuyerId() != null)
                order.setBuyer(buyerRepository.findById(order.getBuyerId()).orElse(null));
            if (order.getDeliveryAddressId() != null)
                order.setDeliveryAddress(
                    buyerAddressRepository.findById(order.getDeliveryAddressId()).orElse(null));
        });
	}

	private List<OrderItem> createOrderItemsFromRequest(Long orderId, List<OrderItemRequest> itemRequests) {
        if (orderRepository.findById(orderId).isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found for id = " + orderId);
		if (itemRequests == null || itemRequests.isEmpty())
			return List.of();
		List<OrderItem> orderItems = new ArrayList<>();
		for (OrderItemRequest req : itemRequests) {
			Item item = itemRepository.findById(req.getItemId()).stream()
					.findFirst()
					.orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Item not found for id = " + req.getItemId()));
			OrderItem orderItem = new OrderItem();
			orderItem.setItemId(item.getId());
			orderItem.setOrderId(orderId);
			orderItem.setSnapshotPrice(item.getPrice());
			orderItem.setQuantity(req.getQuantity());
			orderItems.add(orderItem);
		}
		return orderItems;
	}
}
