package com.devops00.spectra.core.scheduler.javabean.vo;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** 调度接口值对象不会暴露可变集合。 */
class SchedulerValueObjectImmutabilityTest {

    @Test
    void catalogCopiesMapsAndActions() {
        var parameterSchema = new HashMap<String, Object>();
        var supportedActions = new ArrayList<String>();
        var executionPolicy = new HashMap<String, Object>();
        parameterSchema.put("before", true);
        supportedActions.add("VIEW");
        executionPolicy.put("before", true);

        var value = SchedulerCatalogVO.builder()
                .parameterSchema(parameterSchema)
                .supportedActions(supportedActions)
                .executionPolicy(executionPolicy)
                .build();

        parameterSchema.put("after", true);
        supportedActions.add("TRIGGER");
        executionPolicy.put("after", true);

        assertEquals(Map.of("before", true), value.parameterSchema());
        assertEquals(java.util.List.of("VIEW"), value.supportedActions());
        assertEquals(Map.of("before", true), value.executionPolicy());
        assertThrows(UnsupportedOperationException.class, () -> value.parameterSchema().put("x", true));
        assertThrows(UnsupportedOperationException.class, () -> value.supportedActions().add("x"));
        assertThrows(UnsupportedOperationException.class, () -> value.executionPolicy().put("x", true));
    }

    @Test
    void executionJobAndErrorCopyMaps() {
        var executionParameters = new HashMap<String, Object>();
        var resultSummary = new HashMap<String, Object>();
        var executionPolicy = new HashMap<String, Object>();
        var jobParameters = new HashMap<String, Object>();
        var lastContext = new HashMap<String, Object>();
        executionParameters.put("key", "execution");
        resultSummary.put("key", "result");
        executionPolicy.put("key", "policy");
        jobParameters.put("key", "job");
        lastContext.put("key", "error");

        var execution = SchedulerExecutionVO.builder()
                .parametersSnapshot(executionParameters)
                .resultSummary(resultSummary)
                .build();
        var job = SchedulerJobVO.builder().executionPolicy(executionPolicy).parameters(jobParameters).build();
        var error = SchedulerLoopErrorVO.builder().lastContext(lastContext).build();

        executionParameters.put("mutated", true);
        resultSummary.put("mutated", true);
        executionPolicy.put("mutated", true);
        jobParameters.put("mutated", true);
        lastContext.put("mutated", true);

        assertEquals(Map.of("key", "execution"), execution.parametersSnapshot());
        assertEquals(Map.of("key", "result"), execution.resultSummary());
        assertEquals(Map.of("key", "policy"), job.executionPolicy());
        assertEquals(Map.of("key", "job"), job.parameters());
        assertEquals(Map.of("key", "error"), error.lastContext());
        assertThrows(UnsupportedOperationException.class, () -> execution.parametersSnapshot().put("x", true));
        assertThrows(UnsupportedOperationException.class, () -> execution.resultSummary().put("x", true));
        assertThrows(UnsupportedOperationException.class, () -> job.executionPolicy().put("x", true));
        assertThrows(UnsupportedOperationException.class, () -> job.parameters().put("x", true));
        assertThrows(UnsupportedOperationException.class, () -> error.lastContext().put("x", true));
    }
}
