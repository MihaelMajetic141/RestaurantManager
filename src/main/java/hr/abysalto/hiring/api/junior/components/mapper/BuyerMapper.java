package hr.abysalto.hiring.api.junior.components.mapper;

import org.springframework.stereotype.Component;

import hr.abysalto.hiring.api.junior.data.dto.BuyerResponse;
import hr.abysalto.hiring.api.junior.data.model.Buyer;

@Component
public class BuyerMapper {

	public static BuyerResponse toBuyerResponse(Buyer buyer) {
		return BuyerResponse.builder()
				.firstName(buyer.getFirstName())
				.lastName(buyer.getLastName())
				.title(buyer.getTitle())
				.build();
	}
}
