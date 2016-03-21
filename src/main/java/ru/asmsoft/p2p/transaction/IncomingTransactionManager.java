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

package ru.asmsoft.p2p.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;
import ru.asmsoft.p2p.configuration.INodeConfiguration;
import ru.asmsoft.p2p.fsm.IBuffer;
import ru.asmsoft.p2p.fsm.NodeEvents;
import ru.asmsoft.p2p.fsm.NodeStates;
import ru.asmsoft.p2p.packets.*;
import ru.asmsoft.p2p.storage.IMessageRepository;
import ru.asmsoft.p2p.storage.INodeRepository;
import ru.asmsoft.p2p.storage.NoChangesetFoundException;

import java.util.List;
import java.util.UUID;

@Service("incomingTransactionManager")
public class IncomingTransactionManager {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    IBuffer incomingBuffer;

    @Autowired
    StateMachine<NodeStates, NodeEvents> stateMachine;

    @Autowired
    INodeRepository nodeRepository;

    @Autowired
    IMessageRepository messageRepository;

    @Autowired
    INodeConfiguration nodeConfiguration;

    private Message prepareResponse(Message requestMessage, P2PPacket response){

        Message responseMessage = MessageBuilder.withPayload(response)
                .setHeader("ip_address", requestMessage.getHeaders().get("ip_address"))
                .setHeader("ip_port", nodeConfiguration.getPort())
                .setHeader("uuid", response.getUuid())
                .build();

        return responseMessage;

    }

    @ServiceActivator(inputChannel = "incoming-start-transaction", outputChannel = "outgoing-channel")
    public Message handeIncomingStartTransaction(Message<StartTransactionPacket> message){

        logger.info("Incoming transaction received: {}", message.toString());

        stateMachine.sendEvent(NodeEvents.StartTransactionReceived);

        // Prepare response packet
        ConfirmTransactionStartedPacket packet = new ConfirmTransactionStartedPacket(message.getPayload());

        // Send response
        return prepareResponse(message, packet);
    }

    @ServiceActivator(inputChannel = "incoming-update-transaction", outputChannel = "outgoing-channel")
    public Message handeIncomingUpdateTransaction(Message<MessagePacket> message){

        logger.info("Incoming update received: {}", message.toString());

        stateMachine.sendEvent(NodeEvents.UpdateReceived);

        // Prepare response packet
        MessageReceivedPacket packet = new MessageReceivedPacket(message.getPayload());

        // registerChangeset
        MessagePacket messagePacket = message.getPayload();
        messageRepository.registerChangeset(messagePacket.getDbVersion(), messagePacket.getMessages());

        // Send response
        return prepareResponse(message, packet);
    }

    @ServiceActivator(inputChannel = "incoming-commit-transaction")
    public void handleCommitTransaction(Message<CommitTransactionPacket> message){

        logger.info("Commit transaction received: {}", message.toString());

        stateMachine.sendEvent(NodeEvents.CommitReceived);

        // commit
        CommitTransactionPacket commitPacket = message.getPayload();

        try {
            messageRepository.applyChangeset(commitPacket.getDbVersion());
        } catch (NoChangesetFoundException e) {
            // failed to update
            logger.error(e.getMessage());
            // set wrong version to re-sync
            messageRepository.updateDbVersion(-1L);
        }

        // Switch state machine
        stateMachine.sendEvent(NodeEvents.CommitReceived);

    }

    @ServiceActivator(inputChannel = "incoming-rollback-transaction")
    public void handleRollbackTransaction(Message<RollbackTransactionPacket> message){

        logger.info("Rollback transaction received: {}", message.toString());

        // rollback
        RollbackTransactionPacket commitPacket = message.getPayload();

        try {
            messageRepository.cancelChangeset(commitPacket.getDbVersion());
        } catch (NoChangesetFoundException e) {
            logger.error(e.getMessage());
        }

        // Switch state machine
        stateMachine.sendEvent(NodeEvents.RollbackReceived);

    }


}
