package com.splitpay.frontend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
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

    private static final Logger log = LoggerFactory.getLogger(GatewayProxyController.class);

    private final RestTemplate restTemplate;

    @Value("${gateway.url}")
    private String gatewayUrl;

    public GatewayProxyController() {
        this.restTemplate = new RestTemplate();
    }

    // ------------------------------------------------------------------ //
    //  SSE Proxy — specialized for streaming                              //
    // ------------------------------------------------------------------ //
    @RequestMapping(value = "/v1/split/stream/**",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<StreamingResponseBody> proxySse(HttpServletRequest request) {
        String path = request.getRequestURI().replaceFirst("^/api", "");
        String targetUri = gatewayUrl + path;
        log.info("Proxying SSE request to: {}", targetUri);

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(outputStream -> {
                    try {
                        restTemplate.execute(targetUri, HttpMethod.GET, req -> {
                            String auth = request.getHeader("Authorization");
                            if (auth != null) req.getHeaders().set("Authorization", auth);
                        }, response -> {
                            try (InputStream inputStream = response.getBody()) {
                                byte[] buffer = new byte[1024];
                                int bytesRead;
                                while ((bytesRead = inputStream.read(buffer)) != -1) {
                                    outputStream.write(buffer, 0, bytesRead);
                                    outputStream.flush();
                                }
                            }
                            return null;
                        });
                    } catch (Exception e) {
                        log.error("SSE Proxy error for {}: {}", targetUri, e.getMessage());
                    }
                });
    }

    // ------------------------------------------------------------------ //
    //  Generic proxy — handles GET, POST, PUT, DELETE, PATCH              //
    // ------------------------------------------------------------------ //

    @RequestMapping(value = "/**", produces = MediaType.ALL_VALUE)
    public ResponseEntity<byte[]> proxy(
            HttpServletRequest request,
            @RequestBody(required = false) byte[] body) {

        try {
            String path = request.getRequestURI().replaceFirst("^/api", "");
            if (path.startsWith("/v1/split/stream/")) return null;

            String queryString = request.getQueryString();
            String targetUri = gatewayUrl + path + (queryString != null ? "?" + queryString : "");
            
            log.info("Proxying {} request to: {}", request.getMethod(), targetUri);

            HttpHeaders headers = new HttpHeaders();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames != null && headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                if (!name.equalsIgnoreCase("host") && !name.equalsIgnoreCase("connection")) {
                    headers.set(name, request.getHeader(name));
                }
            }

            HttpMethod method = HttpMethod.valueOf(request.getMethod());
            RequestEntity<byte[]> requestEntity = new RequestEntity<>(body, headers, method, URI.create(targetUri));
            
            return restTemplate.exchange(requestEntity, byte[].class);

        } catch (HttpStatusCodeException e) {
            log.warn("Downstream error: {} {}", e.getStatusCode(), request.getRequestURI());
            return ResponseEntity.status(e.getStatusCode())
                    .headers(e.getResponseHeaders())
                    .body(e.getResponseBodyAsByteArray());
        } catch (Exception e) {
            log.error("Proxy failure for {}: {}", request.getRequestURI(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(("{\"error\": \"Proxy Error\", \"message\": \"" + e.getMessage() + "\"}").getBytes());
        }
    }
}
