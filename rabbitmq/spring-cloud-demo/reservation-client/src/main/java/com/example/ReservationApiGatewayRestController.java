package com.example;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpMethod.GET;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Description;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;

@RestController
@RequestMapping("/reservations")
//@RefreshScope
class ReservationApiGatewayRestController {

	private static Logger LOGGER = LoggerFactory.getLogger(ReservationApiGatewayRestController.class);
	
    private final RestTemplate restTemplate;
    
	private final MessageChannel reservationService;

    @Value("${message}")
    private String message;

	@Autowired
	public ReservationApiGatewayRestController(
			ReservationClientChannels channels,
			@LoadBalanced RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
		this.reservationService = channels.output();
	}
	


    @Description("Post new reservations using Spring Cloud Stream")
    @RequestMapping(method = RequestMethod.POST)
    public void acceptNewReservations(@RequestBody Reservation reservation) {
    	
    	LOGGER.info("+++IN-> ReservationRestController.acceptNewReservations()");
    	LOGGER.info("\t reservation=" + reservation);    	
    	
    	String reservationName = reservation.getReservationName(); 
        Message<String> msg = MessageBuilder
				.withPayload(reservationName)
				.build();
        this.reservationService.send(msg);
    }

    public Collection<String> getReservationNameFallback() {
        return emptyList();
    }

    
    /**
     * 
     * @return
     */
    @HystrixCommand(fallbackMethod = "getReservationNameFallback")
    @RequestMapping(path = "/names", method = RequestMethod.GET)
    public Collection<String> getReservationNames() {

        ParameterizedTypeReference<Resources<Reservation>> ptr =
                new ParameterizedTypeReference<Resources<Reservation>>() {
                };
                
        		ResponseEntity<Resources<Reservation>> responseEntity =
        				this.restTemplate.exchange("http://reservation-service/reservations",
        						HttpMethod.GET,
        						null,
        						ptr
        				);                

        return responseEntity
                .getBody()
                .getContent()
                .stream()
                .map(Reservation::getReservationName)
                .collect(toList());
    }

    
    /**
     * 
     * @return
     */
    @RequestMapping(path = "/client-message", method = RequestMethod.GET)
    public String getMessage() {
        return this.message;
    }

        
    /**
     * 
     * @return
     */
    @HystrixCommand(fallbackMethod = "getReservationServiceMessageFallback", commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "3000")
    })
    @RequestMapping(path = "/service-message", method = RequestMethod.GET)
    public String getReservationServiceMessage() {
        return this.restTemplate.getForObject(
                "http://reservation-service/message",
                String.class);
    }
    
    public String getReservationServiceMessageFallback() {
        return "Unable to contact Reservation Service";
    }
    
    
}
