package hr.abysalto.hiring.api.junior.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {
    private String city;
    private String street;
    private String homeNumber;
}
