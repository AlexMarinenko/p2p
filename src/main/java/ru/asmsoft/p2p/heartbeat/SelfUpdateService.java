package ru.asmsoft.p2p.heartbeat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;
import ru.asmsoft.p2p.configuration.INodeConfiguration;
import ru.asmsoft.p2p.fsm.NodeEvents;
import ru.asmsoft.p2p.fsm.NodeStates;
import ru.asmsoft.p2p.packets.UpdateMePacket;
import ru.asmsoft.p2p.packets.UpdateMeResponse;
import ru.asmsoft.p2p.storage.IMessageRepository;

/**
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 21.03.16 <Alex S. Marinenko> alex.marinenko@gmail.com
 * <p>
 * <p>
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
@Service
public class SelfUpdateService implements ISelfUpdateService {

    @Autowired
    @Qualifier("outgoing-channel")
    private MessageChannel outgoingChannel;

    @Autowired
    private INodeConfiguration nodeConfiguration;

    @Autowired
    StateMachine<NodeStates, NodeEvents> stateMachine;

    @Autowired
    IMessageRepository messageRepository;

    @Override
    public void startNodeUpdate(String nodeAddress) {

        UpdateMePacket updateMePacket = new UpdateMePacket();

        // Send UpdateMePacket
        outgoingChannel.send(
                MessageBuilder.withPayload(updateMePacket)
                        .setHeader("ip_address", nodeAddress)
                        .setHeader("ip_port", nodeConfiguration.getPort())
                        .setHeader("uuid", updateMePacket.getUuid())
                        .build()
        );

        // Switch state machine
        stateMachine.sendEvent(NodeEvents.UpdateMeRequestSent);

    }

    @ServiceActivator(inputChannel = "update-me-response-received")
    public void updateReceived(UpdateMeResponse updateMeResponse){

        messageRepository.syncDb(updateMeResponse.getVersion(), updateMeResponse.getMessages());

        // Switch state machine
        stateMachine.sendEvent(NodeEvents.UpdateByRequestReceived);
    }
}
