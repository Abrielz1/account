
SET search_path TO business;

-- Заполняем справочник ролей
INSERT INTO roles (id, name) VALUES
     (1, 'CLIENT'),
     (2, 'ADMIN'),
     (3, 'TECH_SUPPORT'),
     (4, 'MANAGER'),
     (5, 'SECURITY_OFFICER'),
     (6, 'SECURITY_SUPERVISOR'),
     (7, 'SECURITY_TOP_SUPERVISOR'),
     (8, 'TOP_MANAGEMENT')
ON CONFLICT (id) DO NOTHING; -- Не падать, если роли уже существуют


-- Заполняем счета и пользователей (из твоего кода)
INSERT INTO accounts(id, balance, initial_balance) VALUES
                                                       (1, 10.0, 10.0), (2, 10.0, 10.0), (3, 10.0, 10.0), (4, 10.0, 10.0),
                                                       (5, 10.0, 10.0), (6, 10.0, 10.0), (7, 10.0, 10.0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO users(id, username, password, date_of_birth, account_id) VALUES
      (1, 'john_shepard', '$2a$12$q4fb3xtW/X.dSh2jSUr88exA52T05hTJzEyo/vb9gRkWqxUrQopH2', '2006-06-06', 1),
      (2, 'vlastimil_peterjela', '$2a$12$gHdOJO0XG6NdKZoDceWgoeu8FtP0IoOScxeaxS3zWOb1.hyPOw8gK','2007-07-07', 2),
      (3, 'hacku_yuki', '$2a$12$gHdOJO0XG6NdKZoDceWgoeu8FtP0IoOScxeaxS3zWOb1.hyPOw8gK', '2008-06-06', 3),
      (4, 'tarja_turunen', '$2a$12$gHdOJO0XG6NdKZoDceWgoeu8FtP0IoOScxeaxS3zWOb1.hyPOw8gK', '1970-07-01', 4),
      (5, 'nathatan_drake', '$2a$12$gHdOJO0XG6NdKZoDceWgoeu8FtP0IoOScxeaxS3zWOb1.hyPOw8gK', '1980-11-11', 5),
      (6, 'nate_pinkerthon', '$2a$12$gHdOJO0XG6NdKZoDceWgoeu8FtP0IoOScxeaxS3zWOb1.hyPOw8gK', '1920-12-01', 6),
      (7, 'shion_uzuki', '$2a$12$gHdOJO0XG6NdKZoDceWgoeu8FtP0IoOScxeaxS3zWOb1.hyPOw8gK', '1999-10-10', 7)
ON CONFLICT (id) DO NOTHING;


-- Назначаем роли
INSERT INTO user_roles (user_id, role_id) VALUES
      (1, 1), (2, 1), (3, 1), (4, 1), (5, 1), (6, 1), (7, 1),
      (2, 4) -- Дадим второму пользователю еще и роль менеджера
ON CONFLICT (user_id, role_id) DO NOTHING;

-- Заполняем контакты
INSERT INTO email_data(id, email, user_id) VALUES
      (1, 'john_shepard@gmail.com', 1), (2, 'vlastimil@gmail.com', 2), (3, 'hacku_yuki@gmail.com', 3)
ON CONFLICT (id) DO NOTHING;
-- и так далее...

INSERT INTO phone_data(id, phone, user_id) VALUES
      (1, '+79525559801', 1), (2, '+79525559802', 2), (3, '+79525559803', 3)
ON CONFLICT (id) DO NOTHING;
-- и так далее...

-- Обновляем sequence'ы, чтобы новые записи не конфликтовали
SELECT setval('accounts_id_seq', (SELECT MAX(id) FROM accounts), true);
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users), true);
SELECT setval('user_roles_id_seq', (SELECT MAX(id) FROM user_roles), true);
SELECT setval('email_data_id_seq', (SELECT MAX(id) FROM email_data), true);
SELECT setval('phone_data_id_seq', (SELECT MAX(id) FROM phone_data), true);