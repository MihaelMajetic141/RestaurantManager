package hr.abysalto.hiring.api.junior.repository;

import java.util.List;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import hr.abysalto.hiring.api.junior.data.enums.OrderStatus;
import hr.abysalto.hiring.api.junior.data.model.Order;

@Repository
public interface OrderRepository extends CrudRepository<Order, Long>, PagingAndSortingRepository<Order, Long> {

	List<Order> findAllByOrderByTotalPriceAsc();

	List<Order> findAllByOrderByTotalPriceDesc();

	@Query("SELECT * FROM ORDERS WHERE ORDER_STATUS = :status")
	List<Order> findByOrderStatus(@Param("status") String status);

	default List<Order> findByOrderStatus(OrderStatus status) {
		return status != null ? findByOrderStatus(status.name()) : List.of();
	}
}
