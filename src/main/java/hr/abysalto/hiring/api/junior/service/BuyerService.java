package hr.abysalto.hiring.api.junior.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import hr.abysalto.hiring.api.junior.data.dto.request.BuyerRequest;
import hr.abysalto.hiring.api.junior.data.model.Buyer;
import hr.abysalto.hiring.api.junior.data.model.BuyerAddress;
import hr.abysalto.hiring.api.junior.repository.BuyerAddressRepository;
import hr.abysalto.hiring.api.junior.repository.BuyerRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class BuyerService {

	private final BuyerRepository buyerRepository;
	private final BuyerAddressRepository buyerAddressRepository;

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

	@Transactional
	public Buyer createBuyer(BuyerRequest request) {
		Buyer buyer = new Buyer();
		buyer.setFirstName(request.getFirstName());
		buyer.setLastName(request.getLastName());
		buyer.setTitle(request.getTitle());
		buyer = buyerRepository.save(buyer);
		assignAddressesToBuyer(buyer.getId(), request.getAddressIds());
		return buyerRepository.findById(buyer.getId()).orElse(buyer);
	}

	@Transactional
	public Buyer updateBuyer(Long id, BuyerRequest request) {
		Buyer buyer = buyerRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Buyer not found for id = " + id));
		buyer.setFirstName(request.getFirstName());
		buyer.setLastName(request.getLastName());
		buyer.setTitle(request.getTitle());
		assignAddressesToBuyer(id, request.getAddressIds());
		return buyerRepository.save(buyer);
	}

	@Transactional
	public Buyer patchBuyer(Long id, BuyerRequest request, JsonNode patchNode) {
		Buyer buyer = buyerRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Buyer not found with ID: " + id));
		if (patchNode.has("firstName")) {
			buyer.setFirstName(request.getFirstName());
		}
		if (patchNode.has("lastName")) {
			buyer.setLastName(request.getLastName());
		}
		if (patchNode.has("title")) {
			buyer.setTitle(request.getTitle());
		}
		if (patchNode.has("addressIds")) {
			assignAddressesToBuyer(id, request.getAddressIds());
		}
		return buyerRepository.save(buyer);
	}

	public void deleteBuyer(Long id) {
		buyerRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Buyer not found for id = " + id));
		buyerRepository.deleteById(id);
	}

	private void assignAddressesToBuyer(Long buyerId, List<Long> addressIds) {
		if (addressIds == null || addressIds.isEmpty()) {
			return;
		}
		List<BuyerAddress> addresses = new ArrayList<>();
		for (Long addressId : addressIds) {
			BuyerAddress address = buyerAddressRepository.findById(addressId)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Buyer address not found for id = " + addressId));
			address.setBuyerId(buyerId);
			buyerAddressRepository.save(address);
			addresses.add(address);
		}
		Buyer buyer = buyerRepository.findById(buyerId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Buyer not found for id = " + buyerId));
		buyer.setAddresses(new HashSet<>(addresses));
		buyerRepository.save(buyer);
	}
}
