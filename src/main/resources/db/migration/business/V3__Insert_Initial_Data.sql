-- Работаем со схемой business
SET search_path TO business;

-- Создаем первый счет для нашего админа
INSERT INTO accounts (id, owner_user_id, account_type, account_name, balance, status, version)
    -- ВАЖНО: owner_user_id пока не можем вставить, так как user еще не создан.
-- Делаем это в два шага.
VALUES (1, 1, 'PERSONAL_CHECKING', 'Admin Main Account', 1000.00, 'ACTIVE', 0)
ON CONFLICT (id) DO NOTHING;


-- Создаем нашего админа
-- Он будет и User, и Employee
INSERT INTO users (id, user_type, username, password, date_of_birth, version)
VALUES (1, 'EMPLOYEE', 'admin', '$2a$12$K4j1s2T5nL8B9sE2gH0oFu3y4c7V1jI6.w5.e9X8kY/D0bZ.d7z.W', '1990-01-01', 0)
ON CONFLICT (id) DO NOTHING;

-- Теперь связываем счет и юзера (в реальной жизни это делается одной транзакцией в приложении)
UPDATE accounts SET owner_user_id = 1 WHERE id = 1;


-- Добавляем данные для наследника Employee
INSERT INTO employees(id, employee_internal_id, position)
VALUES (1, 'E-000001', 'System Administrator')
ON CONFLICT(id) DO NOTHING;

-- Назначаем роли админу
INSERT INTO user_roles (user_id, role)
VALUES
    (1, 'ROLE_ADMIN'),
    (1, 'ROLE_CLIENT'), -- Админ тоже может быть клиентом
    (1, 'ROLE_TOP_MANAGEMENT')
ON CONFLICT (user_id, role) DO NOTHING;


-- Обновляем sequence, чтобы новые записи из приложения не конфликтовали
SELECT setval('business.accounts_id_seq', (SELECT MAX(id) FROM business.accounts), true);
SELECT setval('business.users_id_seq', (SELECT MAX(id) FROM business.users), true);