package com.tableturn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The rule table. One row per case (src/test/resources/rules.csv): its setup, its action, and the status and
 * type expected under each SMALL_PARTY_UPGRADE setting. Every row runs on a fresh floor, on the whole app through
 * MockMvc, with a fixed clock. The three subclasses run the same table under each setting.
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:rule-table")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
abstract class RuleTable {

    private static final ObjectMapper JSON = new ObjectMapper();

    @TestConfiguration
    static class FixedClock {
        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(                    Instant.parse("2026-09-27T19:10:00Z"),                    ZoneOffset.UTC);
        }
    }

    @Autowired
    MockMvc http;

    /** Which column of expectations this run checks: 0 default, 1 never, 2 always.     */
    abstract int column();

    @ParameterizedTest(name = "{0} [{1}]")
    @CsvFileSource(resources = "/rules.csv", numLinesToSkip = 1)
    void rule(           String name, String tag, String setup, String action,            int statusDefault, String typeDefault,
              int statusNever, String typeNever, int statusAlways, String typeAlways)            throws Exception {
        List<String> parties = new ArrayList<>();
        String seating = null;
        for (String step : setup == null ? new String[0] : setup.split(";")) {
            String[] w = step.trim().split(" ");
            switch (w[0]) {
                case "join" -> parties.add(join(w[1]).path("partyId").asText());
                case "seat" -> seating = seat(                        parties.get(parties.size() - 1), w[1])                        .path("seatingId").asText();
                case "leave" -> perform("/seatings/" + seating + "/leave", null);
                default -> throw new IllegalArgumentException("unknown step " + step);
            }
        }
        String[] a = action.split(" ");
        MockHttpServletResponse answer = "join".equals(a[0])
                ? perform("/waitlist", "{\"name\":\"Party\",\"covers\":" + a[1] + "}")
                : perform("/seatings", "{\"partyId\":\""                        + (a.length == 3 ? a[1] : parties.get(parties.size() - 1))
                        + "\",\"table\":\"" + a[a.length - 1] + "\"}");
    
    int[] statuses = {statusDefault, statusNever, statusAlways};
        String[] types = {typeDefault, typeNever, typeAlways};

        assertThat(answer.getStatus())
                .as(name + " status")
                .isEqualTo(statuses[column()]);

        if (types[column()] != null) {
            assertThat(JSON.readTree(answer.getContentAsString()).path("type").asText())
                    .as(name + " type")
                    .isEqualTo(types[column()]);
        }
    }

    private JsonNode join(String covers) throws Exception {
        return JSON.readTree(
                perform("/waitlist", "{\"name\":\"Party\",\"covers\":" + covers + "}")
                        .getContentAsString());
    }

    private JsonNode seat(String party, String table) throws Exception {
        return JSON.readTree(
                perform("/seatings", "{\"partyId\":\"" + party + "\",\"table\":\"" + table + "\"}")
                        .getContentAsString());
    }

    private MockHttpServletResponse perform(String path, String body) throws Exception {
        var request = post(path);
        if (body != null) {
            request = request.contentType(MediaType.APPLICATION_JSON).content(body);
        }
        return http.perform(request).andReturn().getResponse();
    }
}
