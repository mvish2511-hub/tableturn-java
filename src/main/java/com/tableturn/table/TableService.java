package com.tableturn.table;

import java.time.Clock;
import java.time.Duration;
import java.util.List;

import org.springframework.stereotype.Service;

import com.tableturn.error.NoSuchBranch;
import com.tableturn.error.NoSuchTable;
import com.tableturn.party.PartyIds;

/**
 * The floor: every table with its state, derived from its current seating on every read.
 */
@Service
public class TableService {

    private final BranchRepository branches;
    private final TableRepository tables;
    private final Clock clock;

    public TableService(BranchRepository branches, TableRepository tables, Clock clock) {
        this.branches = branches;
        this.tables = tables;
        this.clock = clock;
    }

    /**
     * The floor, optionally only one section and/or one state; free counts the free tables among those shown.
     */
    public FloorView floor(String branch, Section section, TableState state) {
        long branchId = branches.idOf(branch)
                .orElseThrow(() -> new NoSuchBranch(branch));

        List<TableView> shown = tables.all(branchId).stream()
                .map(this::view)
                .filter(t -> section == null || t.section().equals(section.name()))
                .filter(t -> state == null || t.state() == state)
                .toList();

        int free = (int) shown.stream()
                .filter(t -> t.state() == TableState.free)
                .count();

        return new FloorView(branch, free, shown);
    }

    public TableView table(String branch, String no) {
        long branchId = branches.idOf(branch)
                .orElseThrow(() -> new NoSuchBranch(branch));

        return view(tables.one(branchId, no)
                .orElseThrow(() -> new NoSuchTable(branch, no)));
    }

    /**
     * The seats of the largest table in the branch - the largest party it can ever seat.
     */
    public int maxCovers(String branch) {
        long branchId = branches.idOf(branch)
                .orElseThrow(() -> new NoSuchBranch(branch));

        return tables.largest(branchId);
    }

    /**
     * Derived on every read: free when no seating is current; uncleared
     * once its party has left; seated while the party is still there,
     * with the minutes it has sat counted from the clock.
     */
    private TableView view(TableRepository.Row row) {
        if (row.currentSeatingId() == null) {
            return new TableView(
                    row.no(), row.seats(), row.section().name(),
                    TableState.free, null, null);
        }

        if (row.leftAt() != null) {
            return new TableView(
                    row.no(), row.seats(), row.section().name(),
                    TableState.uncleared, null, null);
        }

        int seatedMinutes = (int) Duration.between(
                row.seatedAt(), clock.instant()).toMinutes();

        return new TableView(
                row.no(), row.seats(), row.section().name(),
                TableState.seated,
                PartyIds.format(row.partyId()),
                seatedMinutes);
    }
}