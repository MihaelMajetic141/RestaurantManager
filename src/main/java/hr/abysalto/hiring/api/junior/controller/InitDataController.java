package hr.abysalto.hiring.api.junior.controller;

import hr.abysalto.hiring.api.junior.components.DatabaseInitializer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Data initalizer", description = "Invokes data population from DatabaseInitializer")
@RestController()
@RequestMapping("init-data")
public class InitDataController {
	@Autowired
	private DatabaseInitializer databaseInitializer;

	@Operation(summary = "init tables and data", responses = {
			@ApiResponse(description = "Success", responseCode = "204"),
			@ApiResponse(description = "Error", responseCode = "500", content = @Content(mediaType = "application/json"))})
	@PostMapping("/")
	public ResponseEntity<?> init() {
		try {
			this.databaseInitializer.initialize();
			return ResponseEntity.noContent().build();
		} catch (Exception ex) {
			return ResponseEntity.internalServerError().body(ex);
		}
	}
}
