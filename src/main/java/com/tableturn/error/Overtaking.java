package com.tableturn.error;

import java.util.Map;

/**
 * 409 /errors/overtaking: the setting does not let this party take a table with more seats than covers. When the
 * reason is a larger party waiting, mustSeatFirst names it.
 */
public class Overtaking extends DomainException {

    private final String mustSeatFirst;

    public Overtaking(String table, String mustSeatFirst) {
        super(409,"/errors/overtaking","Overtaking",                mustSeatFirst == null
                        ? "A smaller party may not take table " + table + " at this restaurant"
                        : "Party " + mustSeatFirst + " is waiting and fits table " + table + "; seat them first");
        this.mustSeatFirst = mustSeatFirst;
    }

    @Override
    public Map<String, Object> properties() {
        return mustSeatFirst == null? Map.of()                : Map.of("mustSeatFirst", mustSeatFirst);
    }
}
