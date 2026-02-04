package hr.abysalto.hiring.api.junior.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hr.abysalto.hiring.api.junior.components.PatchUtils;
import hr.abysalto.hiring.api.junior.components.mapper.ItemMapper;
import hr.abysalto.hiring.api.junior.data.dto.request.ItemRequest;
import hr.abysalto.hiring.api.junior.data.model.Item;
import hr.abysalto.hiring.api.junior.service.ItemService;
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

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/item")
@AllArgsConstructor
public class ItemController {

	private final ItemService itemService;
	private final ObjectMapper objectMapper;
	private final PatchUtils patchUtils;
	private final Validator validator;

	@GetMapping("/getAll")
	public ResponseEntity<?> getItems(
			@RequestParam(required = false) String name,
			@RequestParam(required = false) Short itemNumber,
			@RequestParam(required = false) BigDecimal minPrice,
			@RequestParam(required = false) BigDecimal maxPrice,
			@PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable
	) {
		Page<Item> page = itemService.getAllItems(name, itemNumber, minPrice, maxPrice, pageable);
		return ResponseEntity.ok(page.getContent());
	}

	@GetMapping("/get/{id}")
	public ResponseEntity<?> getItemById(@PathVariable Long id) {
		Item item = itemService.getItemById(id);
		return ResponseEntity.ok(ItemMapper.toItemResponse(item));
	}

	@PostMapping("/create")
	public ResponseEntity<?> createItem(@Valid @RequestBody ItemRequest request) {
		Item newItem = itemService.createItem(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(newItem);
	}

	@PutMapping("/update/{id}")
	public ResponseEntity<?> updateItem(@PathVariable Long id, @Valid @RequestBody ItemRequest request) {
		Item updatedItem = itemService.updateItem(id, request);
		return ResponseEntity.ok(ItemMapper.toItemResponse(updatedItem));
	}

	@PatchMapping(path = "/patch/{id}", consumes = "application/merge-patch+json")
	public ResponseEntity<?> patchItem(
			@PathVariable Long id,
			@RequestBody JsonNode patchNode
	) throws JsonProcessingException, IllegalArgumentException {
		Item existingItem = itemService.getItemById(id);

		JsonNode existingNode = objectMapper.valueToTree(existingItem);
		JsonNode merged = patchUtils.merge(existingNode, patchNode);
		ItemRequest itemPatch = objectMapper.treeToValue(merged, ItemRequest.class);
		validator.validate(itemPatch);

		Item savedItem = itemService.patchItem(id, itemPatch, patchNode);
		return ResponseEntity.ok(ItemMapper.toItemResponse(savedItem));
	}

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<?> deleteItem(@PathVariable Long id) {
		itemService.deleteItem(id);
		return ResponseEntity.noContent().build();
	}
}
