package hr.abysalto.hiring.api.junior.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import hr.abysalto.hiring.api.junior.data.dto.AddressRequest;
import hr.abysalto.hiring.api.junior.data.dto.OrderItemRequest;
import hr.abysalto.hiring.api.junior.data.dto.OrderRequest;
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
		return orderRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found for id = " + id));
	}

	@Transactional
	public Order createOrder(OrderRequest request) {
		Buyer buyer = getBuyerByFullName(request.getBuyerName());
		BuyerAddress deliveryAddress = resolveDeliveryAddress(buyer.getId(), request.getDeliveryAddress());
		buyer.getAddresses().add(deliveryAddress);
		buyerRepository.save(buyer);

		Order order = Order.builder()
				.buyer(buyer)
				.orderStatus(OrderStatus.valueOf(request.getOrderStatus()))
				.orderTime(LocalDateTime.parse(request.getOrderTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))
				.paymentOption(PaymentOption.valueOf(request.getPaymentOption()))
				.deliveryAddress(deliveryAddress)
				.contactNumber(request.getContactNumber())
				.orderNote(request.getOrderNote())
				.currency(request.getCurrency())
				.orderItems(List.of())
				.totalPrice(BigDecimal.ZERO)
				.build();

		List<OrderItem> orderItems = createOrderItemsFromRequest(order.getId(), request.getOrderItems());
		BigDecimal totalPrice = calculateTotalPrice(orderItems);
		order.setOrderItems(orderItems);
		order.setTotalPrice(totalPrice);

		return orderRepository.save(order);
	}

	public Order updateOrderStatus(Long id, OrderStatus status) {
		Order order = orderRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found for id = " + id));
		order.setOrderStatus(status);
		try {
			return orderRepository.save(order);
		} catch (OptimisticLockingFailureException ex) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Order was updated concurrently");
		}
	}

	@Transactional
	public Order updateOrder(Long id, OrderRequest request) {
		Order order = orderRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found for id = " + id));

		Buyer buyer = getBuyerByFullName(request.getBuyerName());
		BuyerAddress deliveryAddress = resolveDeliveryAddress(buyer.getId(), request.getDeliveryAddress());
		buyer.getAddresses().add(deliveryAddress);
		buyerRepository.save(buyer);

		order.setBuyer(buyer);
		order.setOrderStatus(OrderStatus.valueOf(request.getOrderStatus()));
		order.setOrderTime(LocalDateTime.parse(request.getOrderTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
		order.setPaymentOption(PaymentOption.valueOf(request.getPaymentOption()));
		order.setDeliveryAddress(deliveryAddress);
		order.setContactNumber(request.getContactNumber());
		order.setOrderNote(request.getOrderNote());
		order.setCurrency(request.getCurrency());

		orderItemRepository.findByOrderId(id).forEach(orderItemRepository::delete);

		List<OrderItem> orderItems = createOrderItemsFromRequest(id, request.getOrderItems());
		BigDecimal totalPrice = calculateTotalPrice(orderItems);
		orderItems.forEach(orderItemRepository::save);
		order.setOrderItems(orderItems);
		order.setTotalPrice(totalPrice);

		try {
			return orderRepository.save(order);
		} catch (OptimisticLockingFailureException ex) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Order was updated concurrently");
		}
	}

	@Transactional
	public Order patchOrder(Long id, OrderRequest request) {
		Order existingOrder = orderRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found with ID: " + id));

		if (request.getBuyerName() != null) {
			existingOrder.setBuyer(getBuyerByFullName(request.getBuyerName()));
		}
		if (request.getOrderStatus() != null) {
			existingOrder.setOrderStatus(OrderStatus.valueOf(request.getOrderStatus()));
		}
		if (request.getOrderTime() != null) {
			existingOrder.setOrderTime(LocalDateTime.parse(
					request.getOrderTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
		}
		if (request.getPaymentOption() != null) {
			existingOrder.setPaymentOption(PaymentOption.valueOf(request.getPaymentOption()));
		}
		if (request.getDeliveryAddress() != null) {
			BuyerAddress deliveryAddress = resolveDeliveryAddress(
					existingOrder.getBuyer().getId(), request.getDeliveryAddress());
			existingOrder.setDeliveryAddress(deliveryAddress);
			existingOrder.getBuyer().getAddresses().add(deliveryAddress);
			buyerRepository.save(existingOrder.getBuyer());
		}
		if (request.getContactNumber() != null) {
			existingOrder.setContactNumber(request.getContactNumber());
		}
		if (request.getOrderNote() != null) {
			existingOrder.setOrderNote(request.getOrderNote());
		}
		if (request.getCurrency() != null) {
			existingOrder.setCurrency(request.getCurrency());
		}
		if (request.getOrderItems() != null) {
			existingOrder.setOrderItems(createOrderItemsFromRequest(id, request.getOrderItems()));
		}
		BigDecimal totalPrice = calculateTotalPrice(existingOrder.getOrderItems());
		existingOrder.setTotalPrice(totalPrice);

		try {
			return orderRepository.save(existingOrder);
		} catch (OptimisticLockingFailureException ex) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Order was updated concurrently");
		}
	}

	public void deleteOrder(Long id) {
		Order order = orderRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found for id = " + id));
		orderItemRepository.deleteAll(order.getOrderItems());
		orderRepository.deleteById(id);
	}

	public BigDecimal calculateTotalPrice(List<OrderItem> items) {
		if (items == null || items.isEmpty()) {
			return BigDecimal.ZERO;
		}
		return items.stream()
				.filter(item -> item.getSnapshotPrice() != null && item.getQuantity() != null)
				.map(item -> item.getSnapshotPrice()
						.multiply(BigDecimal.valueOf(item.getQuantity())))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private Buyer getBuyerByFullName(String buyerName) {
		if (buyerName == null || buyerName.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Buyer name is required");
		}
		String[] parts = buyerName.trim().split("\\s+", 2);
		String firstName = parts[0];
		String lastName = parts.length > 1 ? parts[1] : "";
		List<Buyer> buyers = buyerRepository.findByFirstNameAndLastName(firstName, lastName);
		if (buyers.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Buyer not found: " + buyerName);
		}
		return buyers.get(0);
	}

	private BuyerAddress resolveDeliveryAddress(Long buyerId, AddressRequest shippingAddress) {
		BuyerAddress address = new BuyerAddress();
		address.setBuyerId(buyerId);
		address.setStreet(shippingAddress.getStreet());
		address.setCity(shippingAddress.getCity());
		address.setHomeNumber(shippingAddress.getHomeNumber());
		return buyerAddressRepository.save(address);
	}

	private List<OrderItem> createOrderItemsFromRequest(Long orderId, List<OrderItemRequest> itemRequests) {
		if (itemRequests == null || itemRequests.isEmpty()) {
			return List.of();
		}
		List<OrderItem> orderItems = new ArrayList<>();
		for (OrderItemRequest req : itemRequests) {
			Item item = itemRepository.findById(req.getItemId()).stream()
					.findFirst()
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found for id = " + req.getItemId()));
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
