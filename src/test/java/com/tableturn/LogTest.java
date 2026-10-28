package com.tableturn;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.web.client.TestRestTemplate;

/** Every request and every event leaves one line on stdout, and no line ever holds a guest's phone. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:log-test")
@ExtendWith(OutputCaptureExtension.class)
class LogTest {

    @Autowired
    TestRestTemplate http;

@Test
    void aJoinAndASeatingAreLoggedWithoutThePhone(CapturedOutput output) {
        http.postForObject("/waitlist", Map.of("name", "Kulkarni", "covers", 6, "phone", "+919876543210"),                Map.class);
        assertThat(http.postForEntity("/seatings", Map.of("partyId", "W001", "table", "T20"), String.class)
                        .getStatusCode().value()).isEqualTo(201);

        String out = output.getOut();
        assertThat(out.lines().filter(l -> l.contains("method=POST") && l.contains("path=/waitlist"))).hasSize(1);
        assertThat(out.lines().filter(l -> l.contains("method=POST") && l.contains("path=/seatings"))).hasSize(1);
        assertThat(out).contains("kind=joined", "kind=quoted", "kind=seated");
        assertThat(output.getAll()).doesNotContain("9876543210");
    }
}