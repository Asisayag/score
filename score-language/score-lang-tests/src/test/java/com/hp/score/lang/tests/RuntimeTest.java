/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
package com.hp.score.lang.tests;

import com.google.common.collect.ImmutableMap;
import com.hp.score.api.ExecutionPlan;
import com.hp.score.api.Score;
import com.hp.score.api.TriggeringProperties;
import com.hp.score.events.EventBus;
import com.hp.score.events.EventConstants;
import com.hp.score.events.ScoreEvent;
import com.hp.score.events.ScoreEventListener;
import com.hp.score.lang.runtime.RunEnvironment;
import com.hp.score.lang.entities.ScoreLangConstants;
import com.hp.score.lang.tests.runtime.builders.POCExecutionPlanActionsBuilder;
import com.hp.score.lang.tests.runtime.builders.POCParentExecutionPlanActionsBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.python.util.PythonInterpreter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * User: stoneo
 * Date: 06/10/2014
 * Time: 08:36
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:META-INF/spring/pocContext.xml", "classpath:META-INF/spring/langRuntimeContext.xml"})
public class RuntimeTest {

    @Autowired
    private Score score;

    @Autowired
    private EventBus eventBus;

    private LinkedBlockingQueue<ScoreEvent> queue = new LinkedBlockingQueue<>();

    public static final String USER_INPUTS = "userInputs";

    @Test
    public void testFlow() throws InterruptedException {

        //Parse YAML -> flow

        //Compile Flow -> ExecutionPlan
        POCExecutionPlanActionsBuilder builder = new POCExecutionPlanActionsBuilder();
        ExecutionPlan executionPlan = builder.getExecutionPlan();

        //Trigger ExecutionPlan
        Map<String, Serializable> executionContext = createExecutionContext();
        addUserInputs(executionContext);

        TriggeringProperties triggeringProperties = TriggeringProperties
                .create(executionPlan)
                .setContext(executionContext);
        score.trigger(triggeringProperties);

        registerHandlers();
        Assert.assertEquals(EventConstants.SCORE_FINISHED_EVENT, queue.take().getEventType());
    }

    private void registerHandlers() {
        Set<String> handlerTypes = new HashSet<>();
        handlerTypes.add(EventConstants.SCORE_FINISHED_EVENT);
        handlerTypes.add(EventConstants.SCORE_ERROR_EVENT);
        handlerTypes.add(EventConstants.SCORE_FAILURE_EVENT);
        eventBus.subscribe(new ScoreEventListener() {
            @Override
            public void onEvent(ScoreEvent event) {
                try {
                    queue.put(event);
                } catch (InterruptedException ignore) {
                }
            }
        }, handlerTypes);
    }

    @Test
    public void testSubFlow() throws InterruptedException {

        //Parse YAML -> flow

        //Compile Flow -> ExecutionPlan
        POCExecutionPlanActionsBuilder builder = new POCExecutionPlanActionsBuilder();
        ExecutionPlan executionPlan = builder.getExecutionPlan();

        POCParentExecutionPlanActionsBuilder parentBuilder = new POCParentExecutionPlanActionsBuilder();
        ExecutionPlan parentExecutionPlan = parentBuilder.getExecutionPlan();

        //Trigger ExecutionPlan
        Map<String, Serializable> executionContext = createExecutionContext();
        addUserInputs(executionContext);

        Map<String, ExecutionPlan> dependencies = ImmutableMap.of("childFlow", executionPlan);
        TriggeringProperties triggeringProperties = TriggeringProperties
                .create(parentExecutionPlan)
                .setContext(executionContext)
                .setDependencies(dependencies);
        score.trigger(triggeringProperties);
        registerHandlers();
        Assert.assertEquals(EventConstants.SCORE_FINISHED_EVENT, queue.take().getEventType());
    }

    private void addUserInputs(Map<String, Serializable> executionContext) {
        HashMap<String, Serializable> userInputs = new HashMap<>();
        userInputs.put("name", "orit");
        userInputs.put("id", "123");
        executionContext.put(USER_INPUTS, userInputs);
    }

    private static Map<String, Serializable> createExecutionContext() {
        Map<String, Serializable> executionContext = new HashMap<>();
        executionContext.put(ScoreLangConstants.RUN_ENV, new RunEnvironment());
        return executionContext;
    }


}
