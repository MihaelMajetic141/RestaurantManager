package hr.abysalto.hiring.api.junior.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import hr.abysalto.hiring.api.junior.data.dto.BuyerAddressRequest;
import hr.abysalto.hiring.api.junior.data.model.BuyerAddress;
import hr.abysalto.hiring.api.junior.repository.BuyerAddressRepository;
import hr.abysalto.hiring.api.junior.repository.BuyerRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class BuyerAddressService {

	private BuyerAddressRepository buyerAddressRepository;
	private BuyerRepository buyerRepository;

	public Page<BuyerAddress> getAllBuyerAddresses(Long buyerId, String city, String street, Pageable pageable) {
		List<BuyerAddress> all = new ArrayList<>();
		buyerAddressRepository.findAll().forEach(all::add);

		var stream = all.stream();
		if (buyerId != null) {
			stream = stream.filter(a -> buyerId.equals(a.getBuyerId()));
		}
		if (city != null && !city.isBlank()) {
			stream = stream.filter(a -> city.equalsIgnoreCase(a.getCity()));
		}
		if (street != null && !street.isBlank()) {
			stream = stream.filter(a -> street.equalsIgnoreCase(a.getStreet()));
		}
		List<BuyerAddress> filtered = stream.toList();

		int start = (int) pageable.getOffset();
		int end = Math.min(start + pageable.getPageSize(), filtered.size());
		List<BuyerAddress> pageContent = start < filtered.size() ? filtered.subList(start, end) : List.of();

		return new PageImpl<>(pageContent, pageable, filtered.size());
	}

	public BuyerAddress getBuyerAddressById(Long id) {
		if (id == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Buyer address ID is required");
		}
		return buyerAddressRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Buyer address not found for id = " + id));
	}

	public BuyerAddress createBuyerAddress(BuyerAddressRequest request) {
		validateBuyerExists(request.getBuyerId());
		BuyerAddress address = new BuyerAddress();
		address.setBuyerId(request.getBuyerId());
		address.setCity(request.getCity());
		address.setStreet(request.getStreet());
		address.setHomeNumber(request.getHomeNumber());
		return buyerAddressRepository.save(address);
	}

	public BuyerAddress updateBuyerAddress(Long id, BuyerAddressRequest request) {
		BuyerAddress address = buyerAddressRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Buyer address not found for id = " + id));
		validateBuyerExists(request.getBuyerId());
		address.setBuyerId(request.getBuyerId());
		address.setCity(request.getCity());
		address.setStreet(request.getStreet());
		address.setHomeNumber(request.getHomeNumber());
		return buyerAddressRepository.save(address);
	}

	public BuyerAddress patchBuyerAddress(Long id, BuyerAddressRequest request) {
		BuyerAddress address = buyerAddressRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Buyer address not found for id = " + id));
		if (request.getBuyerId() != null) {
			validateBuyerExists(request.getBuyerId());
			address.setBuyerId(request.getBuyerId());
		}
		if (request.getCity() != null) {
			address.setCity(request.getCity());
		}
		if (request.getStreet() != null) {
			address.setStreet(request.getStreet());
		}
		if (request.getHomeNumber() != null) {
			address.setHomeNumber(request.getHomeNumber());
		}
		return buyerAddressRepository.save(address);
	}

	public void deleteBuyerAddress(Long id) {
		if (buyerAddressRepository.findById(id).isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Buyer address not found for id = " + id);
		}
		buyerAddressRepository.deleteById(id);
	}

	private void validateBuyerExists(Long buyerId) {
		if (buyerId == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Buyer ID is required");
		}
		if (buyerRepository.findById(buyerId).isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Buyer not found for id = " + buyerId);
		}
	}
}
