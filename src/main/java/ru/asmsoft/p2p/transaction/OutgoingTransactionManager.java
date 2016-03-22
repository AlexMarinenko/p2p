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
import org.springframework.integration.store.MessageGroup;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;
import ru.asmsoft.p2p.configuration.INodeConfiguration;
import ru.asmsoft.p2p.fsm.IBuffer;
import ru.asmsoft.p2p.fsm.NodeEvents;
import ru.asmsoft.p2p.fsm.NodeStates;
import ru.asmsoft.p2p.packets.CommitTransactionPacket;
import ru.asmsoft.p2p.packets.MessagePacket;
import ru.asmsoft.p2p.packets.P2PPacket;
import ru.asmsoft.p2p.packets.RollbackTransactionPacket;
import ru.asmsoft.p2p.storage.IMessageRepository;
import ru.asmsoft.p2p.storage.INodeRepository;
import ru.asmsoft.p2p.storage.NoChangesetFoundException;

@Service("outgoingTransactionManager")
public class OutgoingTransactionManager {

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

        return MessageBuilder.withPayload(response)
                .setHeader("ip_address", requestMessage.getHeaders().get("ip_address"))
                .setHeader("ip_port", nodeConfiguration.getPort())
                .setHeader("uuid", response.getUuid())
                .build();

    }

    @ServiceActivator(inputChannel = "outgoing-start-transaction-started-channel")
    public void startUpdateRemoteNodes(Message message){

        logger.info("Transaction started. Start updating remote nodes.");

        // Switch state machine
        stateMachine.sendEvent(NodeEvents.UpdateRemoteNodes);

    }

    @ServiceActivator(inputChannel = "outgoing-start-transaction-failed-channel")
    public void failedStartTransaction(){

        logger.error("Transaction failed. Yield.");

        // Cancel pending changes
        try {
            incomingBuffer.add(messageRepository.cancelChangeset(messageRepository.getDbVersion() + 1));
        } catch (NoChangesetFoundException e) {
            logger.error(e.getMessage());
        }

        // Switch state machine
        stateMachine.sendEvent(NodeEvents.StartTransactionFailed);
    }

    @ServiceActivator(inputChannel = "outgoing-update-transaction-confirmed-channel", outputChannel = "outgoing-broadcast")
    public P2PPacket updateConfirmationsReceived(){

        logger.info("Transaction update confirmed. Committing.");

        // Apply the changes
        try {
            messageRepository.applyChangeset(messageRepository.getDbVersion() + 1);
        } catch (NoChangesetFoundException e) {

            // Handle lost changeset
            logger.error(e.getMessage());

            // Switch state machine on failure
            stateMachine.sendEvent(NodeEvents.RollbackSent);

            // Rollback the changeset on failure
            return new RollbackTransactionPacket(messageRepository.getDbVersion() + 1);
        }

        // Switch state machine on success
        stateMachine.sendEvent(NodeEvents.CommitSent);

        return new CommitTransactionPacket(messageRepository.getDbVersion());
    }

    @ServiceActivator(inputChannel = "outgoing-update-transaction-failed-channel", outputChannel = "outgoing-broadcast")
    public RollbackTransactionPacket updateConfirmationsFailed(){

        logger.error("Transaction update failed. Rolling back.");

        // Cancel changeset
        try {
            messageRepository.cancelChangeset(messageRepository.getDbVersion() + 1);
        } catch (NoChangesetFoundException e) {
            logger.error(e.getMessage());
        }

        // Switch state machine
        stateMachine.sendEvent(NodeEvents.RollbackSent);

        return new RollbackTransactionPacket(messageRepository.getDbVersion() + 1);
    }




}
