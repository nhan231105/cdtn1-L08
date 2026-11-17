package com.mekongmobile.smartcrm;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HealthControllerTest {
    @Test
    void reportsDatabaseUpWhenProbeSucceeds() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForObject("SELECT 1", Integer.class)).thenReturn(1);

        var response = new HealthController(jdbc).health();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(new HealthController.HealthResponse("ok", "up"), response.getBody());
    }

    @Test
    void reportsDatabaseDownWithoutExposingTheDatabaseError() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForObject("SELECT 1", Integer.class))
                .thenThrow(new DataAccessResourceFailureException("internal connection detail"));

        var response = new HealthController(jdbc).health();

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals(new HealthController.HealthResponse("error", "down"), response.getBody());
    }
}
