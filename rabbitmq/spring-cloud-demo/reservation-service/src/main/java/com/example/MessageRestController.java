package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
class MessageRestController {

	private static Logger LOGGER = LoggerFactory.getLogger(MessageRestController.class);

	private final String value;
	
	@Autowired
	public MessageRestController(
			@Value("${message}") String value) {
		this.value = value;
	}
	

    @RequestMapping(method = RequestMethod.GET, value = "/message")
	public String read() {
    	LOGGER.info("+++IN-> ReservationRestController.getMessage()");
    	LOGGER.info("\t message=" + value);    	
		return this.value;
	}
    
}
