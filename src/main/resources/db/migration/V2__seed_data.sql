-- 5 Vehicles
INSERT INTO vehicles (id, name, plate_number) VALUES
(1, 'Vehicle Alpha',   'DXB-1001'),
(2, 'Vehicle Bravo',   'DXB-1002'),
(3, 'Vehicle Charlie',  'DXB-1003'),
(4, 'Vehicle Delta',   'DXB-1004'),
(5, 'Vehicle Echo',    'DXB-1005');

-- 25 Professionals (5 per vehicle)
INSERT INTO professionals (id, name, vehicle_id, version) VALUES
-- Vehicle Alpha
(1,  'Ali Hassan',       1, 0),
(2,  'Sara Ahmed',       1, 0),
(3,  'Omar Khalid',      1, 0),
(4,  'Fatima Noor',      1, 0),
(5,  'Yusuf Malik',      1, 0),
-- Vehicle Bravo
(6,  'Aisha Rahman',     2, 0),
(7,  'Khalid Saeed',     2, 0),
(8,  'Maryam Jaber',     2, 0),
(9,  'Hassan Raza',      2, 0),
(10, 'Nadia Farooq',     2, 0),
-- Vehicle Charlie
(11, 'Tariq Hussain',    3, 0),
(12, 'Layla Mustafa',    3, 0),
(13, 'Imran Qureshi',    3, 0),
(14, 'Zainab Iqbal',     3, 0),
(15, 'Bilal Sharif',     3, 0),
-- Vehicle Delta
(16, 'Amina Saleh',      4, 0),
(17, 'Rashid Mansoor',   4, 0),
(18, 'Huda Bashar',      4, 0),
(19, 'Faisal Nawaz',     4, 0),
(20, 'Samira Anwar',     4, 0),
-- Vehicle Echo
(21, 'Jamal Othman',     5, 0),
(22, 'Rania Haddad',     5, 0),
(23, 'Sami Bishara',     5, 0),
(24, 'Dina Khoury',      5, 0),
(25, 'Waleed Taha',      5, 0);
