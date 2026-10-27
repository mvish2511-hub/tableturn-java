package com.tableturn.party;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tableturn.config.TableTurnSettings;
import com.tableturn.error.NoSuchParty;
import com.tableturn.error.PartyTooBig;
import com.tableturn.table.BranchRepository;
import com.tableturn.table.TableRepository;

/**
 * The waitlist: parties join it, quoted a wait, and are read back.
 * Every time comes from the injected clock.
 */
@Service
public class PartyService {

    private final BranchRepository branches;
    private final TableRepository tables;
    private final PartyRepository parties;
    private final Clock clock;
    private final TableTurnSettings settings;
    private final EventLog events;

    public PartyService(
            BranchRepository branches,
            TableRepository tables,
            PartyRepository parties,
            Clock clock,
            TableTurnSettings settings,
            EventLog events) {
        this.branches = branches;
        this.tables = tables;
        this.parties = parties;
        this.clock = clock;
        this.settings = settings;
        this.events = events;
    }

    /**
     * A party no table in the branch could ever seat is refused before
     * anything is written. The quote is the default-quote-minutes setting,
     * stored with the moment the party joined; the join and its two events are one
     * transaction.
     */
    @Transactional
    public JoinResponse join(String branch, String name, int covers, String phone) {
        long branchId = branchId(branch);

        int maxCovers = tables.largest(branchId);
        if (covers > maxCovers) {
            throw new PartyTooBig(covers, maxCovers);
        }

        Instant now = clock.instant().truncatedTo(ChronoUnit.SECONDS);

        long id = parties.insert(
                branchId,
                name,
                covers,
                phone,
                now,
                settings.defaultQuoteMinutes()
        );

        events.append(branchId, id, null, "joined", now, Map.of("covers", covers));
        events.append(branchId, id, null, "quoted", now,
                Map.of("quotedMinutes", settings.defaultQuoteMinutes()));

        PartyRepository.Row row = parties.find(branchId, id).orElseThrow();

        return new JoinResponse(
                PartyIds.format(id),
                row.name(),
                row.covers(),
                row.position(),
                row.joinedAt(),
                row.quotedMinutes(),
                row.quotedAt()
        );
    }

    /**
     * Reads a party, including its actual wait and table when seated.
     */
    public PartyView party(String branch, String partyId) {
        long branchId = branchId(branch);

        PartyRepository.Row row = PartyIds.parse(partyId)
                .flatMap(id -> parties.find(branchId, id))
                .orElseThrow(() -> new NoSuchParty(partyId));

        return new PartyView(
                partyId,
                row.name(),
                row.covers(),
                row.state(),
                row.position(),
                row.joinedAt(),
                row.quotedMinutes(),
                row.quotedAt(),
                row.waitedMinutes(),
                row.table(),
                events.forParty(row.id())
        );
    }

    /**
     * Stamps the real wait once at seating: whole minutes since the party
     * joined, rounded down. A read never recomputes it.
     */
    public int stampWait(long partyId, Instant joinedAt, Instant seatedAt) {
        int waited = (int) Duration.between(joinedAt, seatedAt).toMinutes();

        parties.stampWait(partyId, waited);

        return waited;
    }

    /**
     * The waitlist has no branch in its path: the one branch is seeded at
     * start-up, so it is always there.
     */
    private long branchId(String branch) {
        return branches.idOf(branch).orElseThrow();
    }
}

/**
 * Party entity.
 */
class Party {

    private Long id;
    private Long branchId;
    private String name;
    private int covers;
    private String phone;
    private String state;
    private OffsetDateTime joinedAt;
    private int position;

    public Party() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBranchId() {
        return branchId;
    }

    public void setBranchId(Long branchId) {
        this.branchId = branchId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCovers() {
        return covers;
    }

    public void setCovers(int covers) {
        this.covers = covers;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public OffsetDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(OffsetDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
