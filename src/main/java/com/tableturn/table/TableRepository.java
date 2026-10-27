package com.tableturn.table;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** The tables table. */
@Repository
public class TableRepository {

    /**
     * One tables row: its number, its seats, its section and the seating on it - with that seating's party, the
     * moment it sat down and the moment it left (all null when the table has no seating).
     */
    public record Row(long id, String no, int seats, Section section, Long currentSeatingId, Long partyId,
            Instant seatedAt, Instant leftAt) {
    }

    private static final String SELECT = """
            SELECT t.id, t.no, t.seats, t.section, t.current_seating_id, s.party_id, s.seated_at, s.left_at
            FROM tables t LEFT JOIN seatings s ON s.id = t.current_seating_id
            WHERE t.branch_id = ?""";

    private final JdbcClient jdbc;

    public TableRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** Every table of a branch, lowest number first. */
    public List<Row> all(long branchId) {
        return jdbc.sql(SELECT + " ORDER BY t.no").param(branchId).query(Row.class).list();
    }

    public Optional<Row> one(long branchId, String no) {
        return jdbc.sql(SELECT + " AND t.no = ?").params(branchId, no).query(Row.class).optional();
    }

    /** The seats of the largest table in the branch: no party bigger than this can ever be seated. */
    public int largest(long branchId) {
        return jdbc.sql("SELECT MAX(seats) FROM tables WHERE branch_id = ?").param(branchId)
                .query(Integer.class).single();
    }

    /** The table now holds this seating. */
    public void seat(long tableId, long seatingId) {
        jdbc.sql("UPDATE tables SET current_seating_id = ? WHERE id = ?")
                .params(seatingId, tableId).update();
    }
/** Cleared: the table holds no seating again. */
    public void clear(long tableId, Instant clearedAt) {
        jdbc.sql("UPDATE tables SET current_seating_id = NULL, cleared_at = ? WHERE id = ?")
                .params(clearedAt, tableId)
                .update();
    }

    public void insert(long branchId, String no, int seats, Section section) {
        jdbc.sql("INSERT INTO tables (branch_id, no, seats, section) VALUES (?, ?, ?, ?)")
                .params(branchId, no, seats, section.name()).update();
    }
}
