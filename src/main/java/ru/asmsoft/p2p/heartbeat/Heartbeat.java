/**
 * The MIT License (MIT)
 *
 * Copyright (c) 20.03.16 <Alex S. Marinenko> alex.marinenko@gmail.com
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package ru.asmsoft.p2p.heartbeat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;
import ru.asmsoft.p2p.configuration.INodeConfiguration;
import ru.asmsoft.p2p.fsm.NodeEvents;
import ru.asmsoft.p2p.fsm.NodeStates;
import ru.asmsoft.p2p.packets.PingPacket;
import ru.asmsoft.p2p.packets.UpdateMePacket;
import ru.asmsoft.p2p.packets.UpdateMeResponse;
import ru.asmsoft.p2p.storage.IMessageRepository;
import ru.asmsoft.p2p.storage.INodeRepository;

import java.io.IOException;
import java.net.DatagramSocket;

@Service("heartbeat")
public class Heartbeat {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    INodeRepository nodeRepository;

    @Autowired
    IMessageRepository messageRepository;

    @Autowired
    @Qualifier("outgoing-broadcast")
    private MessageChannel outgoingHeartbeatChannel;

    private Message<PingPacket> pingPacket;

    @Autowired
    private ISelfUpdateService selfUpdateService;

    public void ping() throws IOException {
        pingPacket = MessageBuilder.withPayload(new PingPacket(messageRepository.getDbVersion())).build();
        logger.trace("Send: {}", pingPacket);
        outgoingHeartbeatChannel.send(pingPacket);
    }

    @ServiceActivator(inputChannel = "incoming-heartbeat")
    public void handleIncomingPing(Message<PingPacket> message){
        logger.trace("Received: {}", message);

        String nodeAddress = (String) message.getHeaders().get("ip_address");
        nodeRepository.registerNode(nodeAddress);

        PingPacket packet = message.getPayload();

        // If our DB is outdated
        if (messageRepository.getDbVersion() < packet.getDbVersion() ){
            selfUpdateService.startNodeUpdate(nodeAddress);
        }

    }


}
