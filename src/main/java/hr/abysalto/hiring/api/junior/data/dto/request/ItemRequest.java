package hr.abysalto.hiring.api.junior.data.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequest {
	@NotNull(message = "Item number is required")
	private Short itemNumber;

	@NotBlank(message = "Name is required")
	@Size(max = 100)
	private String name;

	@DecimalMin(value = "0", inclusive = true, message = "Price must be non-negative")
	private BigDecimal price;
}
