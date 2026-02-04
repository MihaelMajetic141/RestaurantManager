package hr.abysalto.hiring.api.junior.data.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuyerAddressResponse {
	private String city;
	private String street;
	private String homeNumber;
	private Long buyerId;
}
