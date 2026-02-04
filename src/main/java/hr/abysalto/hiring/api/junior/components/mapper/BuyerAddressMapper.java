package hr.abysalto.hiring.api.junior.components.mapper;

import org.springframework.stereotype.Component;

import hr.abysalto.hiring.api.junior.data.dto.response.BuyerAddressResponse;
import hr.abysalto.hiring.api.junior.data.model.BuyerAddress;

@Component
public class BuyerAddressMapper {

	public static BuyerAddressResponse toBuyerAddressResponse(BuyerAddress address) {
		return BuyerAddressResponse.builder()
				.city(address.getCity())
				.street(address.getStreet())
				.homeNumber(address.getHomeNumber())
				.buyerId(address.getBuyerId())
				.build();
	}
}
