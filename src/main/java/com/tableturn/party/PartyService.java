package com.tableturn.party;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Service;

import com.tableturn.error.NoSuchParty;
import com.tableturn.error.PartyTooBig;
import com.tableturn.table.BranchRepository;
import com.tableturn.table.TableRepository;

/** The waitlist: parties join it and are read back. */
@Service
public class PartyService {

    private final BranchRepository branches;
    private final TableRepository tables;
    private final PartyRepository parties;
    private final Clock clock;

    public PartyService(BranchRepository branches, TableRepository tables, PartyRepository parties, Clock clock) {
        this.branches = branches;
        this.tables = tables;
        this.parties = parties;
        this.clock = clock;
    }

    /**A party no table in the branch could ever seat is refused before anything is written.*/
    public JoinResponse join(String branch, String name, int covers, String phone) {
        long branchId = branchId(branch);
        int maxCovers = tables.largest(branchId);
        if (covers > maxCovers) {
            throw new PartyTooBig(covers, maxCovers);
        }
        long id = parties.insert(branchId, name, covers, phone, clock.instant().truncatedTo(ChronoUnit.SECONDS));
        PartyRepository.Row row = parties.find(branchId, id).orElseThrow();
        return new JoinResponse(PartyIds.format(id), row.name(), row.covers(), row.position(), row.joinedAt());
    }

    public PartyView party(String branch, String partyId) {
        long branchId = branchId(branch);
        PartyRepository.Row row = PartyIds.parse(partyId).flatMap(id -> parties.find(branchId, id))
                .orElseThrow(() -> new NoSuchParty(partyId));
        return new PartyView(partyId, row.name(), row.covers(), row.state(), row.position(), row.joinedAt());
    }

    /**The waitlist has no branch in its path: the one branch is seeded at start-up, so it is always there.*/
    private long branchId(String branch) {
        return branches.idOf(branch).orElseThrow();
    }
}

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
