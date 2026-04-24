package com.splitpay.frontend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.Enumeration;

/**
 * Transparent reverse-proxy: forwards every /api/** request to the API Gateway.
 *
 * Benefits:
 *  - JavaScript only uses relative paths (e.g. /api/v1/split/process).
 *  - No backend port is ever exposed to the browser.
 *  - Gateway URL is a single configurable property (gateway.url).
 *  - SSE streams are proxied with correct Content-Type so EventSource works.
 */
@RestController
@RequestMapping("/api")
public class GatewayProxyController {

    private final RestTemplate restTemplate;

    @Value("${gateway.url}")
    private String gatewayUrl;

    public GatewayProxyController() {
        this.restTemplate = new RestTemplate();
    }

    // ------------------------------------------------------------------ //
    //  Generic proxy — handles GET, POST, PUT, DELETE, PATCH              //
    // ------------------------------------------------------------------ //

    @RequestMapping(value = "/**",
            produces = {MediaType.APPLICATION_JSON_VALUE,
                        MediaType.TEXT_EVENT_STREAM_VALUE,
                        MediaType.APPLICATION_OCTET_STREAM_VALUE,
                        MediaType.ALL_VALUE})
    public ResponseEntity<byte[]> proxy(
            HttpServletRequest request,
            @RequestBody(required = false) byte[] body) throws Exception {

        // Build target URI: strip "/api" prefix, preserve path + query string
        String path = request.getRequestURI().replaceFirst("^/api", "");
        String queryString = request.getQueryString();
        String targetUri = gatewayUrl + path + (queryString != null ? "?" + queryString : "");

        // Copy incoming headers (forward Authorization, Content-Type, etc.)
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            for (String name : Collections.list(headerNames)) {
                // Skip hop-by-hop headers
                if (!name.equalsIgnoreCase("host") &&
                    !name.equalsIgnoreCase("connection") &&
                    !name.equalsIgnoreCase("transfer-encoding")) {
                    headers.set(name, request.getHeader(name));
                }
            }
        }

        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        RequestEntity<byte[]> requestEntity =
                new RequestEntity<>(body, headers, method, URI.create(targetUri));

        return restTemplate.exchange(requestEntity, byte[].class);
    }
}
