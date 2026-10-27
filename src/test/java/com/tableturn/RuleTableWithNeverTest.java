package com.tableturn;

import org.springframework.test.context.TestPropertySource;

/** The same table and rows, with SMALL_PARTY_UPGRADE never and its column expected.*/
@TestPropertySource(properties = "small-party-upgrade=never")
class RuleTableWithNeverTest extends RuleTable {

    @Override
    int column() {
        return 1;
    }
}