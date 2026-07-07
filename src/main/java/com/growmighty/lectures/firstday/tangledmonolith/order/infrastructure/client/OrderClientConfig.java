package com.growmighty.lectures.firstday.tangledmonolith.order.infrastructure.client;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/*
    API 호출을 위한 Infrastructure 계층
    ProductHttpClient에서 사용하는 RestClient를 자주 쓸 테니 Bean으로 만들어놓는다.
 */
@Configuration // Bean을 생성하는 클래스가 있어요!
public class OrderClientConfig {

    @Bean // 빈 생성
    // order.client.base-url이 있으면 그것을 쓰고, 없으면 localhost:8080을 사용하겠다. MSA 염두.
    RestClient orderRestClient(@Value("${order.client.base-url:http://localhost:8080}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}
