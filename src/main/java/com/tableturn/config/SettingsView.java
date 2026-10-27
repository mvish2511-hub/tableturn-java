package com.tableturn.config;

/** GET /settings: the rules the app is running with right now.*/
public record SettingsView(int defaultQuoteMinutes, SmallPartyUpgrade smallPartyUpgrade, int maxCovers) {
}