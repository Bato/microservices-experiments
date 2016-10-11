package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@EnableBinding(ReservationClientChannels.class)
@EnableCircuitBreaker
@EnableZuulProxy
@EnableDiscoveryClient
@SpringBootApplication
public class ReservationClientApplication {

//    @Bean
//    AlwaysSampler alwaysSampler() {
//        return new AlwaysSampler();
//    }

    public static void main(String[] args) {
        SpringApplication.run(ReservationClientApplication.class, args);
    }
    
    
    /**
     *  If you need a RestTemplate you'll have to provide one.
     *  
     *  Note, in earlier versions of the Spring cloud starter for Eureka, 
     *  a RestTemplate bean was created for you, but this is no longer true
     * @return
     */
	@Bean
	@LoadBalanced
	RestTemplate restTemplate() {
		return new RestTemplate();
	}    
}

