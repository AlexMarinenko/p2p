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

package ru.asmsoft.p2p.fsm;

import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;

@Configuration
@EnableStateMachine
public class NodeStateMachine extends EnumStateMachineConfigurerAdapter<NodeStates, NodeEvents> {

    @Override
    public void configure(StateMachineStateConfigurer<NodeStates, NodeEvents> states) throws Exception {
        states
            .withStates()
            .initial(NodeStates.CONNECTED)
            .states(EnumSet.allOf(NodeStates.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<NodeStates, NodeEvents> transitions) throws Exception {

        transitions
                .withExternal()
                .source(NodeStates.CONNECTED).target(NodeStates.IN_TRANSACTION)
                .event(NodeEvents.StartTransactionReceived)
            .and()
            .withExternal()
                .source(NodeStates.IN_TRANSACTION).target(NodeStates.UPDATED_BY_REMOTE)
                .event(NodeEvents.UpdateReceived)
            .and()
                .withExternal()
                .source(NodeStates.UPDATED_BY_REMOTE).target(NodeStates.CONNECTED)
                .event(NodeEvents.CommitReceived)
            .and()
                .withExternal()
                .source(NodeStates.UPDATED_BY_REMOTE).target(NodeStates.CONNECTED)
                .event(NodeEvents.RollbackReceived)

            .and()
                .withExternal()
                .source(NodeStates.CONNECTED).target(NodeStates.INCOMING_MESSAGE_RECEIVED)
                .event(NodeEvents.IncomingMessageArrived)
            .and()
                .withExternal()
                .source(NodeStates.INCOMING_MESSAGE_RECEIVED).target(NodeStates.STARTED_TRANSACTION)
                .event(NodeEvents.StartTransactionSent)
            .and()
                .withExternal()
                .source(NodeStates.STARTED_TRANSACTION).target(NodeStates.UPDATING_REMOTE)
                .event(NodeEvents.UpdateRemoteNodes)
            .and()
                .withExternal()
                .source(NodeStates.UPDATING_REMOTE).target(NodeStates.CONNECTED)
                .event(NodeEvents.CommitSent)
            .and()
                .withExternal()
                .source(NodeStates.UPDATING_REMOTE).target(NodeStates.CONNECTED)
                .event(NodeEvents.RollbackSent)

            .and()
                .withExternal()
                .source(NodeStates.CONNECTED).target(NodeStates.SENDING_UPDATE)
                .event(NodeEvents.UpdateMeRequestReceived)
            .and()
                .withExternal()
                .source(NodeStates.SENDING_UPDATE).target(NodeStates.CONNECTED)
                .event(NodeEvents.UpdateByRequestSent)

            .and()
                .withExternal()
                .source(NodeStates.CONNECTED).target(NodeStates.WAIT_FOR_UPDATE_BY_REQUEST)
                .event(NodeEvents.UpdateMeRequestSent)
                .and()
                .withExternal()
                .source(NodeStates.WAIT_FOR_UPDATE_BY_REQUEST).target(NodeStates.CONNECTED)
                .event(NodeEvents.UpdateByRequestReceived);

    }
}
