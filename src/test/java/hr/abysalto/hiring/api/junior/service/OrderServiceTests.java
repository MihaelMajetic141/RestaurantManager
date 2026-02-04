package hr.abysalto.hiring.api.junior.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTests {

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private OrderItemRepository orderItemRepository;

	@Mock
	private ItemRepository itemRepository;

	@Mock
	private BuyerRepository buyerRepository;

	@Mock
	private BuyerAddressRepository buyerAddressRepository;

	@InjectMocks
	private OrderService orderService;

	private Order order;
	private OrderRequest orderRequest;
	private Buyer buyer;
	private BuyerAddress deliveryAddress;
	private Item item;
	private OrderItem orderItem;
	private Pageable pageable;

	@BeforeEach
	void setUp() {
		buyer = new Buyer();
		buyer.setId(1L);
		buyer.setFirstName("John");
		buyer.setLastName("Doe");
		buyer.setAddresses(new HashSet<>());

		deliveryAddress = BuyerAddress.builder()
				.id(1L)
				.buyerId(1L)
				.city("Zagreb")
				.street("Main St")
				.homeNumber("10")
				.build();

		item = new Item();
		item.setId(1L);
		item.setName("Test Item");
		item.setPrice(new BigDecimal("19.99"));

		orderItem = new OrderItem();
		orderItem.setId(1L);
		orderItem.setOrderId(1L);
		orderItem.setItemId(1L);
		orderItem.setSnapshotPrice(new BigDecimal("19.99"));
		orderItem.setQuantity((short) 2);

		order = Order.builder()
				.id(1L)
				.buyerId(1L)
				.deliveryAddressId(1L)
				.buyer(buyer)
				.orderStatus(OrderStatus.WAITING_FOR_CONFIRMATION)
				.orderTime(LocalDateTime.now())
				.paymentOption(PaymentOption.CARD_UPFRONT)
				.deliveryAddress(deliveryAddress)
				.contactNumber("+385123456789")
				.orderNote("Test note")
				.currency("EUR")
				.totalPrice(new BigDecimal("39.98"))
				.orderItems(new HashSet<>(Set.of(orderItem)))
				.build();

		AddressRequest addressRequest = new AddressRequest("Zagreb", "Main St", "10");
		OrderItemRequest orderItemRequest = new OrderItemRequest(null, 1L, (short) 2);

		orderRequest = new OrderRequest(
				1L,
				OrderStatus.WAITING_FOR_CONFIRMATION.name(),
				"2025-02-01T10:00:00",
				PaymentOption.CARD_UPFRONT.name(),
				addressRequest,
				"+385123456789",
				"Test note",
				List.of(orderItemRequest),
				"EUR",
				new BigDecimal("39.98")
		);

		pageable = PageRequest.of(0, 10);
	}

	@Test
	void getAllOrders_NoFilters_ReturnsAllOrders() {
		List<Order> orders = List.of(order);
		when(orderRepository.findAll()).thenReturn(orders);
		when(buyerRepository.findById(1L)).thenReturn(Optional.of(buyer));
		when(buyerAddressRepository.findById(1L)).thenReturn(Optional.of(deliveryAddress));

		var result = orderService.getAllOrders(null, null, null, pageable);

		assertEquals(1, result.getContent().size());
		assertEquals(order, result.getContent().get(0));
		verify(orderRepository).findAll();
	}

	@Test
	void getAllOrders_WithOrderStatusFilter_ReturnsFilteredOrders() {
		List<Order> orders = List.of(order);
		when(orderRepository.findAll()).thenReturn(orders);
		when(buyerRepository.findById(1L)).thenReturn(Optional.of(buyer));
		when(buyerAddressRepository.findById(1L)).thenReturn(Optional.of(deliveryAddress));

		var result = orderService.getAllOrders(OrderStatus.WAITING_FOR_CONFIRMATION.name(), null, null, pageable);

		assertEquals(1, result.getContent().size());
		verify(orderRepository).findAll();
	}

	@Test
	void getAllOrders_WithPaymentOptionFilter_ReturnsFilteredOrders() {
		List<Order> orders = List.of(order);
		when(orderRepository.findAll()).thenReturn(orders);
		when(buyerRepository.findById(1L)).thenReturn(Optional.of(buyer));
		when(buyerAddressRepository.findById(1L)).thenReturn(Optional.of(deliveryAddress));

		var result = orderService.getAllOrders(null, PaymentOption.CARD_UPFRONT.name(), null, pageable);

		assertEquals(1, result.getContent().size());
		verify(orderRepository).findAll();
	}

	@Test
	void getAllOrders_WithCurrencyFilter_ReturnsFilteredOrders() {
		List<Order> orders = List.of(order);
		when(orderRepository.findAll()).thenReturn(orders);
		when(buyerRepository.findById(1L)).thenReturn(Optional.of(buyer));
		when(buyerAddressRepository.findById(1L)).thenReturn(Optional.of(deliveryAddress));

		var result = orderService.getAllOrders(null, null, "EUR", pageable);

		assertEquals(1, result.getContent().size());
		verify(orderRepository).findAll();
	}

	@Test
	void getAllOrders_WithFilters_ReturnsEmptyWhenNoMatch() {
		when(orderRepository.findAll()).thenReturn(List.of(order));
		when(buyerRepository.findById(1L)).thenReturn(Optional.of(buyer));
		when(buyerAddressRepository.findById(1L)).thenReturn(Optional.of(deliveryAddress));

		var result = orderService.getAllOrders(OrderStatus.DONE.name(), null, null, pageable);

		assertTrue(result.getContent().isEmpty());
		verify(orderRepository).findAll();
	}

	@Test
	void getOrderById_ExistingId_ReturnsOrder() {
		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
		when(buyerRepository.findById(1L)).thenReturn(Optional.of(buyer));
		when(buyerAddressRepository.findById(1L)).thenReturn(Optional.of(deliveryAddress));

		Order result = orderService.getOrderById(1L);

		assertNotNull(result);
		assertEquals(order, result);
		verify(orderRepository).findById(1L);
		verify(buyerRepository).findById(1L);
		verify(buyerAddressRepository).findById(1L);
	}

	@Test
	void getOrderById_NonExistingId_ThrowsNotFound() {
		when(orderRepository.findById(1L)).thenReturn(Optional.empty());

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> orderService.getOrderById(1L));

		assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
		assertEquals("Order not found for id = 1", exception.getReason());
		verify(orderRepository).findById(1L);
	}

	@Test
	void getOrderById_NullId_ThrowsBadRequest() {
		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> orderService.getOrderById(null));

		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
		assertEquals("Order ID is required", exception.getReason());
		verifyNoInteractions(orderRepository);
	}

	@Test
	void createOrder_Success() {
		when(buyerRepository.findById(1L)).thenReturn(Optional.of(buyer));
		when(buyerAddressRepository.save(any(BuyerAddress.class))).thenReturn(deliveryAddress);
		when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
		when(orderRepository.save(any(Order.class))).thenReturn(order);

		Order result = orderService.createOrder(orderRequest);

		assertNotNull(result);

		ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
		verify(orderRepository, times(2)).save(orderCaptor.capture());
		Order capturedOrder = orderCaptor.getValue();
		assertEquals(buyer, capturedOrder.getBuyer());
		assertNotNull(capturedOrder.getDeliveryAddress());
		assertEquals(1L, capturedOrder.getDeliveryAddress().getBuyerId());
		assertEquals("Zagreb", capturedOrder.getDeliveryAddress().getCity());
		assertEquals("Main St", capturedOrder.getDeliveryAddress().getStreet());
		assertEquals("10", capturedOrder.getDeliveryAddress().getHomeNumber());
		assertEquals(OrderStatus.WAITING_FOR_CONFIRMATION, capturedOrder.getOrderStatus());
		assertEquals(PaymentOption.CARD_UPFRONT, capturedOrder.getPaymentOption());
		assertEquals("EUR", capturedOrder.getCurrency());
	}

	@Test
	void createOrder_BuyerNotFound_ThrowsNotFound() {
		when(buyerRepository.findById(1L)).thenReturn(Optional.empty());

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> orderService.createOrder(orderRequest));

		assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
		assertEquals("Buyer not found for id = 1", exception.getReason());
		verify(buyerRepository).findById(1L);
		verifyNoInteractions(orderRepository);
	}

	@Test
	void createOrder_ItemNotFound_ThrowsNotFound() {
		when(buyerRepository.findById(1L)).thenReturn(Optional.of(buyer));
		when(buyerAddressRepository.save(any(BuyerAddress.class))).thenReturn(deliveryAddress);
		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
		when(orderRepository.save(any(Order.class))).thenReturn(order);
		when(itemRepository.findById(1L)).thenReturn(Optional.empty());

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> orderService.createOrder(orderRequest));

		assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
		assertTrue(exception.getReason().contains("Item not found"));
		verify(orderRepository).save(any(Order.class));
	}

	@Test
	void createOrder_EmptyOrderItems_Success() {
		orderRequest.setOrderItems(List.of());
		when(buyerRepository.findById(1L)).thenReturn(Optional.of(buyer));
		when(buyerAddressRepository.save(any(BuyerAddress.class))).thenReturn(deliveryAddress);
		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
		when(orderRepository.save(any(Order.class))).thenReturn(order);

		Order result = orderService.createOrder(orderRequest);

		assertNotNull(result);
		verify(orderRepository, times(2)).save(any(Order.class));
	}

	@Test
	void updateOrder_Success() {
		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
		when(buyerRepository.findById(1L)).thenReturn(Optional.of(buyer));
		when(buyerAddressRepository.save(any(BuyerAddress.class))).thenReturn(deliveryAddress);
		when(orderItemRepository.findByOrderId(1L)).thenReturn(List.of(orderItem));
		when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
		when(orderItemRepository.save(any(OrderItem.class))).thenReturn(orderItem);
		when(orderRepository.save(any(Order.class))).thenReturn(order);

		Order result = orderService.updateOrder(1L, orderRequest);

		assertNotNull(result);

		verify(orderRepository, times(2)).findById(1L);
		verify(orderItemRepository).findByOrderId(1L);
		verify(orderRepository).save(any(Order.class));
	}

	@Test
	void updateOrder_NonExistingId_ThrowsNotFound() {
		when(orderRepository.findById(1L)).thenReturn(Optional.empty());

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> orderService.updateOrder(1L, orderRequest));

		assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
		assertEquals("Order not found for id = 1", exception.getReason());
		verify(orderRepository).findById(1L);
		verify(orderRepository, never()).save(any(Order.class));
	}

	@Test
	void patchOrder_Success() {
		ObjectNode patchNode = JsonNodeFactory.instance.objectNode();
		patchNode.put("buyerId", 1L);
		patchNode.put("orderStatus", "WAITING_FOR_CONFIRMATION");
		patchNode.put("orderTime", "2026-01-01T12:00:00");
		patchNode.put("paymentOption", "CASH");
		patchNode.set("deliveryAddress", JsonNodeFactory.instance.objectNode());
		patchNode.put("contactNumber", "+123");
		patchNode.put("orderNote", "Note");
		patchNode.put("currency", "USD");

		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
		when(buyerRepository.findById(1L)).thenReturn(Optional.of(buyer));
		when(buyerAddressRepository.save(any(BuyerAddress.class))).thenReturn(deliveryAddress);
		when(orderRepository.save(any(Order.class))).thenReturn(order);

		Order result = orderService.patchOrder(1L, orderRequest, patchNode);

		assertNotNull(result);
		verify(orderRepository).save(any(Order.class));
	}

	@Test
	void patchOrder_PartialUpdate_OnlyOrderStatus() {
		OrderRequest patchRequest = new OrderRequest();
		patchRequest.setOrderStatus(OrderStatus.DONE.name());

		ObjectNode patchNode = JsonNodeFactory.instance.objectNode();
		patchNode.put("orderStatus", OrderStatus.DONE.name());

		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
		when(orderRepository.save(any(Order.class))).thenReturn(order);

		Order result = orderService.patchOrder(1L, patchRequest, patchNode);

		assertNotNull(result);
		assertEquals(OrderStatus.DONE, order.getOrderStatus());
		verify(orderRepository).save(any(Order.class));
	}

	@Test
	void patchOrder_NonExistingId_ThrowsNotFound() {
		ObjectNode patchNode = JsonNodeFactory.instance.objectNode();
		when(orderRepository.findById(1L)).thenReturn(Optional.empty());

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> orderService.patchOrder(1L, orderRequest, patchNode));

		assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
		assertEquals("Order not found with ID: 1", exception.getReason());
		verify(orderRepository).findById(1L);
		verify(orderRepository, never()).save(any(Order.class));
	}

	@Test
	void deleteOrder_Success() {
		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
		when(buyerRepository.findById(1L)).thenReturn(Optional.of(buyer));
		when(buyerAddressRepository.findById(1L)).thenReturn(Optional.of(deliveryAddress));
		doNothing().when(orderRepository).deleteById(1L);

		orderService.deleteOrder(1L);

		verify(orderRepository).findById(1L);
		verify(orderItemRepository).deleteAll(order.getOrderItems());
		verify(orderRepository).deleteById(1L);
	}

	@Test
	void deleteOrder_NonExistingId_ThrowsNotFound() {
		when(orderRepository.findById(1L)).thenReturn(Optional.empty());

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> orderService.deleteOrder(1L));

		assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
		assertEquals("Order not found for id = 1", exception.getReason());
		verify(orderRepository).findById(1L);
		verify(orderItemRepository, never()).deleteAll(anyList());
		verify(orderRepository, never()).deleteById(anyLong());
	}

	@Test
	void calculateTotalPrice_WithItems_ReturnsCorrectTotal() {
		OrderItem item1 = new OrderItem();
		item1.setSnapshotPrice(new BigDecimal("10.00"));
		item1.setQuantity((short) 2);

		OrderItem item2 = new OrderItem();
		item2.setSnapshotPrice(new BigDecimal("5.50"));
		item2.setQuantity((short) 3);

		BigDecimal total = orderService.calculateTotalPrice(List.of(item1, item2));

		assertEquals(new BigDecimal("36.50"), total);
	}

	@Test
	void calculateTotalPrice_EmptyList_ReturnsZero() {
		BigDecimal total = orderService.calculateTotalPrice(List.of());

		assertEquals(BigDecimal.ZERO, total);
	}

	@Test
	void calculateTotalPrice_NullList_ReturnsZero() {
		BigDecimal total = orderService.calculateTotalPrice(null);

		assertEquals(BigDecimal.ZERO, total);
	}
}
