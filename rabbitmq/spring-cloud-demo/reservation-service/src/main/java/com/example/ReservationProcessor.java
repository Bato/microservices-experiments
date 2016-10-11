package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;

@MessageEndpoint
class ReservationProcessor {
	
    private static Logger LOGGER = LoggerFactory.getLogger(ReservationProcessor.class);

    @Autowired
    private final ReservationRepository reservationRepository;

	@Autowired
	public ReservationProcessor(ReservationRepository reservationRepository) {
		this.reservationRepository = reservationRepository;
	}
	
	
    @ServiceActivator(inputChannel = "input")
	public void acceptNewReservations(Message<String> msg) {
    	
		String rn = msg.getPayload();
		
    	LOGGER.info("+++IN-> ReservationProcessor.acceptNewReservation()");
    	LOGGER.info("\t reservation=" + rn);
        this.reservationRepository.save(new Reservation(rn));
    }
}
