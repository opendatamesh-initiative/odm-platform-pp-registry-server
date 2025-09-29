package org.opendatamesh.platform.pp.registry.rest.v2;

import org.opendatamesh.platform.pp.registry.RegistryApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.util.DefaultUriBuilderFactory;

import jakarta.annotation.PostConstruct;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {RegistryApplication.class, TestContainerConfig.class, TestConfig.class})
public abstract class RegistryApplicationIT {

    @LocalServerPort
    protected String port;

    protected TestRestTemplate rest;


    @PostConstruct
    public final void init() {
        rest = new TestRestTemplate();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setConnectionRequestTimeout(java.time.Duration.ofSeconds(30));
        requestFactory.setConnectTimeout(java.time.Duration.ofSeconds(30));
        rest.getRestTemplate().setRequestFactory(requestFactory);
        // add uri template handler because '+' of iso date would not be encoded
        DefaultUriBuilderFactory defaultUriBuilderFactory = new DefaultUriBuilderFactory();
        defaultUriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.TEMPLATE_AND_VALUES);
        rest.setUriTemplateHandler(defaultUriBuilderFactory);
    }

    protected String apiUrl(RoutesV2 route) {
        return apiUrl(route, "");
    }

    protected String apiUrl(RoutesV2 route, String extension) {
        return apiUrlFromString(route.getPath() + extension);
    }

    protected String apiUrlFromString(String routeUrlString) {
        return "http://localhost:" + port + routeUrlString;
    }

    protected String apiUrlOfItem(RoutesV2 route) {
        return apiUrl(route, "/{uuid}");
    }
}