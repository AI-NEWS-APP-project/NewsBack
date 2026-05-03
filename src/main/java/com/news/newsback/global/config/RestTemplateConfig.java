package com.news.newsback.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() throws NoSuchAlgorithmException, KeyManagementException {
        RestTemplate restTemplate = new RestTemplate(new Tls12ClientHttpRequestFactory());
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().set("User-Agent", "NewsBack RSS Collector/1.0");
            return execution.execute(request, body);
        });
        return restTemplate;
    }

    private static class Tls12ClientHttpRequestFactory extends SimpleClientHttpRequestFactory {

        private final javax.net.ssl.SSLSocketFactory sslSocketFactory;

        private Tls12ClientHttpRequestFactory() throws NoSuchAlgorithmException, KeyManagementException {
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, null, null);
            this.sslSocketFactory = sslContext.getSocketFactory();
        }

        @Override
        protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
            if (connection instanceof HttpsURLConnection httpsConnection) {
                httpsConnection.setSSLSocketFactory(sslSocketFactory);
            }
            super.prepareConnection(connection, httpMethod);
        }
    }
}
