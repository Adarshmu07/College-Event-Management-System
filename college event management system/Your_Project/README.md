# Contribution package: YOUR HALF

This ZIP is a complete runnable working copy, including the other contributor's
source files as dependencies. To keep the two halves separate, edit and commit
ONLY the files listed under YOUR OWNED FILES. Treat the other files as shared
reference/dependencies and do not make competing edits to them.

## YOUR OWNED FILES
- `src/Main.java`
- `src/LoginFrame.java`
- `src/MainFrame.java`
- `src/Page.java`
- `src/UI.java`
- `src/DashboardPage.java`
- `src/StudentsPage.java`
- `src/VenuesPage.java`

## OTHER CONTRIBUTOR'S FILES — DO NOT EDIT
- `src/Db.java`
- `src/EventsPage.java`
- `src/RegistrationsPage.java`
- `src/ReportsPage.java`

## Shared / included resources
- `lib/` — JDBC driver JAR files required to compile/run.
- `cems_mysql.sql` — SQL script; ownership belongs to the database contributor.
- `README_ORIGINAL.md` — original project instructions.
- `README.md` — this contribution split and setup guide.

## Important
- Both contributors work in the SAME GitHub repository, on separate branches.
- Before merging, copy/commit only your owned files (plus agreed changes to shared
  files). Do not replace the whole repository with this ZIP, because that could
  overwrite your partner's newer work.
- The app source uses SQLite through `sqlite-jdbc`; `cems.db` is created locally
  when the application runs. The supplied MySQL SQL file is retained as part of
  the original project, but it is not the runtime database used by `Db.java`.

## Compile and run (Windows)
Open Command Prompt in this folder:

```bat
javac -cp "lib/*" -d bin src/*.java
java -cp "bin;lib/*" Main
```

On macOS/Linux, use `:` instead of `;` in the runtime classpath:

```bash
javac -cp "lib/*" -d bin src/*.java
java -cp "bin:lib/*" Main
```
