package cumulocity.microservice.translation.manager.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.cumulocity.microservice.api.CumulocityClientProperties;
import com.cumulocity.microservice.context.ContextService;
import com.cumulocity.microservice.context.credentials.MicroserviceCredentials;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cumulocity.microservice.translation.manager.model.Translation;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TranslationService {

	private ContextService<MicroserviceCredentials> contextService;

	private final CumulocityClientProperties clientProperties;

	private static final String I18NEXTRA = "i18nExtra";

	@Autowired
	public TranslationService(ContextService<MicroserviceCredentials> contextService,
			CumulocityClientProperties clientProperties) {
		super();
		this.contextService = contextService;
		this.clientProperties = clientProperties;
	}

	public Collection<Translation> findTranslations() {
		Map<String, Translation> translationMap = new HashMap<>();
		JsonNode optionsJsonNode = getOptionsJsonNode();
		JsonNode i18n = optionsJsonNode.get(I18NEXTRA);
		log.info(i18n.toPrettyString());
		Iterator<Entry<String, JsonNode>> locales = i18n.fields();
		while (locales.hasNext()) {
			Entry<String, JsonNode> locale = locales.next();
			String isoCodeLocale = locale.getKey();
			Iterator<Entry<String, JsonNode>> fields = locale.getValue().fields();
			while (fields.hasNext()) {
				Entry<String, JsonNode> translation = fields.next();
				log.info("Locale: {}, Translation Key: {}, Text: {}", isoCodeLocale, translation.getKey(),
						translation.getValue().asText());
				Translation currentTranslation = translationMap.get(translation.getKey());
				if (currentTranslation != null) {
					currentTranslation.addTranslation(isoCodeLocale, translation.getValue().asText());
				} else {
					Translation newTranslation = new Translation(translation.getKey());
					newTranslation.addTranslation(isoCodeLocale, translation.getValue().asText());
					translationMap.put(translation.getKey(), newTranslation);
				}
			}
		}

		return translationMap.values();
	}

	public List<Translation> addOrUpdateTranslations(List<Translation> translations) {
		JsonNode optionsJsonNode = getOptionsJsonNode();
		JsonNode i18n = optionsJsonNode.get(I18NEXTRA);
		for (Translation translation : translations) {
			for (String locale : translation.getTranslations().keySet()) {
				log.info("Locale: {}, Translation Key: {}, Text: {}", locale, translation.getKey(),
						translation.getTranslations().get(locale));
				JsonNode localeNode = i18n.get(locale);
				if (localeNode == null) {
					log.warn("Skip Translation, Locale {} not found", locale);
					continue;
				}
				((ObjectNode) localeNode).put(translation.getKey(), translation.getTranslations().get(locale));

			}

		}

		log.info(optionsJsonNode.toPrettyString());
		log.info("Is Binary? {}", optionsJsonNode.isBinary());
		
		String publicOptionsAppId = getPublicOptionsAppId();
		log.info("Public Options Application ID: {}", publicOptionsAppId);

		ByteArrayResource resource;
		try {
			resource = new ByteArrayResource(optionsJsonNode.toPrettyString().getBytes());
			uploadApplicationAttachment(resource, publicOptionsAppId);
		} catch (Exception e) {
			log.error("Upload new binary options json failed!", e);
		}

		return translations;
	}

	private JsonNode getOptionsJsonNode() {
		byte[] optionsJson = getOptionsJson();
		return createJsonNode(optionsJson);
	}

	private JsonNode createJsonNode(byte[] jsonByteArray) {
		if (jsonByteArray.length <= 0) {
			return null;
		}

		ObjectMapper objectMapper = new ObjectMapper();
		try {
			JsonNode tree = objectMapper.readTree(jsonByteArray);
			return tree;
		} catch (IOException e) {
			log.error("Reading Json byte array failed!", e);
			return null;
		}
	}

	private byte[] getOptionsJson() {
		// GET: {{url}}/apps/public-options/options.json

		
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", contextService.getContext().toCumulocityCredentials().getAuthenticationString());

		String hostName = "https://" + getDomainName();
		String serverUrl = hostName + "/apps/public-options/options.json";
		RestTemplate restTemplate = new RestTemplate();

		byte[] attachment = restTemplate.execute(serverUrl, HttpMethod.GET, clientHttpRequest -> {
			clientHttpRequest.getHeaders().set("Authorization",
					contextService.getContext().toCumulocityCredentials().getAuthenticationString());
		}, clientHttpResponse -> {
			clientHttpResponse.getRawStatusCode();
			clientHttpResponse.getStatusText();
			byte[] readAllBytes = clientHttpResponse.getBody().readAllBytes();
			log.info("Download event attachment response; HTTP StatusCode: {}, Text: {}",
					clientHttpResponse.getRawStatusCode(), clientHttpResponse.getStatusText());
			return readAllBytes;
		});

		return attachment;
	}

	private String getPublicOptionsAppId() {
		// {{url}}/application/applicationsByName/public-options
		String serverUrl = clientProperties.getBaseURL() + "/application/applicationsByName/public-options";
		RestTemplate restTemplate = new RestTemplate();

		String applicationId = restTemplate.execute(serverUrl, HttpMethod.GET, clientHttpRequest -> {
			clientHttpRequest.getHeaders().set("Authorization",
					contextService.getContext().toCumulocityCredentials().getAuthenticationString());
		}, clientHttpResponse -> {
			clientHttpResponse.getRawStatusCode();
			clientHttpResponse.getStatusText();
			byte[] readAllBytes = clientHttpResponse.getBody().readAllBytes();
			JsonNode createJsonNode = createJsonNode(readAllBytes);
			
			JsonNode app = createJsonNode.get("applications").elements().next();
			String appId = app.get("id").asText();

			log.info("Download event attachment response; HTTP StatusCode: {}, Text: {}",
					clientHttpResponse.getRawStatusCode(), clientHttpResponse.getStatusText());
			return appId;
		});
		
		return applicationId;
	}

	private String getDomainName() {
		String serverUrl = clientProperties.getBaseURL() + "/tenant/currentTenant";
		RestTemplate restTemplate = new RestTemplate();
		
		String domainName = restTemplate.execute(serverUrl, HttpMethod.GET, clientHttpRequest -> {
			clientHttpRequest.getHeaders().set("Authorization",
					contextService.getContext().toCumulocityCredentials().getAuthenticationString());
		}, clientHttpResponse -> {
			clientHttpResponse.getRawStatusCode();
			clientHttpResponse.getStatusText();
			byte[] readAllBytes = clientHttpResponse.getBody().readAllBytes();
			JsonNode createJsonNode = createJsonNode(readAllBytes);
			String domainNameValue = createJsonNode.get("domainName").asText();
			log.info("Download event attachment response; HTTP StatusCode: {}, Text: {}",
					clientHttpResponse.getRawStatusCode(), clientHttpResponse.getStatusText());
			return domainNameValue;
		});
		
		return domainName;
	}
	
	private void uploadApplicationAttachment(Resource resource, final String applicationId) {	
		//TODO Before sending this data to cumulocity an validation should be done: file size, does the content type fit etc.
		
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", contextService.getContext().toCumulocityCredentials().getAuthenticationString());
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		
		MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("options.json", resource);
		
		MultiValueMap<String,HttpEntity<?>> body = multipartBodyBuilder.build();
		HttpEntity<MultiValueMap<String, HttpEntity<?>>> requestEntity = new HttpEntity<>(body, headers);

		String serverUrl = clientProperties.getBaseURL() + "/application/applications/" + applicationId + "/binaries/files";
		log.info(serverUrl);
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<Object> response = restTemplate.postForEntity(serverUrl, requestEntity, Object.class);
		log.info("Response: " + response.getStatusCodeValue());
		if(response.getStatusCodeValue() >= 300) {
			log.error("Upload application binary failed with http code {}", response.getStatusCode().toString());;
		}
	}
}
