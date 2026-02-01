package hr.abysalto.hiring.api.junior.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hr.abysalto.hiring.api.junior.components.PatchUtils;
import hr.abysalto.hiring.api.junior.components.mapper.BuyerAddressMapper;
import hr.abysalto.hiring.api.junior.data.dto.BuyerAddressRequest;
import hr.abysalto.hiring.api.junior.data.model.BuyerAddress;
import hr.abysalto.hiring.api.junior.service.BuyerAddressService;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/buyer-address")
@AllArgsConstructor
public class BuyerAddressController {

	private final BuyerAddressService buyerAddressService;
	private final ObjectMapper objectMapper;
	private final PatchUtils patchUtils;
	private final Validator validator;

	@GetMapping("/getAll")
	public ResponseEntity<?> getBuyerAddresses(
			@RequestParam(required = false) Long buyerId,
			@RequestParam(required = false) String city,
			@RequestParam(required = false) String street,
			@PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable
	) {
		Page<BuyerAddress> page = buyerAddressService.getAllBuyerAddresses(buyerId, city, street, pageable);
		return ResponseEntity.ok(page.getContent());
	}

	@GetMapping("/get/{id}")
	public ResponseEntity<?> getBuyerAddressById(@PathVariable Long id) {
		BuyerAddress address = buyerAddressService.getBuyerAddressById(id);
		return ResponseEntity.ok(BuyerAddressMapper.toBuyerAddressResponse(address));
	}

	@PostMapping("/create")
	public ResponseEntity<?> createBuyerAddress(@Valid @RequestBody BuyerAddressRequest request) {
		BuyerAddress newAddress = buyerAddressService.createBuyerAddress(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(newAddress);
	}

	@PutMapping("/update/{id}")
	public ResponseEntity<?> updateBuyerAddress(@PathVariable Long id, @Valid @RequestBody BuyerAddressRequest request) {
		BuyerAddress updatedAddress = buyerAddressService.updateBuyerAddress(id, request);
		return ResponseEntity.ok(BuyerAddressMapper.toBuyerAddressResponse(updatedAddress));
	}

	@PatchMapping(path = "/patch/{id}", consumes = "application/merge-patch+json")
	public ResponseEntity<?> patchBuyerAddress(
			@PathVariable Long id,
			@RequestBody JsonNode patchNode
	) throws JsonProcessingException, IllegalArgumentException {
		BuyerAddress existingAddress = buyerAddressService.getBuyerAddressById(id);

		JsonNode existingNode = objectMapper.valueToTree(existingAddress);
		JsonNode merged = patchUtils.merge(existingNode, patchNode);
		BuyerAddressRequest addressPatch = objectMapper.treeToValue(merged, BuyerAddressRequest.class);
		validator.validate(addressPatch);

		BuyerAddress savedAddress = buyerAddressService.patchBuyerAddress(id, addressPatch);
		return ResponseEntity.ok(BuyerAddressMapper.toBuyerAddressResponse(savedAddress));
	}

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<?> deleteBuyerAddress(@PathVariable Long id) {
		buyerAddressService.deleteBuyerAddress(id);
		return ResponseEntity.noContent().build();
	}
}
