package ru.asmsoft.p2p;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.asmsoft.p2p.packets.PingPacket;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by Alex on 20.03.16.
 */
@Service
public class Pinger {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    private byte buf[] = new byte[]{ 0x01, 0x02, 0x03 };
    private DatagramSocket socket;
    private InetAddress broadcastAddress;

    @Autowired
    @Qualifier("outgoing")
    private MessageChannel outgoingChannel;

    @PostConstruct
    public void init() throws IOException {
        socket = new DatagramSocket();
        socket.setBroadcast(true);
        broadcastAddress = InetAddress.getByName("192.168.99.255");
    }

    @PreDestroy
    public void shutdown() throws IOException {
        socket.close();
    }

    @Scheduled(fixedRate = 1000)
    public void ping() throws IOException {

        logger.info("ping");

        /*DatagramPacket sendPacket = new DatagramPacket(buf, buf.length, broadcastAddress, 8888);
        socket.send(sendPacket);*/

        outgoingChannel.send(MessageBuilder.withPayload(new PingPacket()).build());


    }

}
