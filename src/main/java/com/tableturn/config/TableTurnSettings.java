package com.tableturn.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * The app's own settings, read once at start-up from application.yml. Spring's own settings - the database
 * URL, the host and the port - stay under spring.datasource and server.
 *
 * @param defaultQuoteMinutes the wait every party is quoted when it joins the list
 * @param seedDir              the folder that holds tables.csv
 * @param smallPartyUpgrade           whether a party may take a table with more seats than covers
 */
@ConfigurationProperties
public record TableTurnSettings(@DefaultValue("40") int defaultQuoteMinutes, @DefaultValue("seed") String seedDir,
@DefaultValue("when_nobody_larger_waits") SmallPartyUpgrade smallPartyUpgrade ){
}