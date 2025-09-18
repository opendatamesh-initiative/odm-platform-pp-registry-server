package org.opendatamesh.platform.pp.registry.utils.client;

import org.opendatamesh.platform.pp.registry.exceptions.client.ClientException;
import org.opendatamesh.platform.pp.registry.utils.client.http.HttpEntity;
import org.opendatamesh.platform.pp.registry.utils.client.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

class RestTemplateWrapper implements RestUtilsTemplate {

    private final RestTemplate restTemplate;

    private RestTemplateWrapper(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public static RestTemplateWrapper wrap(RestTemplate restTemplate) {
        return new RestTemplateWrapper(restTemplate);
    }


    public RestUtils build() {
        RestUtilsTemplate template = this;
        return new BaseRestUtils(template);
    }

    @Override
    public <T> T exchange(String url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType, Object... uriVariables) throws ClientException {
        try {
            LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
            requestEntity.getRawHeaders().forEach(headers::add);
            return restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.resolve(method.name()),
                    new org.springframework.http.HttpEntity<>(requestEntity.getBody(), headers),
                    responseType,
                    uriVariables
            ).getBody();
        } catch (RestClientResponseException e) {
            throw new ClientException(e.getRawStatusCode(), e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, e.getMessage());
        }
    }

    @Override
    public <T> T exchange(String url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType, Map<String, ?> uriVariables) throws ClientException {
        try {
            LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
            requestEntity.getRawHeaders().forEach(headers::add);
            return restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.resolve(method.name()),
                    new org.springframework.http.HttpEntity<>(requestEntity.getBody(), headers),
                    responseType,
                    uriVariables
            ).getBody();
        } catch (RestClientResponseException e) {
            throw new ClientException(e.getRawStatusCode(), e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ClientException(500, e.getMessage());
        }
    }


}
