
INSERT INTO business.roles (id, name) VALUES
      (1, 'CLIENT'),
      (2, 'ADMIN'),
      (3, 'TECH_SUPPORT'),
      (4, 'MANAGER'),
      (5, 'SECURITY_OFFICER'),
      (6, 'SECURITY_SUPERVISOR'),
      (7, 'SECURITY_TOP_SUPERVISOR'),
      (8, 'TOP_MANAGEMENT')
ON CONFLICT (id) DO NOTHING;

INSERT INTO business.accounts (id, balance, initial_balance, version) VALUES
      (1, 100.00, 100.00, 0),
      (2, 200.00, 200.00, 0),
      (3, 150.00, 150.00, 0),
      (4, 5000.00, 5000.00, 0),
      (5, 1000.00, 1000.00, 0),
      (6, 250.00, 250.00, 0),
      (7, 750.00, 750.00, 0)
ON CONFLICT (id) DO UPDATE SET balance = EXCLUDED.balance, initial_balance = EXCLUDED.initial_balance;

INSERT INTO business.users (id, username, password, date_of_birth, account_id) VALUES
       (1, 'john_shepard', '$2a$12$Kj6B.S9..8p9/a.o0Jd0XeVr20LqTzQ1f1G.0k0v8w1.5l.b1.8w.', '1983-04-11', 1),
       (2, 'liara_tsoni', '$2a$12$Kj6B.S9..8p9/a.o0Jd0XeVr20TzQ1f1G.0k0v8w1.5l.b1.8w.', '1982-10-22', 2),
       (3, 'garus_vakarian', '$2a$12$Kj6B.S9..8p9/a.o0Jd0XeVr20TzQ1f1G.0k0v8w1.5l.b1.8w.', '1980-02-01', 3),
       (4, 'sergey_galyonkin', '$2a$12$Kj6B.S9..8p9/a.o0Jd0XeVr20TzQ1f1G.0k0v8w1.5l.b1.8w.', '1985-05-20', 4),
       (5, 'wylsacom', '$2a$12$Kj6B.S9..8p9/a.o0Jd0XeVr20TzQ1f1G.0k0v8w1.5l.b1.8w.', '1987-01-26', 5),
       (6, 'admin_user', '$2a$12$Kj6B.S9..8p9/a.o0Jd0XeVr20TzQ1f1G.0k0v8w1.5l.b1.8w.', '1990-01-01', 6),
       (7, 'security_officer', '$2a$12$Kj6B.S9..8p9/a.o0Jd0XeVr20TzQ1f1G.0k0v8w1.5l.b1.8w.', '1990-01-01', 7)
ON CONFLICT (id) DO UPDATE SET username = EXCLUDED.username;

INSERT INTO business.user_roles (user_id, role_id) VALUES
       (1, 1), -- john_shepard - CLIENT
       (2, 1), -- liara_tsoni - CLIENT
       (3, 1), -- garus_vakarian - CLIENT
       (4, 8), -- sergey_galyonkin - TOP_MANAGEMENT
       (5, 4), -- wylsacom - MANAGER
       (6, 2), -- admin_user - ADMIN
       (7, 5)  -- security_officer - SECURITY_OFFICER
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO business.email_data (id, email, user_id) VALUES
         (1, 'shepard@normandy.com', 1),
         (2, 'tsoni@normandy.com', 2),
         (3, 'vakarian@normandy.com', 3),
         (4, 'galyonkin@epicgames.com', 4),
         (5, 'wylsa@wylsa.com', 5),
         (6, 'admin@mycorp.com', 6),
         (7, 'security@mycorp.com', 7)
ON CONFLICT (id) DO NOTHING;

INSERT INTO business.phone_data (id, phone, user_id) VALUES
         (1, '+79990000001', 1),
         (2, '+79990000002', 2),
         (3, '+79990000003', 3),
         (4, '+79990000004', 4),
         (5, '+79990000005', 5),
         (6, '+79990000006', 6),
         (7, '+79990000007', 7)
ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('business.accounts', 'id'), (SELECT MAX(id) FROM business.accounts), true);
SELECT setval(pg_get_serial_sequence('business.users', 'id'), (SELECT MAX(id) FROM business.users), true);
SELECT setval(pg_get_serial_sequence('business.user_roles', 'id'), (SELECT MAX(id) FROM business.user_roles), true);
SELECT setval(pg_get_serial_sequence('business.email_data', 'id'), (SELECT MAX(id) FROM business.email_data), true);
SELECT setval(pg_get_serial_sequence('business.phone_data', 'id'), (SELECT MAX(id) FROM business.phone_data), true);