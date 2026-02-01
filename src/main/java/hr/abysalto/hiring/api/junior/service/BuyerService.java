package hr.abysalto.hiring.api.junior.service;

import hr.abysalto.hiring.api.junior.data.model.Buyer;
import hr.abysalto.hiring.api.junior.repository.BuyerRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class BuyerService {

	private final BuyerRepository buyerRepository;

	public Iterable<Buyer> getAllBuyers() {
		return buyerRepository.findAll();
	}

	public void save(Buyer buyer) {
		buyerRepository.save(buyer);
	}

	public Buyer getById(Long id) {
		return buyerRepository.findById(id).orElse(null);
	}

	public void deleteById(Long id) {
		buyerRepository.deleteById(id);
	}
}
