package hr.abysalto.hiring.api.junior.manager;

import hr.abysalto.hiring.api.junior.data.model.Buyer;
import hr.abysalto.hiring.api.junior.repository.BuyerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BuyerManagerImpl implements BuyerManager {
	@Autowired
	private BuyerRepository buyerRepository;

	@Override
	public Iterable<Buyer> getAllBuyers() {
		return this.buyerRepository.findAll();
	}

	@Override
	public void save(Buyer buyer) {
		this.buyerRepository.save(buyer);
	}

	@Override
	public Buyer getById(Long id) {
		return this.buyerRepository.findById(id).orElse(null);
	}

	@Override
	public void deleteById(Long id) {
		this.buyerRepository.deleteById(id);
	}
}
