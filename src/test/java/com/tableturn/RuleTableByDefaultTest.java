package com.tableturn;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.junit.jupiter.api.Test;

/** The same table and rows, with SMALL_PARTY_UPGRADE at its default (when_nobody_larger_waits). */
class RuleTableByDefaultTest extends RuleTable {

    @Override
    int column() {
        return 0;
    }

    @Test
    void settingsShowTheDefaults() throws Exception {
        http.perform(get("/settings"))
                .andExpect(jsonPath("$.defaultQuoteMinutes").value(40))
                .andExpect(jsonPath("$.smallPartyUpgrade").value("when_nobody_larger_waits"))
                .andExpect(jsonPath("$.maxCovers").value(6));
    }
}
