package hr.abysalto.hiring.api.junior.data.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuyerAddressRequest {
	@NotBlank(message = "City is required")
	@Size(max = 100)
	private String city;

	@NotBlank(message = "Street is required")
	@Size(max = 100)
	private String street;

	@Size(max = 100)
	private String homeNumber;

	@NotNull(message = "Buyer ID is required")
	private Long buyerId;
}
