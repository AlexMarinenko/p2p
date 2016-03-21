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

package ru.asmsoft.p2p.fsm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.annotation.OnTransitionEnd;
import org.springframework.statemachine.annotation.WithStateMachine;
import ru.asmsoft.p2p.packets.StartTransactionPacket;
import ru.asmsoft.p2p.storage.INodeRepository;
import ru.asmsoft.p2p.storage.entity.P2PMessage;


@WithStateMachine
public class NodeLifeCycle {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private IBuffer incomingBuffer;

    @Autowired
    private INodeRepository nodeRepository;

    @Autowired
    @Qualifier("outgoing-broadcast")
    private MessageChannel outgoingBroadcastChannel;

    @OnTransitionEnd(target = "CONNECTED")
    public void onConnected(StateContext<NodeStates, NodeEvents> contextState){
        if (!incomingBuffer.isEmpty()){
            StartTransactionPacket startTransactionPacket = new StartTransactionPacket();
            outgoingBroadcastChannel.send(MessageBuilder.withPayload(startTransactionPacket).setHeader("uuid", startTransactionPacket.getUuid()).build());
            contextState.getStateMachine().sendEvent(NodeEvents.StartTransactionSent);
        }
    }

    @OnTransitionEnd(target = "INCOMING_MESSAGE_RECEIVED")
    public void onIncomingMessage(StateContext<NodeStates, NodeEvents> contextState){
        P2PMessage message = (P2PMessage) contextState.getMessageHeader("MESSAGE");
        incomingBuffer.acceptMessage(message);
    }

}
