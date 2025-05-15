
INSERT INTO accounts(id, balance, initial_balance)
VALUES (1, 10.0, 10.0),
       (2, 10.0, 10.0),
       (3, 10.0, 10.0),
       (4, 10.0, 10.0),
       (5, 10.0, 10.0),
       (6, 10.0, 10.0),
       (7, 10.0, 10.0);

INSERT INTO users(id, username, password, account_id)
VALUES (1, 'john_shepard', '$2a$04$gHdOJO0XG6NdKZoDceWgoeu8FtP0IoOScxeaxS3zWOb1.hyPOw8gK', 1),
       (2, 'vlastimil_peterjela', '$2a$04$gHdOJO0XG6NdKZoDceWgoeu8FtP0IoOScxeaxS3zWOb1.hyPOw8gK', 2),
       (3, 'hacku_yuki', '$2a$04$gHdOJO0XG6NdKZoDceWgoeu8FtP0IoOScxeaxS3zWOb1.hyPOw8gK', 3),
       (4, 'tarja_turunen', '$2a$04$gHdOJO0XG6NdKZoDceWgoeu8FtP0IoOScxeaxS3zWOb1.hyPOw8gK', 4),
       (5, 'nathatan_drake', '$2a$04$gHdOJO0XG6NdKZoDceWgoeu8FtP0IoOScxeaxS3zWOb1.hyPOw8gK', 5),
       (6, 'nate_pinkerthon', '$2a$04$gHdOJO0XG6NdKZoDceWgoeu8FtP0IoOScxeaxS3zWOb1.hyPOw8gK', 6),
       (7, 'shion_uzuki', '$2a$04$gHdOJO0XG6NdKZoDceWgoeu8FtP0IoOScxeaxS3zWOb1.hyPOw8gK', 7);

INSERT INTO user_roles
    (user_id, roles)
VALUES (1, 'ROLE_USER'),
       (2, 'ROLE_USER'),
       (3, 'ROLE_USER'),
       (4, 'ROLE_USER'),
       (5, 'ROLE_USER'),
       (6, 'ROLE_USER'),
       (7, 'ROLE_USER');


INSERT INTO email_data(id, email, user_id)
VALUES (1, 'john_shepard@gmail.com', 1),
       (2, 'vlastimil@gmail.com', 2),
       (3, 'hacku_yuki@gmail.com', 3),
       (4, 'tarja@gmail.com', 4),
       (5, 'nathatan@gmail.com', 5),
       (6, 'nate@gmail.com', 6),
       (7, 'shion@gmail.com', 7);

INSERT INTO phone_data(id, phone, user_id)
VALUES (1, '+79525559801', 1),
       (2, '+79525559802', 2),
       (3, '+79525559803', 3),
       (4, '+79525559899', 4),
       (5, '+79525559888', 5),
       (6, '+79525559807', 6),
       (7, '+79525550001', 7);


SELECT setval(
               'users_id_seq',
               (SELECT MAX(id) FROM users)
           );

SELECT setval(
               'accounts_id_seq',
               (SELECT MAX(id) FROM accounts)
           );

SELECT setval(
               'email_data_id_seq',
               (SELECT MAX(id) FROM email_data)
           );

SELECT setval(
               'phone_data_id_seq',
               (SELECT MAX(id) FROM phone_data)
           );