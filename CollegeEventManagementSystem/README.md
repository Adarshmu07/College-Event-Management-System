# College Event Management System (Java Swing + SQLite)

An advanced desktop application for managing college events, venues, students,
registrations, attendance, feedback and reports.

## Folder structure

```
CollegeEventManagementSystem/
  src/   (all Java source files)
  lib/   (put the two sqlite-jdbc jar files here)
  README.md
```

## One-time setup

Download these two files from https://github.com/xerial/sqlite-jdbc/releases
(same version number) and put them in the `lib` folder:

- sqlite-jdbc-X.X.X.X-without-natives.jar
- sqlite-jdbc-X.X.X.X-natives-windows.jar   (use natives-mac / natives-linux on those systems)

## Compile and run (Windows)

Open Command Prompt inside the project folder, then:

```
javac -cp "lib/*" -d bin src/*.java
java -cp "bin;lib/*" Main
```

On macOS / Linux use `:` instead of `;` in the second command:
`java -cp "bin:lib/*" Main`

The database file `cems.db` is created automatically on first run and is filled
with sample venues, students, events and registrations.
To start again with fresh sample data, close the app and delete `cems.db`.

## Logins

| Role          | Username | Password |
|---------------|----------|----------|
| Administrator | admin    | admin123 |
| Coordinator   | staff    | staff123 |

Only the Administrator can delete events, students and venues.

## What the app does

- **Dashboard** - live counts, registrations-per-event bar chart, events-by-category
  chart, upcoming events with seat bars, and a recent activity log.
- **Events** - add, edit, delete, search, filter by category and status
  (Upcoming / Today / Completed), view participants, export CSV.
- **Students** - add, edit, delete, search, filter by department, event history per
  student, validation of email and phone, export CSV.
- **Registrations** - register a student for an event, mark attendance, collect
  rating and feedback, cancel, filter and export.
- **Venues** - manage venues and capacity, see each venue's schedule.
- **Reports** - event summary, department analysis, top students, venue usage,
  each exportable to CSV.

## How the data is connected

- An event must have a venue. Its max participants cannot exceed the venue capacity,
  and two events cannot use the same venue at overlapping times.
- Registration is blocked when the event is full, already over, a duplicate,
  or clashes with another event the same student has at that date and time.
- Attendance can only be marked on or after the event date; only students marked
  Present can give a rating.
- Deleting an event or a student also removes their registrations.
  A venue that still hosts events cannot be deleted.
- Every change is written to the activity log shown on the dashboard.

## Showing the database to your teacher

Open `cems.db` with DB Browser for SQLite (sqlitebrowser.org) and use the
Browse Data tab to show the tables: users, venues, students, events,
registrations and activity.
