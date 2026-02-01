package hr.abysalto.hiring.api.junior.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuyerRequest {
	private String firstName;
	private String lastName;
	private String title;
}
