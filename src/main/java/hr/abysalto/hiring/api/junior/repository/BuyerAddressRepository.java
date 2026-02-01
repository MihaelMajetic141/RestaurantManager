package hr.abysalto.hiring.api.junior.repository;

import java.util.List;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import hr.abysalto.hiring.api.junior.data.model.BuyerAddress;


@Repository
public interface BuyerAddressRepository extends CrudRepository<BuyerAddress, Long> {

    @Query("SELECT * FROM BUYER_ADDRESS WHERE BUYER_ID = :buyerId")
    List<BuyerAddress> findByBuyerId(@Param("buyerId") Long buyerId);

    @Query("SELECT * FROM BUYER_ADDRESS WHERE CITY = :city")
    List<BuyerAddress> findByCity(String city);

    @Query("SELECT * FROM BUYER_ADDRESS WHERE STREET = :street")
    List<BuyerAddress> findByStreet(String street);

}

