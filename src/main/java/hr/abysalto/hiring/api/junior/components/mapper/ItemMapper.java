package hr.abysalto.hiring.api.junior.components.mapper;

import org.springframework.stereotype.Component;

import hr.abysalto.hiring.api.junior.data.dto.ItemResponse;
import hr.abysalto.hiring.api.junior.data.model.Item;

@Component
public class ItemMapper {

	public static ItemResponse toItemResponse(Item item) {
		return ItemResponse.builder()
				.itemNumber(item.getItemNumber())
				.name(item.getName())
				.price(item.getPrice())
				.build();
	}
}
