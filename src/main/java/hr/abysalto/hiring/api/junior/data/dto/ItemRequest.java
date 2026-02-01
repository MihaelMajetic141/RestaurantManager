package hr.abysalto.hiring.api.junior.data.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequest {
	private Short itemNumber;
	private String name;
	private BigDecimal price;
}
