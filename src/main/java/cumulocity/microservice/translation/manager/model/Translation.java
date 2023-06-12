package cumulocity.microservice.translation.manager.model;

import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Translation")
@Validated
public class Translation {

	@Schema(required = true, description = "Translation key", example = "cancel.button.label")
	@NotNull
	@Valid
	private String key;
	
	@Schema(required = true, description = "Translation map, contains the translations <iso-code, text>", example = " {\r\n"
			+ "      \"de\": \"Abbrechen\",\r\n"
			+ "      \"en\": \"Cancel\"\r\n"
			+ "    }")
	@NotNull
	@Valid
	private Map<String, String> translations = new HashMap<>();
	
	
	
	public Translation() {
		super();
	}

	public Translation(@NotNull @Valid String key) {
		super();
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	
	public void addTranslation(String locale, String text) {
		translations.put(locale, text);
	}

	public Map<String, String> getTranslations() {
		return translations;
	}

	public void setTranslations(Map<String, String> translations) {
		this.translations = translations;
	}
	
}
