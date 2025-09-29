package cumulocity.microservice.translation.manager.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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

	private static final Set<String> SUPPORTED_LOCALES = Set.of("de", "en");

	@Autowired
	public TranslationService(ContextService<MicroserviceCredentials> contextService,
			CumulocityClientProperties clientProperties) {
		super();
		this.contextService = contextService;
		this.clientProperties = clientProperties;
	}

	public Collection<Translation> findTranslations() {
		Map<String, Translation> translationMap = new HashMap<>();
		Map<String, JsonNode> translations = getTranslationsNode();
		
		for(String locale : translations.keySet()) {
			JsonNode localeNode = translations.get(locale).get(locale);
			if(localeNode == null) {
				log.warn("No translations found for locale {}", locale);
				continue;
			}
			log.debug(localeNode.toPrettyString());
			Iterator<Entry<String, JsonNode>> fields = localeNode.fields();
			while (fields.hasNext()) {
				Entry<String, JsonNode> translation = fields.next();
				Translation currentTranslation = translationMap.get(translation.getKey());
				if (currentTranslation != null) {
					currentTranslation.addTranslation(locale, translation.getValue().asText());
				} else {
					Translation newTranslation = new Translation(translation.getKey());
					newTranslation.addTranslation(locale, translation.getValue().asText());
					translationMap.put(translation.getKey(), newTranslation);
				}
			}
		}
		log.info("Found {} translations", translationMap.size());

		return translationMap.values();
	}

	public synchronized List<Translation> addOrUpdateTranslations(List<Translation> translations) {
		log.info("Add or update {} translations", translations.size());
		
		Map<String, JsonNode> currentTranslations = getTranslationsNode();
		
		for (Translation translation : translations) {
			for (String locale : translation.getTranslations().keySet()) {
				JsonNode localeNode = currentTranslations.get(locale).get(locale);
				if (localeNode == null) {
					log.warn("Skip Translation, Locale {} not found", locale);
					continue;
				}
				((ObjectNode) localeNode).put(translation.getKey(), translation.getTranslations().get(locale));

			}
		}
		
		String publicOptionsAppId = getUserDefinedTranslations();
		log.debug("Public Options Application ID: {}", publicOptionsAppId);

		Map<String, Resource> resourceMap = new HashMap<>();
		for (String locale : currentTranslations.keySet()) {
			ByteArrayResource resource;
			try {
				resource = new ByteArrayResource(currentTranslations.get(locale).toPrettyString().getBytes());
				resourceMap.put(locale + ".json", resource);
			} catch (Exception e) {
				log.error("Upload new binary options json failed!", e);
			}
		}
		uploadApplicationAttachment(resourceMap, publicOptionsAppId);


		log.info("Add or update {} translations - done", translations.size());
		return translations;
	}

	private Map<String, JsonNode> getTranslationsNode() {
		String hostName = "https://" + getDomainName();

		Map<String, JsonNode> translations = new java.util.HashMap<>();
		for(String locale : SUPPORTED_LOCALES) {
			log.info("Process locale: {}", locale);
			byte[] translation = getTranslationsJson(locale, hostName);
			translations.put(locale, createJsonNode(translation));
		}
		return translations;
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

	private byte[] getTranslationsJson(String locale, String hostName) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", contextService.getContext().toCumulocityCredentials().getAuthenticationString());

		String serverUrl = hostName + "/apps/public/user-defined-translations/" + locale + ".json";
		RestTemplate restTemplate = new RestTemplate();

		byte[] attachment = restTemplate.execute(serverUrl, HttpMethod.GET, clientHttpRequest -> {
			clientHttpRequest.getHeaders().set("Authorization",
					contextService.getContext().toCumulocityCredentials().getAuthenticationString());
		}, clientHttpResponse -> {
			clientHttpResponse.getRawStatusCode();
			clientHttpResponse.getStatusText();
			byte[] readAllBytes = clientHttpResponse.getBody().readAllBytes();
			log.info("Get translations JSON locale: {}, HTTP StatusCode: {}, Text: {}",
					locale, clientHttpResponse.getRawStatusCode(), clientHttpResponse.getStatusText());
			return readAllBytes;
		});

		return attachment;
	}

	private String getUserDefinedTranslations() {
		String serverUrl = clientProperties.getBaseURL() + "/application/applicationsByName/User defined translations";
		RestTemplate restTemplate = new RestTemplate();

		String applicationId = restTemplate.execute(serverUrl, HttpMethod.GET, clientHttpRequest -> {
			clientHttpRequest.getHeaders().set("Authorization",
					contextService.getContext().toCumulocityCredentials().getAuthenticationString());
		}, clientHttpResponse -> {
			log.info("Get application by name (User defined translations); HTTP StatusCode: {}, Text: {}",
					clientHttpResponse.getRawStatusCode(), clientHttpResponse.getStatusText());
			byte[] readAllBytes = clientHttpResponse.getBody().readAllBytes();
			JsonNode createJsonNode = createJsonNode(readAllBytes);
			JsonNode app = createJsonNode.get("applications").elements().next();
			String appId = app.get("id").asText();
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
			log.info("Get domain name: {} response; HTTP StatusCode: {}, Text: {}",
					domainNameValue, clientHttpResponse.getRawStatusCode(), clientHttpResponse.getStatusText());
			return domainNameValue;
		});
		
		return domainName;
	}
	
	private void uploadApplicationAttachment(Map<String, Resource> resources, final String applicationId) {	
		//TODO Before sending this data to cumulocity an validation should be done: file size, does the content type fit etc.
		
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", contextService.getContext().toCumulocityCredentials().getAuthenticationString());
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		
		MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
		for(String locale : resources.keySet()) {
			multipartBodyBuilder.part(locale, resources.get(locale));
		}

		MultiValueMap<String,HttpEntity<?>> body = multipartBodyBuilder.build();
		HttpEntity<MultiValueMap<String, HttpEntity<?>>> requestEntity = new HttpEntity<>(body, headers);

		String serverUrl = clientProperties.getBaseURL() + "/application/applications/" + applicationId + "/binaries/files";
		log.debug(serverUrl);
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<Object> response = restTemplate.postForEntity(serverUrl, requestEntity, Object.class);
		log.info("Upload application binaries {} Response: {}", resources.keySet(), response.getStatusCodeValue());
		if(response.getStatusCodeValue() >= 300) {
			log.error("Upload application binaries {} failed with http code {}", resources.keySet(), response.getStatusCode().toString());
		}
	}
}
