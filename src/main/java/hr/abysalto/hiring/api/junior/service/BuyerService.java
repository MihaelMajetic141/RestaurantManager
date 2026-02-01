package hr.abysalto.hiring.api.junior.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import hr.abysalto.hiring.api.junior.data.dto.BuyerRequest;
import hr.abysalto.hiring.api.junior.data.model.Buyer;
import hr.abysalto.hiring.api.junior.repository.BuyerRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class BuyerService {

	private final BuyerRepository buyerRepository;

	public Page<Buyer> getAllBuyers(String firstName, String lastName, Pageable pageable) {
		List<Buyer> all = new ArrayList<>();
		buyerRepository.findAll().forEach(all::add);

		var stream = all.stream();
		if (firstName != null && !firstName.isBlank()) {
			stream = stream.filter(b -> firstName.equalsIgnoreCase(b.getFirstName()));
		}
		if (lastName != null && !lastName.isBlank()) {
			stream = stream.filter(b -> lastName.equalsIgnoreCase(b.getLastName()));
		}
		List<Buyer> filtered = stream.toList();

		int start = (int) pageable.getOffset();
		int end = Math.min(start + pageable.getPageSize(), filtered.size());
		List<Buyer> pageContent = start < filtered.size() ? filtered.subList(start, end) : List.of();

		return new PageImpl<>(pageContent, pageable, filtered.size());
	}

	public Buyer getBuyerById(Long id) {
		if (id == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Buyer ID is required");
		}
		return buyerRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Buyer not found for id = " + id));
	}

	public Buyer createBuyer(BuyerRequest request) {
		Buyer buyer = new Buyer();
		buyer.setFirstName(request.getFirstName());
		buyer.setLastName(request.getLastName());
		buyer.setTitle(request.getTitle());
		return buyerRepository.save(buyer);
	}

	public Buyer updateBuyer(Long id, BuyerRequest request) {
		Buyer buyer = buyerRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Buyer not found for id = " + id));
		buyer.setFirstName(request.getFirstName());
		buyer.setLastName(request.getLastName());
		buyer.setTitle(request.getTitle());
		return buyerRepository.save(buyer);
	}

	public Buyer patchBuyer(Long id, BuyerRequest request) {
		Buyer buyer = buyerRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Buyer not found for id = " + id));
		if (request.getFirstName() != null) {
			buyer.setFirstName(request.getFirstName());
		}
		if (request.getLastName() != null) {
			buyer.setLastName(request.getLastName());
		}
		if (request.getTitle() != null) {
			buyer.setTitle(request.getTitle());
		}
		return buyerRepository.save(buyer);
	}

	public void deleteBuyer(Long id) {
		if (buyerRepository.findById(id).isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Buyer not found for id = " + id);
		}
		buyerRepository.deleteById(id);
	}
}
