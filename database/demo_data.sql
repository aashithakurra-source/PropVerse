-- Optional PropVerse demonstration rows. Safe to execute repeatedly; no data is deleted.
USE propverse_db;
START TRANSACTION;

INSERT INTO projects (project_name, location, client_name, start_date, end_date, budget, status, progress_percentage)
SELECT 'Riverside Community Center', 'Pune, Maharashtra', 'Riverside Residents Association', '2026-08-01', '2027-06-30', 18500000.00, 'ONGOING', 28
WHERE NOT EXISTS (SELECT 1 FROM projects WHERE project_name='Riverside Community Center' AND location='Pune, Maharashtra');
INSERT INTO projects (project_name, location, client_name, start_date, end_date, budget, status, progress_percentage)
SELECT 'Eastside School Renovation', 'Pune, Maharashtra', 'Eastside Public School', '2026-09-15', '2027-03-31', 7200000.00, 'PLANNED', 0
WHERE NOT EXISTS (SELECT 1 FROM projects WHERE project_name='Eastside School Renovation' AND location='Pune, Maharashtra');

INSERT INTO workers (full_name, role, phone, email, project_id, availability_status)
SELECT 'Aarav Patil', 'Site Supervisor', '+91 98765 41001', 'aarav.patil@example.com', p.project_id, 'BUSY'
FROM projects p WHERE p.project_name='Riverside Community Center' AND p.location='Pune, Maharashtra'
AND NOT EXISTS (SELECT 1 FROM workers w WHERE w.full_name='Aarav Patil' AND w.email='aarav.patil@example.com');
INSERT INTO workers (full_name, role, phone, email, project_id, availability_status)
SELECT 'Meera Joshi', 'Electrician', '+91 98765 41002', 'meera.joshi@example.com', p.project_id, 'AVAILABLE'
FROM projects p WHERE p.project_name='Riverside Community Center' AND p.location='Pune, Maharashtra'
AND NOT EXISTS (SELECT 1 FROM workers w WHERE w.full_name='Meera Joshi' AND w.email='meera.joshi@example.com');
INSERT INTO workers (full_name, role, phone, email, project_id, availability_status)
SELECT 'Kabir Shah', 'Mason', '+91 98765 41003', 'kabir.shah@example.com', p.project_id, 'AVAILABLE'
FROM projects p WHERE p.project_name='Eastside School Renovation' AND p.location='Pune, Maharashtra'
AND NOT EXISTS (SELECT 1 FROM workers w WHERE w.full_name='Kabir Shah' AND w.email='kabir.shah@example.com');

INSERT INTO tasks (task_name, project_id, assigned_worker_id, start_date, deadline, status, progress_percentage, description)
SELECT 'Foundation inspection', p.project_id, w.worker_id, '2026-10-01', '2026-10-30', 'IN_PROGRESS', 60, 'Inspect reinforcement and concrete curing before the next construction stage.'
FROM projects p JOIN workers w ON w.email='aarav.patil@example.com'
WHERE p.project_name='Riverside Community Center' AND p.location='Pune, Maharashtra'
AND NOT EXISTS (SELECT 1 FROM tasks t WHERE t.task_name='Foundation inspection' AND t.project_id=p.project_id);
INSERT INTO tasks (task_name, project_id, assigned_worker_id, start_date, deadline, status, progress_percentage, description)
SELECT 'Electrical layout marking', p.project_id, w.worker_id, '2026-11-01', '2026-11-15', 'PENDING', 0, 'Mark planned conduit and switch locations on the approved drawings.'
FROM projects p JOIN workers w ON w.email='meera.joshi@example.com'
WHERE p.project_name='Riverside Community Center' AND p.location='Pune, Maharashtra'
AND NOT EXISTS (SELECT 1 FROM tasks t WHERE t.task_name='Electrical layout marking' AND t.project_id=p.project_id);
INSERT INTO tasks (task_name, project_id, assigned_worker_id, start_date, deadline, status, progress_percentage, description)
SELECT 'Classroom wall repairs', p.project_id, w.worker_id, '2026-11-10', '2026-12-05', 'PENDING', 0, 'Repair and prepare interior classroom walls for painting.'
FROM projects p JOIN workers w ON w.email='kabir.shah@example.com'
WHERE p.project_name='Eastside School Renovation' AND p.location='Pune, Maharashtra'
AND NOT EXISTS (SELECT 1 FROM tasks t WHERE t.task_name='Classroom wall repairs' AND t.project_id=p.project_id);

INSERT INTO issues (project_id, issue_title, description, priority, status, reported_date, resolution_notes)
SELECT p.project_id, 'Rainwater pooling near excavation', 'Water accumulated near the north edge after heavy rain; inspect temporary drainage.', 'HIGH', 'OPEN', CURRENT_DATE, NULL
FROM projects p WHERE p.project_name='Riverside Community Center' AND p.location='Pune, Maharashtra'
AND NOT EXISTS (SELECT 1 FROM issues i WHERE i.issue_title='Rainwater pooling near excavation' AND i.project_id=p.project_id);
INSERT INTO issues (project_id, issue_title, description, priority, status, reported_date, resolution_notes)
SELECT p.project_id, 'Material delivery access timing', 'Coordinate delivery window with school administration to avoid arrival-time congestion.', 'MEDIUM', 'IN_PROGRESS', CURRENT_DATE, 'Proposed a delivery window outside student arrival and dismissal.'
FROM projects p WHERE p.project_name='Eastside School Renovation' AND p.location='Pune, Maharashtra'
AND NOT EXISTS (SELECT 1 FROM issues i WHERE i.issue_title='Material delivery access timing' AND i.project_id=p.project_id);

COMMIT;
