package org.opendatamesh.platform.pp.registry.utils.client;


import org.opendatamesh.platform.pp.registry.exceptions.client.ClientException;
import org.opendatamesh.platform.pp.registry.utils.client.http.HttpEntity;
import org.opendatamesh.platform.pp.registry.utils.client.http.HttpMethod;

import java.util.Map;

interface RestUtilsTemplate {

    <T> T exchange(String url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType, Object... uriVariables) throws ClientException;

    <T> T exchange(String url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType, Map<String, ?> uriVariables) throws ClientException;

}
