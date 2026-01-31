package hr.abysalto.hiring.api.junior.repository;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import hr.abysalto.hiring.api.junior.data.model.Buyer;

@Repository
public interface BuyerRepository extends CrudRepository<Buyer, Long> { //PagingAndSortingRepository

	@Modifying
	@Query("UPDATE buyer SET first_name = :name WHERE id = :id")
	boolean updateByFirstName(@Param("id") Long id, @Param("name") String name);
}
