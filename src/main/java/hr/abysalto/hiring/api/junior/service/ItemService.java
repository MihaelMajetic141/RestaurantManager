package hr.abysalto.hiring.api.junior.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import hr.abysalto.hiring.api.junior.data.dto.request.ItemRequest;
import hr.abysalto.hiring.api.junior.data.model.Item;
import hr.abysalto.hiring.api.junior.repository.ItemRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ItemService {

	private final ItemRepository itemRepository;

	public Page<Item> getAllItems(String name, Short itemNumber, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
		List<Item> all = new ArrayList<>();
		itemRepository.findAll().forEach(all::add);

		var stream = all.stream();
		if (name != null && !name.isBlank()) {
			stream = stream.filter(i -> i.getName() != null && i.getName().toLowerCase().contains(name.toLowerCase()));
		}
		if (itemNumber != null) {
			stream = stream.filter(i -> itemNumber.equals(i.getItemNumber()));
		}
		if (minPrice != null) {
			stream = stream.filter(i -> i.getPrice() != null && i.getPrice().compareTo(minPrice) >= 0);
		}
		if (maxPrice != null) {
			stream = stream.filter(i -> i.getPrice() != null && i.getPrice().compareTo(maxPrice) <= 0);
		}
		List<Item> filtered = stream.toList();

		int start = (int) pageable.getOffset();
		int end = Math.min(start + pageable.getPageSize(), filtered.size());
		List<Item> pageContent = start < filtered.size() ? filtered.subList(start, end) : List.of();

		return new PageImpl<>(pageContent, pageable, filtered.size());
	}

	public Item getItemById(Long id) {
		if (id == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item ID is required");
		}
		return itemRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found for id = " + id));
	}

	public Item createItem(ItemRequest request) {
		Item item = new Item();
		item.setItemNumber(request.getItemNumber());
		item.setName(request.getName());
		item.setPrice(request.getPrice());
		return itemRepository.save(item);
	}

	public Item updateItem(Long id, ItemRequest request) {
		Item item = itemRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found for id = " + id));
		item.setItemNumber(request.getItemNumber());
		item.setName(request.getName());
		item.setPrice(request.getPrice());
		return itemRepository.save(item);
	}

	public Item patchItem(Long id, ItemRequest request, JsonNode patchNode) {
		Item item = itemRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found with ID: " + id));
		if (patchNode.has("itemNumber")) {
			item.setItemNumber(request.getItemNumber());
		}
		if (patchNode.has("name")) {
			item.setName(request.getName());
		}
		if (patchNode.has("price")) {
			item.setPrice(request.getPrice());
		}
		return itemRepository.save(item);
	}

	public void deleteItem(Long id) {
		itemRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found for id = " + id));
		itemRepository.deleteById(id);
	}
}
