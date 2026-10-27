# TableTurn

The floor of a restaurant on a Saturday night: the waitlist, the tables, and who sits where.

## Run it in a container

You need Docker and nothing else: the image builds the app itself. From this folder:

    docker build -t tableturn .
    docker run -p 8080:8080 tableturn

Open http://localhost:8080/ for the floor - or http://<the till PC's IP>:8080/ from the kitchen screen.
`http://localhost:8080/actuator/health` answers `{"status":"UP"}` while the app is up and can reach its database.

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
- [ ] Ticket 10 · It survives the nightly power cut

Built by Vishnuppriya M on [LearnTube Guided Projects](https://guidedprojects.learntube.ai).
<!-- learntube:end -->
