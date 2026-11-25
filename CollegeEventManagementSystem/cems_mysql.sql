-- College Event Management System MySQL Database Dump
CREATE DATABASE IF NOT EXISTS cems_db;
USE cems_db;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS registrations;
DROP TABLE IF EXISTS events;
DROP TABLE IF EXISTS venues;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS activity;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    role VARCHAR(100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE venues (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    location VARCHAR(255),
    capacity INT NOT NULL,
    facilities TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE students (
    id INT AUTO_INCREMENT PRIMARY KEY,
    roll_no VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    department VARCHAR(100),
    year VARCHAR(50),
    email VARCHAR(255),
    phone VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE events (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    event_date VARCHAR(50) NOT NULL,
    start_time VARCHAR(20) NOT NULL,
    end_time VARCHAR(20) NOT NULL,
    venue_id INT NOT NULL,
    organizer VARCHAR(255),
    description TEXT,
    max_participants INT NOT NULL,
    CONSTRAINT fk_event_venue FOREIGN KEY (venue_id) REFERENCES venues(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE registrations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    event_id INT NOT NULL,
    student_id INT NOT NULL,
    reg_date VARCHAR(50),
    attendance VARCHAR(50) DEFAULT 'Pending',
    rating INT DEFAULT 0,
    feedback TEXT,
    UNIQUE KEY uk_event_student (event_id, student_id),
    CONSTRAINT fk_reg_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    CONSTRAINT fk_reg_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE activity (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ts VARCHAR(50),
    username VARCHAR(100),
    action TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO users (id, username, password, full_name, role) VALUES (1, 'admin', 'admin123', 'System Administrator', 'Administrator');
INSERT INTO users (id, username, password, full_name, role) VALUES (2, 'staff', 'staff123', 'Event Coordinator', 'Coordinator');

INSERT INTO venues (id, name, location, capacity, facilities) VALUES (1, 'Main Auditorium', 'Block A, Ground Floor', 500, 'Projector, Sound System, AC');
INSERT INTO venues (id, name, location, capacity, facilities) VALUES (2, 'Seminar Hall', 'Block B, 1st Floor', 150, 'Projector, Mic, AC');
INSERT INTO venues (id, name, location, capacity, facilities) VALUES (3, 'Computer Lab 1', 'IT Block, 2nd Floor', 60, '60 PCs, Wi-Fi, AC');
INSERT INTO venues (id, name, location, capacity, facilities) VALUES (4, 'College Ground', 'Behind Main Building', 1000, 'Floodlights, Pavilion');
INSERT INTO venues (id, name, location, capacity, facilities) VALUES (5, 'Open Air Theatre', 'Campus Center', 400, 'Stage, Lighting, Sound System');
INSERT INTO venues (id, name, location, capacity, facilities) VALUES (6, 'Electronics Lab', 'Block C, 1st Floor', 45, 'Workbenches, Oscilloscopes');
INSERT INTO venues (id, name, location, capacity, facilities) VALUES (7, 'Exhibition Hall', 'Block A, 1st Floor', 200, 'Display Stands, Lighting');

INSERT INTO students (id, roll_no, name, department, year, email, phone) VALUES (1, 'CS101', 'Aarav Patel', 'Computer Science', '2nd Year', 'aarav.patel@college.edu', '9876500001');
INSERT INTO students (id, roll_no, name, department, year, email, phone) VALUES (2, 'CS102', 'Diya Shah', 'Computer Science', '3rd Year', 'diya.shah@college.edu', '9876500002');
INSERT INTO students (id, roll_no, name, department, year, email, phone) VALUES (3, 'CS103', 'Rohan Mehta', 'Computer Science', '1st Year', 'rohan.mehta@college.edu', '9876500003');
INSERT INTO students (id, roll_no, name, department, year, email, phone) VALUES (4, 'CS104', 'Vanya Kapadia', 'Computer Science', '4th Year', 'vanya.kapadia@college.edu', '9876500004');
INSERT INTO students (id, roll_no, name, department, year, email, phone) VALUES (5, 'IT201', 'Isha Joshi', 'Information Technology', '2nd Year', 'isha.joshi@college.edu', '9876500005');
INSERT INTO students (id, roll_no, name, department, year, email, phone) VALUES (6, 'IT202', 'Kabir Desai', 'Information Technology', '3rd Year', 'kabir.desai@college.edu', '9876500006');
INSERT INTO students (id, roll_no, name, department, year, email, phone) VALUES (7, 'IT203', 'Yash Bhatt', 'Information Technology', '1st Year', 'yash.bhatt@college.edu', '9876500007');
INSERT INTO students (id, roll_no, name, department, year, email, phone) VALUES (8, 'EC301', 'Meera Trivedi', 'Electronics', '4th Year', 'meera.trivedi@college.edu', '9876500008');
INSERT INTO students (id, roll_no, name, department, year, email, phone) VALUES (9, 'EC302', 'Neel Parmar', 'Electronics', '2nd Year', 'neel.parmar@college.edu', '9876500009');
INSERT INTO students (id, roll_no, name, department, year, email, phone) VALUES (10, 'EC303', 'Hetal Rana', 'Electronics', '3rd Year', 'hetal.rana@college.edu', '9876500010');
INSERT INTO students (id, roll_no, name, department, year, email, phone) VALUES (11, 'ME401', 'Om Solanki', 'Mechanical', '3rd Year', 'om.solanki@college.edu', '9876500011');
INSERT INTO students (id, roll_no, name, department, year, email, phone) VALUES (12, 'ME402', 'Pooja Rathod', 'Mechanical', '1st Year', 'pooja.rathod@college.edu', '9876500012');
INSERT INTO students (id, roll_no, name, department, year, email, phone) VALUES (13, 'ME403', 'Jay Thakkar', 'Mechanical', '2nd Year', 'jay.thakkar@college.edu', '9876500013');
INSERT INTO students (id, roll_no, name, department, year, email, phone) VALUES (14, 'CE501', 'Riya Vora', 'Civil', '2nd Year', 'riya.vora@college.edu', '9876500014');
INSERT INTO students (id, roll_no, name, department, year, email, phone) VALUES (15, 'CE502', 'Samir Dave', 'Civil', '4th Year', 'samir.dave@college.edu', '9876500015');
INSERT INTO students (id, roll_no, name, department, year, email, phone) VALUES (16, 'BA601', 'Tanvi Gohil', 'Business Administration', '1st Year', 'tanvi.gohil@college.edu', '9876500016');
INSERT INTO students (id, roll_no, name, department, year, email, phone) VALUES (17, 'BA602', 'Uday Chauhan', 'Business Administration', '2nd Year', 'uday.chauhan@college.edu', '9876500017');
INSERT INTO students (id, roll_no, name, department, year, email, phone) VALUES (18, 'BA603', 'Khushi Modi', 'Business Administration', '3rd Year', 'khushi.modi@college.edu', '9876500018');
INSERT INTO students (id, roll_no, name, department, year, email, phone) VALUES (19, 'CS105', 'Dev Sheth', 'Computer Science', '2nd Year', 'dev.sheth@college.edu', '9876500019');
INSERT INTO students (id, roll_no, name, department, year, email, phone) VALUES (20, 'IT204', 'Anjali Pandya', 'Information Technology', '4th Year', 'anjali.pandya@college.edu', '9876500020');
INSERT INTO students (id, roll_no, name, department, year, email, phone) VALUES (21, '92500527023', 'kartik pandya', 'Computer Science', '2nd Year', 'kartik@gmail.com', '9898447758');

INSERT INTO events (id, name, category, event_date, start_time, end_time, venue_id, organizer, description, max_participants) VALUES (1, 'Annual Tech Fest', 'Technical', '2026-09-02', '10:00', '17:00', 1, 'Dr. S. Mehta', 'Technical festival with competitions and demos.', 200);
INSERT INTO events (id, name, category, event_date, start_time, end_time, venue_id, organizer, description, max_participants) VALUES (2, 'Cultural Night', 'Cultural', '2026-09-17', '18:00', '21:30', 5, 'Prof. R. Nair', 'Music, dance and drama performances.', 300);
INSERT INTO events (id, name, category, event_date, start_time, end_time, venue_id, organizer, description, max_participants) VALUES (3, 'Inter-College Cricket', 'Sports', '2026-09-26', '08:00', '16:00', 4, 'Mr. K. Singh', 'Knockout cricket tournament.', 80);
INSERT INTO events (id, name, category, event_date, start_time, end_time, venue_id, organizer, description, max_participants) VALUES (4, 'Robotics Workshop', 'Workshop', '2026-10-05', '11:00', '15:00', 6, 'Dr. A. Kulkarni', 'Hands-on Arduino and line-follower robot workshop.', 40);
INSERT INTO events (id, name, category, event_date, start_time, end_time, venue_id, organizer, description, max_participants) VALUES (5, 'Blood Donation Camp', 'Social', '2026-10-12', '09:30', '14:00', 2, 'Ms. P. Iyer', 'Blood donation drive with a local hospital.', 100);
INSERT INTO events (id, name, category, event_date, start_time, end_time, venue_id, organizer, description, max_participants) VALUES (6, 'Debate Competition', 'Literary', '2026-10-17', '14:00', '17:00', 2, 'Prof. M. Rao', 'Inter-department debate on current affairs.', 50);
INSERT INTO events (id, name, category, event_date, start_time, end_time, venue_id, organizer, description, max_participants) VALUES (7, 'Hackathon', 'Technical', '2026-10-21', '09:00', '18:00', 3, 'Dr. V. Sharma', 'Team coding hackathon with prizes.', 60);
INSERT INTO events (id, name, category, event_date, start_time, end_time, venue_id, organizer, description, max_participants) VALUES (8, 'Science Exhibition', 'Academic', '2026-10-25', '10:00', '16:00', 7, 'Dr. N. Pillai', 'Student science and engineering project exhibition.', 150);
INSERT INTO events (id, name, category, event_date, start_time, end_time, venue_id, organizer, description, max_participants) VALUES (9, 'Photography Contest', 'Cultural', '2026-10-29', '12:00', '16:00', 7, 'Ms. T. Bose', 'Theme-based photography contest.', 70);
INSERT INTO events (id, name, category, event_date, start_time, end_time, venue_id, organizer, description, max_participants) VALUES (10, 'Placement Preparation Talk', 'Career', '2026-11-01', '15:00', '17:00', 2, 'Mr. D. Kapoor', 'Guest lecture on interviews and aptitude tests.', 120);
INSERT INTO events (id, name, category, event_date, start_time, end_time, venue_id, organizer, description, max_participants) VALUES (11, 'Quiz Championship', 'Literary', '2026-11-04', '11:00', '14:00', 2, 'Prof. L. Menon', 'General knowledge and technical quiz.', 48);
INSERT INTO events (id, name, category, event_date, start_time, end_time, venue_id, organizer, description, max_participants) VALUES (12, 'Football League', 'Sports', '2026-11-08', '16:00', '19:00', 4, 'Mr. K. Singh', 'Inter-department football league.', 90);
INSERT INTO events (id, name, category, event_date, start_time, end_time, venue_id, organizer, description, max_participants) VALUES (13, 'Entrepreneurship Summit', 'Career', '2026-11-13', '10:00', '16:00', 1, 'Dr. S. Mehta', 'Startup pitches and talks by local founders.', 180);
INSERT INTO events (id, name, category, event_date, start_time, end_time, venue_id, organizer, description, max_participants) VALUES (14, 'Rangoli and Art Competition', 'Cultural', '2026-11-17', '10:30', '13:30', 7, 'Ms. T. Bose', 'Rangoli and live painting competition.', 60);
INSERT INTO events (id, name, category, event_date, start_time, end_time, venue_id, organizer, description, max_participants) VALUES (15, 'Farewell Party', 'Social', '2026-11-26', '17:00', '21:00', 5, 'Prof. R. Nair', 'Farewell for the final year students.', 250);
INSERT INTO events (id, name, category, event_date, start_time, end_time, venue_id, organizer, description, max_participants) VALUES (16, 'diwali celebration', 'Cultural', '2026-10-28', '10:00', '13:00', 5, 'mr.kartik pandya', 'ignite the firecrackers and do diwali diya decoration', 399);

INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (1, 1, 17, '2026-08-26', 'Present', 3, 'Loved the activities');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (2, 1, 9, '2026-08-26', 'Present', 4, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (3, 1, 1, '2026-08-27', 'Present', 3, 'Great experience, learned a lot');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (4, 1, 15, '2026-08-30', 'Present', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (5, 1, 19, '2026-08-29', 'Present', 3, 'Well organised event');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (6, 1, 12, '2026-08-24', 'Present', 3, 'Well organised event');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (7, 1, 11, '2026-08-31', 'Absent', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (8, 1, 10, '2026-08-27', 'Present', 3, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (9, 1, 4, '2026-08-28', 'Present', 4, 'Loved the activities');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (10, 1, 3, '2026-08-25', 'Present', 3, 'Well organised event');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (11, 1, 18, '2026-08-31', 'Present', 5, 'Well organised event');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (12, 1, 7, '2026-08-26', 'Present', 5, 'Could have started on time');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (13, 1, 13, '2026-08-25', 'Present', 5, 'Well organised event');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (14, 2, 10, '2026-09-08', 'Present', 4, 'Great experience, learned a lot');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (15, 2, 7, '2026-09-09', 'Present', 4, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (16, 2, 18, '2026-09-13', 'Present', 4, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (17, 2, 20, '2026-09-13', 'Present', 4, 'Loved the activities');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (18, 2, 19, '2026-09-12', 'Present', 4, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (19, 2, 15, '2026-09-15', 'Present', 3, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (20, 2, 1, '2026-09-09', 'Present', 3, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (21, 2, 3, '2026-09-10', 'Present', 5, 'Well organised event');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (22, 3, 20, '2026-09-21', 'Present', 4, 'Could have started on time');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (23, 3, 18, '2026-09-24', 'Absent', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (24, 3, 11, '2026-09-22', 'Absent', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (25, 3, 6, '2026-09-21', 'Present', 4, 'Great experience, learned a lot');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (26, 3, 3, '2026-09-20', 'Present', 5, 'Well organised event');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (27, 3, 10, '2026-09-21', 'Present', 4, 'Could have started on time');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (28, 3, 16, '2026-09-18', 'Present', 3, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (29, 3, 13, '2026-09-19', 'Present', 4, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (30, 4, 4, '2026-09-30', 'Present', 5, 'Excellent coordination by the team');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (31, 4, 14, '2026-09-27', 'Present', 4, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (32, 4, 20, '2026-09-29', 'Present', 4, 'Loved the activities');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (33, 4, 1, '2026-09-30', 'Present', 3, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (34, 4, 13, '2026-09-30', 'Present', 3, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (35, 4, 5, '2026-10-02', 'Present', 4, 'Could have started on time');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (36, 4, 18, '2026-09-29', 'Absent', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (37, 4, 7, '2026-10-02', 'Present', 3, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (38, 4, 8, '2026-10-01', 'Present', 3, 'Great experience, learned a lot');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (39, 4, 19, '2026-09-29', 'Present', 5, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (40, 4, 10, '2026-09-29', 'Present', 4, 'Could have started on time');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (41, 4, 6, '2026-10-01', 'Absent', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (42, 4, 11, '2026-09-27', 'Present', 4, 'Loved the activities');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (43, 4, 16, '2026-10-03', 'Present', 5, 'Excellent coordination by the team');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (44, 4, 3, '2026-10-03', 'Present', 3, 'Could have started on time');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (45, 5, 11, '2026-10-10', 'Present', 4, 'Well organised event');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (46, 5, 19, '2026-10-06', 'Absent', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (47, 5, 7, '2026-10-10', 'Absent', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (48, 5, 5, '2026-10-09', 'Absent', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (49, 5, 13, '2026-10-06', 'Present', 4, 'Loved the activities');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (50, 5, 8, '2026-10-03', 'Present', 5, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (51, 5, 18, '2026-10-03', 'Present', 5, 'Could have started on time');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (52, 5, 4, '2026-10-03', 'Present', 5, 'Loved the activities');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (53, 5, 15, '2026-10-03', 'Present', 4, 'Excellent coordination by the team');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (54, 5, 3, '2026-10-07', 'Present', 5, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (55, 5, 10, '2026-10-05', 'Present', 4, 'Could have started on time');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (56, 5, 20, '2026-10-03', 'Present', 3, 'Well organised event');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (58, 6, 12, '2026-10-10', 'Present', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (59, 6, 1, '2026-10-12', 'Present', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (60, 6, 2, '2026-10-14', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (61, 6, 17, '2026-10-14', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (62, 6, 4, '2026-10-14', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (63, 6, 5, '2026-10-11', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (64, 6, 8, '2026-10-08', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (65, 6, 13, '2026-10-14', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (66, 6, 19, '2026-10-12', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (67, 6, 18, '2026-10-13', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (68, 7, 20, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (69, 7, 6, '2026-10-14', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (70, 7, 14, '2026-10-16', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (71, 7, 19, '2026-10-14', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (72, 7, 4, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (73, 7, 12, '2026-10-15', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (74, 7, 10, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (75, 7, 11, '2026-10-12', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (76, 7, 5, '2026-10-14', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (77, 7, 2, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (78, 7, 1, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (79, 7, 15, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (80, 7, 8, '2026-10-16', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (81, 7, 13, '2026-10-14', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (82, 8, 1, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (83, 8, 18, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (84, 8, 15, '2026-10-16', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (85, 8, 3, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (86, 8, 11, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (87, 8, 6, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (88, 8, 5, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (89, 8, 20, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (90, 8, 13, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (91, 8, 17, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (92, 9, 18, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (93, 9, 8, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (94, 9, 19, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (95, 9, 6, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (96, 9, 11, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (97, 9, 7, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (98, 9, 9, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (99, 9, 15, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (100, 9, 13, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (101, 9, 16, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (102, 9, 10, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (103, 9, 12, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (104, 9, 20, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (105, 9, 1, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (106, 10, 8, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (107, 10, 18, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (108, 10, 5, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (109, 10, 11, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (110, 10, 13, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (111, 10, 15, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (112, 10, 19, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (113, 10, 2, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (114, 10, 7, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (115, 10, 1, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (116, 11, 10, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (117, 11, 13, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (118, 11, 8, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (119, 11, 15, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (120, 11, 14, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (121, 11, 20, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (122, 11, 17, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (123, 11, 11, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (124, 12, 1, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (125, 12, 16, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (126, 12, 15, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (127, 12, 10, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (128, 12, 6, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (129, 12, 19, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (130, 12, 9, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (131, 12, 3, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (132, 12, 11, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (133, 12, 8, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (134, 12, 20, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (135, 12, 13, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (136, 13, 20, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (137, 13, 18, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (138, 13, 5, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (139, 13, 11, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (140, 13, 8, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (141, 13, 6, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (142, 13, 7, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (143, 13, 19, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (144, 13, 12, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (145, 13, 10, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (146, 13, 13, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (147, 13, 2, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (148, 14, 5, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (149, 14, 12, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (150, 14, 14, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (151, 14, 13, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (152, 14, 15, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (153, 14, 2, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (154, 14, 9, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (155, 14, 16, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (156, 14, 6, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (157, 15, 7, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (158, 15, 18, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (159, 15, 20, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (160, 15, 16, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (161, 15, 4, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (162, 15, 11, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (163, 15, 6, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (164, 15, 9, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (165, 15, 8, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (166, 15, 12, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (167, 15, 13, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (168, 15, 14, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (169, 15, 2, '2026-10-17', 'Pending', 0, '');
INSERT INTO registrations (id, event_id, student_id, reg_date, attendance, rating, feedback) VALUES (170, 16, 21, '2026-10-21', 'Pending', 0, '');

INSERT INTO activity (id, ts, username, action) VALUES (1, '2026-10-17 18:55', 'system', 'Database created with sample venues, students, events and registrations');
INSERT INTO activity (id, ts, username, action) VALUES (2, '2026-10-17 18:59', 'staff', 'Marked Present: Aarav Patel at Debate Competition');
INSERT INTO activity (id, ts, username, action) VALUES (3, '2026-10-17 18:59', 'staff', 'Marked Present: Pooja Rathod at Debate Competition');
INSERT INTO activity (id, ts, username, action) VALUES (4, '2026-10-17 18:59', 'staff', 'Cancelled registration: Anjali Pandya from Debate Competition');
INSERT INTO activity (id, ts, username, action) VALUES (5, '2026-10-21 08:31', 'admin', 'Created event: diwali celebration');
INSERT INTO activity (id, ts, username, action) VALUES (6, '2026-10-21 08:34', 'staff', 'Added student: kartik pandya (92500527023)');
INSERT INTO activity (id, ts, username, action) VALUES (7, '2026-10-21 08:34', 'staff', 'Registered kartik pandya for diwali celebration');
INSERT INTO activity (id, ts, username, action) VALUES (8, '2026-10-27 10:08', 'admin', 'Marked Present: Samir Dave at Annual Tech Fest');

