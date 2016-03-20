package ru.asmsoft.p2p;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

/**
 * Created by Alex on 20.03.16.
 */
@Service
public class PingReply {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @ServiceActivator(inputChannel = "incoming")
    public void receive(Message message){
        logger.info("Received: {}", message);
    }
}
