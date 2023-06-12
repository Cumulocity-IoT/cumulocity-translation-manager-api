package cumulocity.microservice.translation.manager.controller;

import java.util.Collection;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cumulocity.microservice.translation.manager.model.Translation;
import cumulocity.microservice.translation.manager.service.TranslationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

/**
 * This is an example controller. This should be removed for your real project!
 * 
 * @author APES
 *
 */
@RestController
@RequestMapping("/api/translations")
public class TranslationController {

	private TranslationService translationService;
	
	@Autowired
	public TranslationController(TranslationService translationService) {
		this.translationService = translationService;
	}

	@Operation(summary = "GET all translations", description = "Returns all translations", tags = {})
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "404", description = "Not Found") })
	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Collection<Translation>> getAllTranslation() {
		Collection<Translation> response = translationService.findTranslations();
		return new ResponseEntity<Collection<Translation>>(response, HttpStatus.OK);
	}

	@Operation(summary = "CREATE or UPDATE translations", description = "Creates or updates translations.", tags = {})
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Translation.class)))) })
	@PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<Translation>> addTranslations(@Valid @RequestBody List<Translation> translations) {
		List<Translation> response = translationService.addOrUpdateTranslations(translations);
		return new ResponseEntity<List<Translation>>(response, HttpStatus.OK);
	}
}
