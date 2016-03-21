/**
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 20.03.16 <Alex S. Marinenko> alex.marinenko@gmail.com
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

package ru.asmsoft.p2p.incoming;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ru.asmsoft.p2p.storage.entity.P2PMessage;
import ru.asmsoft.p2p.fsm.NodeEvents;
import ru.asmsoft.p2p.fsm.NodeStates;

/**
 * Rest controller
 * Receives incoming messages for a node
 */
@RestController
public class InputController {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    StateMachine<NodeStates, NodeEvents> stateMachine;

    @RequestMapping(value = "/send", method = RequestMethod.POST)
    public MessageAcceptResult sendMessage(@RequestBody P2PMessage message){

        // Pass incoming message to the State Machine
        stateMachine.sendEvent(MessageBuilder.withPayload(NodeEvents.IncomingMessageArrived).setHeader("MESSAGE", message).build());
        stateMachine.sendEvent(NodeEvents.IncomingMessageAccepted);

        return new MessageAcceptResult(true);
    }

}
