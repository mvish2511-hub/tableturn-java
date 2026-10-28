# TableTurn

The floor of a restaurant on a Saturday night: the waitlist, the tables, and who sits where.

## Run it in a container

You need Docker and nothing else: the image builds the app itself (in a first stage, on a JDK) and runs it on a JRE.
From this folder:

    docker build -t tableturn .
    docker run -p 8080:8080 tableturn

Open http://localhost:8080/ for the floor - or http://<the till PC's IP>:8080/ from the kitchen screen. That run keeps
its database inside the container and loses it with the container.

## Keep the parties, and configure it from one file

Every setting can come from the environment; the table below lists the ones you may need to change. Copy
`.env.example` to `.env`, change what you need, and run with the database on a named volume at `/data`:

    cp .env.example .env
    docker run -d --name tableturn -p 8080:8080 -v tableturn-data:/data --env-file .env tableturn

| Variable | What it sets | Default |
|---|---|---|
| `DATABASE_URL` | the JDBC URL of the H2 file | `jdbc:h2:file:./data/tableturn` (`jdbc:h2:file:/data/tableturn` in `.env.example`) |
| `DEFAULT_QUOTE_MINUTES` | the wait every party is quoted when it joins | `40` |
| `SMALL_PARTY_UPGRADE` | may a smaller party take a larger table: `never`, `when_nobody_larger_waits` or `always` | `when_nobody_larger_waits` |
| `HOST` | the address the server listens on | `127.0.0.1` (`0.0.0.0` in the image) |
| `PORT` | the port | `8080` |

To quote 30 minutes instead of 40: change the line in `.env`, then `docker rm -f tableturn` and run the same
`docker run` again. Nothing is rebuilt, and the parties are still on the list, because they live on the volume.

Logs go to stdout: `docker logs tableturn` shows one line per request and one per event - never a phone number.

## One party, from the door to the bill

With the container running on port 8080:

    curl -s http://localhost:8080/actuator/health
    # {"status":"UP"}

    curl -s -X POST http://localhost:8080/waitlist -H 'content-type: application/json' -d '{"name":"Kulkarni","covers":6}'
    # {"partyId":"W001","name":"Kulkarni","covers":6,"position":1,"joinedAt":"...","quotedMinutes":40,"quotedAt":"..."}

    curl -s http://localhost:8080/parties/W001
    # the quote they were told: "quotedMinutes":40

    curl -s -X POST http://localhost:8080/seatings -H 'content-type: application/json' -d '{"partyId":"W001","table":"T20"}'
    # {"seatingId":"S001","partyId":"W001","table":"T20","seatedAt":"...","waitedMinutes":12}

    curl -s -X POST http://localhost:8080/seatings/S001/leave
    # {"seatingId":"S001","leftAt":"..."} - T20 is now uncleared

    curl -s -X POST http://localhost:8080/seatings/S001/clear
    # {"seatingId":"S001","table":"T20","turnMinutes":83,"clearedAt":"..."} - T20 is free again

Use the ids your own calls returned. The API docs are at http://localhost:8080/swagger-ui.html.

<!-- learntube:start -->
---

[![tests](https://github.com/mvish2511-hub/tableturn-java/actions/workflows/tests.yml/badge.svg)](https://github.com/mvish2511-hub/tableturn-java/actions/workflows/tests.yml)

### Progress

- [x] Ticket 1 · The floor, as something you can look at
- [x] Ticket 2 · A party joins the list
- [x] Ticket 3 · The wait you quote, and the wait they actually have
- [x] Ticket 4 · Three ways to say no
- [x] Ticket 5 · The table turns
- [x] Ticket 6 · A board nobody can wipe
- [x] Ticket 7 · One file becomes an app
- [x] Ticket 8 · The rule table, and then the rule changes
- [x] Ticket 9 · It runs on the machine by the till
- [x] Ticket 10 · It survives the nightly power cut

Built by Vishnuppriya M on [LearnTube Guided Projects](https://guidedprojects.learntube.ai).
<!-- learntube:end -->
