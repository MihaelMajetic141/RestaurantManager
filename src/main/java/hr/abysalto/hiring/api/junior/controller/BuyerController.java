package hr.abysalto.hiring.api.junior.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hr.abysalto.hiring.api.junior.components.PatchUtils;
import hr.abysalto.hiring.api.junior.components.mapper.BuyerMapper;
import hr.abysalto.hiring.api.junior.data.dto.BuyerRequest;
import hr.abysalto.hiring.api.junior.data.model.Buyer;
import hr.abysalto.hiring.api.junior.service.BuyerService;
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
@RequestMapping("/api/buyer")
@AllArgsConstructor
public class BuyerController {

	private final BuyerService buyerService;
	private final ObjectMapper objectMapper;
	private final PatchUtils patchUtils;
	private final Validator validator;

	@GetMapping("/getAll")
	public ResponseEntity<?> getBuyers(
			@RequestParam(required = false) String firstName,
			@RequestParam(required = false) String lastName,
			@PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable
	) {
		Page<Buyer> page = buyerService.getAllBuyers(firstName, lastName, pageable);
		return ResponseEntity.ok(page.getContent());
	}

	@GetMapping("/get/{id}")
	public ResponseEntity<?> getBuyerById(@PathVariable Long id) {
		Buyer buyer = buyerService.getBuyerById(id);
		return ResponseEntity.ok(BuyerMapper.toBuyerResponse(buyer));
	}

	@PostMapping("/create")
	public ResponseEntity<?> createBuyer(@RequestBody BuyerRequest request) {
		Buyer newBuyer = buyerService.createBuyer(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(newBuyer);
	}

	@PutMapping("/update/{id}")
	public ResponseEntity<?> updateBuyer(@PathVariable Long id, @RequestBody BuyerRequest request) {
		Buyer updatedBuyer = buyerService.updateBuyer(id, request);
		return ResponseEntity.ok(BuyerMapper.toBuyerResponse(updatedBuyer));
	}

	@PatchMapping(path = "/patch/{id}", consumes = "application/merge-patch+json")
	public ResponseEntity<?> patchBuyer(
			@PathVariable Long id,
			@RequestBody JsonNode patchNode
	) throws JsonProcessingException, IllegalArgumentException {
		Buyer existingBuyer = buyerService.getBuyerById(id);

		JsonNode existingNode = objectMapper.valueToTree(existingBuyer);
		JsonNode merged = patchUtils.merge(existingNode, patchNode);
		BuyerRequest buyerPatch = objectMapper.treeToValue(merged, BuyerRequest.class);
		validator.validate(buyerPatch);

		Buyer savedBuyer = buyerService.patchBuyer(id, buyerPatch);
		return ResponseEntity.ok(BuyerMapper.toBuyerResponse(savedBuyer));
	}

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<?> deleteBuyer(@PathVariable Long id) {
		buyerService.deleteBuyer(id);
		return ResponseEntity.noContent().build();
	}
}
