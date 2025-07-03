-- V2__Insert_Initial_Business_Data.sql
SET search_path TO business;

-- Вставляем данные в таблицы
INSERT INTO accounts(id, balance, initial_balance) VALUES
                                                       (1, 10.0, 10.0), (2, 10.0, 10.0), (3, 10.0, 10.0)
-- ... и так далее для всех 7
ON CONFLICT (id) DO NOTHING;

INSERT INTO users(id, username, password, date_of_birth, account_id) VALUES
                                                                         (1, 'john_shepard', '$2a$12$...', '2006-06-06', 1),
                                                                         (2, 'vlastimil_peterjela', '$2a$12$...','2007-07-07', 2),
                                                                         (3, 'hacku_yuki', '$2a$12$..', '2008-06-06', 3)
-- ... и так далее для всех 7
ON CONFLICT (id) DO NOTHING;

-- Назначаем роли
INSERT INTO user_roles (user_id, role) VALUES
                                           (1, 'CLIENT'), (2, 'CLIENT'), (3, 'CLIENT'), (4, 'CLIENT'), (5, 'CLIENT'), (6, 'CLIENT'), (7, 'CLIENT'),
                                           (2, 'MANAGER'), -- даем второму юзеру роль менеджера
                                           (1, 'ADMIN') -- а первому - админа для тестов
ON CONFLICT (user_id, role) DO NOTHING;

-- ... INSERT'ы для email_data и phone_data ...
INSERT INTO email_data(...) VALUES (...) ON CONFLICT (id) DO NOTHING;
INSERT INTO phone_data(...) VALUES (...) ON CONFLICT (id) DO NOTHING;

-- Обновляем sequence'ы, чтобы избежать конфликтов при создании новых записей
SELECT setval('accounts_id_seq', (SELECT MAX(id) FROM accounts), true);
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users), true);
-- для user_roles нет sequence, так как нет SERIAL ключа
SELECT setval('email_data_id_seq', (SELECT MAX(id) FROM email_data), true);
SELECT setval('phone_data_id_seq', (SELECT MAX(id) FROM phone_data), true);