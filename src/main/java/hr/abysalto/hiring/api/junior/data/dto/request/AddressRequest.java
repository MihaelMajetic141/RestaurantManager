package hr.abysalto.hiring.api.junior.data.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {
	@NotBlank(message = "City is required")
	@Size(max = 100)
	private String city;

	@NotBlank(message = "Street is required")
	@Size(max = 100)
	private String street;

	@Size(max = 100)
	private String homeNumber;
}
