package hr.abysalto.hiring.api.junior.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuyerResponse {
	private String firstName;
	private String lastName;
	private String title;
}
