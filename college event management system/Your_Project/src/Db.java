import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** All database access (SQLite via JDBC). The file cems.db is created automatically on first run. */
public class Db {

    private static final String URL = "jdbc:sqlite:cems.db";
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static Connection conn;

    public static String currentUser = "";
    public static String currentName = "";
    public static String currentRole = "";

    public static boolean isAdmin() {
        return "Administrator".equals(currentRole);
    }

    // ---------- Core helpers ----------
    public static synchronized Connection c() throws SQLException {
        if (conn == null || conn.isClosed()) {
            conn = DriverManager.getConnection(URL);
            try (Statement st = conn.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON");
            }
        }
        return conn;
    }

    private static void bind(PreparedStatement ps, Object[] p) throws SQLException {
        for (int i = 0; i < p.length; i++) ps.setObject(i + 1, p[i]);
    }

    public static List<Object[]> query(String sql, Object... p) throws SQLException {
        List<Object[]> out = new ArrayList<>();
        try (PreparedStatement ps = c().prepareStatement(sql)) {
            bind(ps, p);
            try (ResultSet rs = ps.executeQuery()) {
                int n = rs.getMetaData().getColumnCount();
                while (rs.next()) {
                    Object[] row = new Object[n];
                    for (int i = 0; i < n; i++) row[i] = rs.getObject(i + 1);
                    out.add(row);
                }
            }
        }
        return out;
    }

    public static int update(String sql, Object... p) throws SQLException {
        try (PreparedStatement ps = c().prepareStatement(sql)) {
            bind(ps, p);
            return ps.executeUpdate();
        }
    }

    /** First column of the first row as a number (0 if there is none). */
    public static long count(String sql, Object... p) throws SQLException {
        List<Object[]> r = query(sql, p);
        if (r.isEmpty() || r.get(0)[0] == null) return 0;
        return ((Number) r.get(0)[0]).longValue();
    }

    public static int num(Object o) {
        return o == null ? 0 : ((Number) o).intValue();
    }

    public static String str(Object o) {
        return o == null ? "" : o.toString();
    }

    public static boolean login(String user, String pass) throws SQLException {
        List<Object[]> r = query("SELECT username, full_name, role FROM users WHERE username=? AND password=?", user, pass);
        if (r.isEmpty()) return false;
        currentUser = str(r.get(0)[0]);
        currentName = str(r.get(0)[1]);
        currentRole = str(r.get(0)[2]);
        return true;
    }

    public static void log(String action) {
        try {
            update("INSERT INTO activity(ts, username, action) VALUES (?,?,?)",
                    LocalDateTime.now().format(STAMP), currentUser.isEmpty() ? "system" : currentUser, action);
        } catch (SQLException ignored) {
        }
    }

    // ---------- Schema ----------
    public static void init() throws SQLException {
        update("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, full_name TEXT, role TEXT)");
        update("CREATE TABLE IF NOT EXISTS venues (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE NOT NULL, " +
                "location TEXT, capacity INTEGER NOT NULL, facilities TEXT)");
        update("CREATE TABLE IF NOT EXISTS students (id INTEGER PRIMARY KEY AUTOINCREMENT, roll_no TEXT UNIQUE NOT NULL, " +
                "name TEXT NOT NULL, department TEXT, year TEXT, email TEXT, phone TEXT)");
        update("CREATE TABLE IF NOT EXISTS events (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, category TEXT, " +
                "event_date TEXT NOT NULL, start_time TEXT NOT NULL, end_time TEXT NOT NULL, venue_id INTEGER NOT NULL, " +
                "organizer TEXT, description TEXT, max_participants INTEGER NOT NULL, " +
                "FOREIGN KEY (venue_id) REFERENCES venues(id))");
        update("CREATE TABLE IF NOT EXISTS registrations (id INTEGER PRIMARY KEY AUTOINCREMENT, event_id INTEGER NOT NULL, " +
                "student_id INTEGER NOT NULL, reg_date TEXT, attendance TEXT DEFAULT 'Pending', rating INTEGER DEFAULT 0, " +
                "feedback TEXT, UNIQUE (event_id, student_id), " +
                "FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE)");
        update("CREATE TABLE IF NOT EXISTS activity (id INTEGER PRIMARY KEY AUTOINCREMENT, ts TEXT, username TEXT, action TEXT)");

        if (count("SELECT COUNT(*) FROM users") == 0) {
            update("INSERT INTO users(username, password, full_name, role) VALUES (?,?,?,?)",
                    "admin", "admin123", "System Administrator", "Administrator");
            update("INSERT INTO users(username, password, full_name, role) VALUES (?,?,?,?)",
                    "staff", "staff123", "Event Coordinator", "Coordinator");
        }
        if (count("SELECT COUNT(*) FROM venues") + count("SELECT COUNT(*) FROM events")
                + count("SELECT COUNT(*) FROM students") == 0) {
            seedSampleData();
        }
    }

