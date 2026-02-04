package hr.abysalto.hiring.api.junior.data.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuyerRequest {
	@NotBlank(message = "First name is required")
	@Size(max = 100)
	private String firstName;

	@NotBlank(message = "Last name is required")
	@Size(max = 100)
	private String lastName;

	@Size(max = 100)
	private String title;

	private List<Long> addressIds;
}
