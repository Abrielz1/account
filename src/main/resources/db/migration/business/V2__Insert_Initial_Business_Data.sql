-- V2__Insert_Initial_Business_Data.sql

SET search_path TO business;

-- 1. Заполняем справочник ролей
INSERT INTO roles (id, name) VALUES
                                 (1, 'ROLE_CLIENT'),
                                 (2, 'ROLE_ADMIN'),
                                 (3, 'ROLE_TECH_SUPPORT'),
                                 (4, 'ROLE_MANAGER');
-- и т.д.

-- 2. Вставляем счета
INSERT INTO accounts(id, balance, initial_balance) VALUES
                                                       (1, 100.00, 100.00),
                                                       (2, 200.00, 200.00);
-- и т.д.

-- 3. Вставляем пользователей
INSERT INTO users(id, username, password, date_of_birth, account_id) VALUES
                                                                         (1, 'shepard.j', '$2a$12$q4fb3xtW/X.dSh2jSUr88exA52T05hTJzEyo/vb9gRkWqxUrQopH2', '2154-04-11', 1),
                                                                         (2, 'liara.t', '$2a$12$R.3Vq9P7M8jV9lT0L.Zq4u2o5y8eN6yK/X.z7B8q0o5P3g7a8S9rC', '2077-01-01', 2);
-- и т.д.

-- 4. Назначаем роли
INSERT INTO user_roles (user_id, role_id) VALUES
                                              (1, 1), -- Shepard - CLIENT
                                              (1, 2); -- Shepard - еще и ADMIN

-- 5. Добавляем контакты
INSERT INTO email_data (email, user_id) VALUES
                                            ('shepard@normandy.com', 1),
                                            ('liara@prothean.expert', 2);

INSERT INTO phone_data (phone, user_id) VALUES
    ('+79991112233', 1);

-- 6. Корректируем sequences в самом конце
SELECT setval('business.accounts_id_seq', (SELECT MAX(id) FROM business.accounts), true);
SELECT setval('business.users_id_seq', (SELECT MAX(id) FROM business.users), true);
SELECT setval('business.user_roles_id_seq', COALESCE((SELECT MAX(id) FROM business.user_roles), 1), false);
SELECT setval('business.email_data_id_seq', COALESCE((SELECT MAX(id) FROM business.email_data), 1), false);
SELECT setval('business.phone_data_id_seq', COALESCE((SELECT MAX(id) FROM business.phone_data), 1), false);