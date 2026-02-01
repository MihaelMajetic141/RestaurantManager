package hr.abysalto.hiring.api.junior.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import hr.abysalto.hiring.api.junior.data.model.Item;

@Repository
public interface ItemRepository extends CrudRepository<Item, Long> {

    @Query("SELECT * FROM ITEM WHERE ITEM_NUMBER = :itemNumber")
    List<Item> findByItemNumber(@Param("itemNumber") Short itemNumber);

    @Query("SELECT * FROM ITEM WHERE NAME = :name")
    List<Item> findByName(@Param("name") String name);

    @Query("SELECT * FROM ITEM WHERE PRICE <= :price")
    List<Item> findByPriceLowerThan(@Param("price") BigDecimal price);

    @Query("SELECT * FROM ITEM WHERE PRICE >= :price")
    List<Item> findByPriceGreaterThan(@Param("price") BigDecimal price);
}
