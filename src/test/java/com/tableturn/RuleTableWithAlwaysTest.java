package com.tableturn;

import org.springframework.test.context.TestPropertySource;

/** The same table and rows, with SMALL_PARTY_UPGRADE always and its column expected. */
@TestPropertySource(properties = "small-party-upgrade=always")
class RuleTableWithAlwaysTest extends RuleTable {

    @Override
    int column() {
        return 2;
    }
}