    // ---------- Sample data ----------
    private static void seedSampleData() throws SQLException {
        String[][] venues = {
            {"Main Auditorium", "Block A, Ground Floor", "500", "Projector, Sound System, AC"},
            {"Seminar Hall", "Block B, 1st Floor", "150", "Projector, Mic, AC"},
            {"Computer Lab 1", "IT Block, 2nd Floor", "60", "60 PCs, Wi-Fi, AC"},
            {"College Ground", "Behind Main Building", "1000", "Floodlights, Pavilion"},
            {"Open Air Theatre", "Campus Center", "400", "Stage, Lighting, Sound System"},
            {"Electronics Lab", "Block C, 1st Floor", "45", "Workbenches, Oscilloscopes"},
            {"Exhibition Hall", "Block A, 1st Floor", "200", "Display Stands, Lighting"}
        };
        for (String[] v : venues) {
            update("INSERT INTO venues(name, location, capacity, facilities) VALUES (?,?,?,?)",
                    v[0], v[1], Integer.parseInt(v[2]), v[3]);
        }

        String[][] students = {
            {"CS101", "Aarav Patel", "Computer Science", "2nd Year", "aarav.patel@college.edu", "9876500001"},
            {"CS102", "Diya Shah", "Computer Science", "3rd Year", "diya.shah@college.edu", "9876500002"},
            {"CS103", "Rohan Mehta", "Computer Science", "1st Year", "rohan.mehta@college.edu", "9876500003"},
            {"CS104", "Vanya Kapadia", "Computer Science", "4th Year", "vanya.kapadia@college.edu", "9876500004"},
            {"IT201", "Isha Joshi", "Information Technology", "2nd Year", "isha.joshi@college.edu", "9876500005"},
            {"IT202", "Kabir Desai", "Information Technology", "3rd Year", "kabir.desai@college.edu", "9876500006"},
            {"IT203", "Yash Bhatt", "Information Technology", "1st Year", "yash.bhatt@college.edu", "9876500007"},
            {"EC301", "Meera Trivedi", "Electronics", "4th Year", "meera.trivedi@college.edu", "9876500008"},
            {"EC302", "Neel Parmar", "Electronics", "2nd Year", "neel.parmar@college.edu", "9876500009"},
            {"EC303", "Hetal Rana", "Electronics", "3rd Year", "hetal.rana@college.edu", "9876500010"},
            {"ME401", "Om Solanki", "Mechanical", "3rd Year", "om.solanki@college.edu", "9876500011"},
            {"ME402", "Pooja Rathod", "Mechanical", "1st Year", "pooja.rathod@college.edu", "9876500012"},
            {"ME403", "Jay Thakkar", "Mechanical", "2nd Year", "jay.thakkar@college.edu", "9876500013"},
            {"CE501", "Riya Vora", "Civil", "2nd Year", "riya.vora@college.edu", "9876500014"},
            {"CE502", "Samir Dave", "Civil", "4th Year", "samir.dave@college.edu", "9876500015"},
            {"BA601", "Tanvi Gohil", "Business Administration", "1st Year", "tanvi.gohil@college.edu", "9876500016"},
            {"BA602", "Uday Chauhan", "Business Administration", "2nd Year", "uday.chauhan@college.edu", "9876500017"},
            {"BA603", "Khushi Modi", "Business Administration", "3rd Year", "khushi.modi@college.edu", "9876500018"},
            {"CS105", "Dev Sheth", "Computer Science", "2nd Year", "dev.sheth@college.edu", "9876500019"},
            {"IT204", "Anjali Pandya", "Information Technology", "4th Year", "anjali.pandya@college.edu", "9876500020"}
        };
        for (String[] s : students) {
            update("INSERT INTO students(roll_no, name, department, year, email, phone) VALUES (?,?,?,?,?,?)",
                    s[0], s[1], s[2], s[3], s[4], s[5]);
        }

        // name, category, day offset from today, start, end, venue, organizer, description, max participants
        Object[][] events = {
            {"Annual Tech Fest", "Technical", -45, "10:00", "17:00", "Main Auditorium", "Dr. S. Mehta", "Technical festival with competitions and demos.", 200},
            {"Cultural Night", "Cultural", -30, "18:00", "21:30", "Open Air Theatre", "Prof. R. Nair", "Music, dance and drama performances.", 300},
            {"Inter-College Cricket", "Sports", -21, "08:00", "16:00", "College Ground", "Mr. K. Singh", "Knockout cricket tournament.", 80},
            {"Robotics Workshop", "Workshop", -12, "11:00", "15:00", "Electronics Lab", "Dr. A. Kulkarni", "Hands-on Arduino and line-follower robot workshop.", 40},
            {"Blood Donation Camp", "Social", -5, "09:30", "14:00", "Seminar Hall", "Ms. P. Iyer", "Blood donation drive with a local hospital.", 100},
            {"Debate Competition", "Literary", 0, "14:00", "17:00", "Seminar Hall", "Prof. M. Rao", "Inter-department debate on current affairs.", 50},
            {"Hackathon", "Technical", 4, "09:00", "18:00", "Computer Lab 1", "Dr. V. Sharma", "Team coding hackathon with prizes.", 60},
            {"Science Exhibition", "Academic", 8, "10:00", "16:00", "Exhibition Hall", "Dr. N. Pillai", "Student science and engineering project exhibition.", 150},
            {"Photography Contest", "Cultural", 12, "12:00", "16:00", "Exhibition Hall", "Ms. T. Bose", "Theme-based photography contest.", 70},
            {"Placement Preparation Talk", "Career", 15, "15:00", "17:00", "Seminar Hall", "Mr. D. Kapoor", "Guest lecture on interviews and aptitude tests.", 120},
            {"Quiz Championship", "Literary", 18, "11:00", "14:00", "Seminar Hall", "Prof. L. Menon", "General knowledge and technical quiz.", 48},
            {"Football League", "Sports", 22, "16:00", "19:00", "College Ground", "Mr. K. Singh", "Inter-department football league.", 90},
            {"Entrepreneurship Summit", "Career", 27, "10:00", "16:00", "Main Auditorium", "Dr. S. Mehta", "Startup pitches and talks by local founders.", 180},
            {"Rangoli and Art Competition", "Cultural", 31, "10:30", "13:30", "Exhibition Hall", "Ms. T. Bose", "Rangoli and live painting competition.", 60},
            {"Farewell Party", "Social", 40, "17:00", "21:00", "Open Air Theatre", "Prof. R. Nair", "Farewell for the final year students.", 250}
        };
        LocalDate today = LocalDate.now();
        for (Object[] e : events) {
            update("INSERT INTO events(name, category, event_date, start_time, end_time, venue_id, organizer, description, max_participants) " +
                            "VALUES (?,?,?,?,?,(SELECT id FROM venues WHERE name=?),?,?,?)",
                    e[0], e[1], today.plusDays((Integer) e[2]).toString(), e[3], e[4], e[5], e[6], e[7], e[8]);
        }

        // Registrations: random students per event; past events get attendance and ratings
        Random rnd = new Random(7);
        String todayStr = today.toString();
        String[] feedbackPool = {
            "Well organised event", "Great experience, learned a lot", "Could have started on time",
            "Loved the activities", "Excellent coordination by the team"
        };
        List<Object[]> evs = query("SELECT id, event_date, max_participants FROM events ORDER BY id");
        List<Object[]> sts = query("SELECT id FROM students ORDER BY id");
        for (Object[] e : evs) {
            int eventId = num(e[0]);
            String date = str(e[1]);
            int max = num(e[2]);
            boolean past = date.compareTo(todayStr) < 0;
            int k = Math.min(Math.min(max, sts.size()), 7 + rnd.nextInt(10));
            List<Object[]> pool = new ArrayList<>(sts);
            Collections.shuffle(pool, rnd);
            for (int i = 0; i < k; i++) {
                int studentId = num(pool.get(i)[0]);
                String regDate = LocalDate.parse(date).minusDays(2 + rnd.nextInt(8)).toString();
                if (regDate.compareTo(todayStr) > 0) regDate = todayStr;
                String attendance = "Pending";
                int rating = 0;
                String feedback = "";
                if (past) {
                    if (rnd.nextInt(100) < 80) {
                        attendance = "Present";
                        rating = 3 + rnd.nextInt(3);
                        if (rnd.nextBoolean()) feedback = feedbackPool[rnd.nextInt(feedbackPool.length)];
                    } else {
                        attendance = "Absent";
                    }
                }
                update("INSERT INTO registrations(event_id, student_id, reg_date, attendance, rating, feedback) VALUES (?,?,?,?,?,?)",
                        eventId, studentId, regDate, attendance, rating, feedback);
            }
        }
        log("Database created with sample venues, students, events and registrations");
    }
}
