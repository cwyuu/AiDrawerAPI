package com.example.aidrawerapi.util;

import com.example.aidrawerapi.pojo.DoDrawer;
import com.example.aidrawerapi.pojo.DrawerResponse;
import com.example.aidrawerapi.pojo.Model;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class AiDrawer {
    //创建一个不安全的webclient
    private WebClient createWebClientWithInsecureSSL() {
        SslContextBuilder sslContextBuilder = SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE);

        HttpClient httpClient = HttpClient.create()
                .secure(t -> t.sslContext(sslContextBuilder));

        return WebClient.builder()
                .baseUrl("")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(item -> item.defaultCodecs().maxInMemorySize(30 * 1024 * 1024))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic MTIzOjEyMw==")
                .build();
    }

    private final WebClient webClient = createWebClientWithInsecureSSL();
    //创建一个安全的webclient，ai绘画在本地的话，url是http://localhost:7860/
//    private final WebClient webClient = WebClient.builder()
//            //.baseUrl("http://localhost:7860/")
//            .baseUrl("https://url/")
//            .codecs(item->item.defaultCodecs().maxInMemorySize(30 * 1024 * 1024))
//            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//            .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic MTIzOjEyMw==")
//            .build();

    @Async
    public CompletableFuture<DrawerResponse> doDrawer(DoDrawer doDrawer) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(doDrawer);
        Mono<DrawerResponse> responseMono = webClient.post()
                .uri("/sdapi/v1/txt2img")
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(HttpStatus::isError, clientResponse -> Mono.error(new RuntimeException("Error response from API")))
                .bodyToMono(DrawerResponse.class);
        return responseMono.toFuture();
    }

    @Async
    public CompletableFuture<List<Model>> getModelList(){
        Mono<List<Model>> responseMono = webClient.get()
                .uri("/sdapi/v1/model")
                .retrieve()
                .onStatus(HttpStatus::isError, clientResponse -> Mono.error(new RuntimeException("Error response from API")))
                .bodyToMono(new ParameterizedTypeReference<List<Model>>() {});
        return responseMono.toFuture();
    }

    @Async
    public CompletableFuture<String> saveImage(DrawerResponse drawerResponse,String FileName) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        // 获得 images 列表中的第一个元素
        String base64Image = drawerResponse.getImages().get(0);
        byte[] imageData = Base64.getDecoder().decode(base64Image);
        int contentLength = imageData.length;
        String contentMD5 =MD5Util.md5(imageData);
        COSSKey cosSKey = new COSSKey();
        String url= cosSKey.getUrl(FileName);


        CompletableFuture<String> result = new CompletableFuture<>();

        DateTimeFormatter rfc1123Formatter = DateTimeFormatter
                .ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
                .withZone(ZoneId.of("GMT"));
        String gmtDate = rfc1123Formatter.format(ZonedDateTime.now());
        System.out.println(gmtDate);

        WebClient client = WebClient.builder()
                .codecs(item->item.defaultCodecs().maxInMemorySize(30 * 1024 * 1024))
                .build();


        String encodedUrl = url.toString();
        String decodedUrl = URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8.toString());

        client.put()
                .uri(decodedUrl)
                .header("Content-Type", MediaType.IMAGE_PNG_VALUE)
                .header("Content-Length", String.valueOf(contentLength))
                .header("Content-MD5", contentMD5)
                .header("Date", gmtDate)
                .body(Mono.just(imageData), byte[].class)
                .retrieve()
                .toBodilessEntity()
                .subscribe(responseEntity -> {
                    if (responseEntity.getStatusCode().is2xxSuccessful()) {
                        result.complete("1");
                    } else {
                        result.complete("0");
                    }
                },error -> {
                            if (error instanceof WebClientResponseException) {
                                WebClientResponseException exception = (WebClientResponseException) error;
                                System.err.println("Error status code: " + exception.getStatusCode());
                                System.err.println("Error body: " + exception.getResponseBodyAsString());
                            } else {
                                System.err.println("Error: " + error.getMessage());
                            }
                            result.complete("0");
                        }
                );
        return result;
    }
}