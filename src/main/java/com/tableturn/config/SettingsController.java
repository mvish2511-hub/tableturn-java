package com.tableturn.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tableturn.table.TableService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/** The live settings, read-only - the rule book screen reads them from here.*/
@RestController
@Tag(name = "settings", description = "The rules the app is running with")
public class SettingsController {

    static final String BRANCH = "Baner";

    private final TableTurnSettings settings;
    private final TableService tables;

    public SettingsController(TableTurnSettings settings, TableService tables) {
        this.settings = settings;
        this.tables = tables;
    }

    @GetMapping("/settings")
    @Operation(summary = "The live quote, the small-party upgrade rule and the largest party a table here seats")
    @ApiResponse(responseCode = "200", description = "The current settings")
    public SettingsView settings() {
        return new SettingsView(                settings.defaultQuoteMinutes(), settings.smallPartyUpgrade(),
                tables.maxCovers(BRANCH));
    }
}
