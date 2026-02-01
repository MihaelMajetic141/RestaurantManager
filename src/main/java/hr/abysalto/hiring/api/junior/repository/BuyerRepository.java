package hr.abysalto.hiring.api.junior.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import hr.abysalto.hiring.api.junior.data.model.Buyer;


@Repository
public interface BuyerRepository extends CrudRepository<Buyer, Long> {

	List<Buyer> findByFirstNameAndLastName(String firstName, String lastName);
}
