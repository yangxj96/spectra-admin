package com.devops00.spectra.core.scheduler;

import com.devops00.spectra.common.exception.SchedulerDatabaseUnavailableException;
import com.devops00.spectra.framework.configure.mvc.advice.exception.SqlExceptionAdvice;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 调度数据库异常的 HTTP fail-closed 契约。 */
class SchedulerDatabaseUnavailableTest {

    @Test
    void returns503WithStableSchedulerErrorCode() {
        var response = new MockHttpServletResponse();
        var result = new SqlExceptionAdvice().handleSchedulerDatabaseUnavailable(
                new SchedulerDatabaseUnavailableException(new IllegalStateException("down")), response);

        assertEquals(HttpServletResponse.SC_SERVICE_UNAVAILABLE, response.getStatus());
        assertEquals(HttpServletResponse.SC_SERVICE_UNAVAILABLE, result.getCode());
        assertEquals(SchedulerDatabaseUnavailableException.CODE, result.getMsg());
    }
}
