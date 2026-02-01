package hr.abysalto.hiring.api.junior.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import hr.abysalto.hiring.api.junior.data.model.OrderItem;

@Repository
public interface OrderItemRepository extends CrudRepository<OrderItem, Long> {
    @Query("SELECT * FROM ORDER_ITEM WHERE ORDER_ID = :orderId")
    List<OrderItem> findByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT * FROM ORDER_ITEM WHERE ITEM_NUMBER = :itemNumber")
    List<OrderItem> findByItemNumber(@Param("itemNumber") Short itemNumber);

    @Query("SELECT * FROM ORDER_ITEM WHERE NAME = :name")
    List<OrderItem> findByName(@Param("name") String name);

    @Query("SELECT * FROM ORDER_ITEM WHERE PRICE <= :price")
    List<OrderItem> findByPriceLowerThan(@Param("price") BigDecimal price);

    @Query("SELECT * FROM ORDER_ITEM WHERE PRICE >= :price")
    List<OrderItem> findByPriceGreaterThan(@Param("price") BigDecimal price);

    @Query("SELECT * FROM ORDER_ITEM WHERE PRICE BETWEEN :minPrice AND :maxPrice")
    List<OrderItem> findByPriceBetween(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

}
