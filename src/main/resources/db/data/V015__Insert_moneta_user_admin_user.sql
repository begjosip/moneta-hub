INSERT INTO moneta_user(first_name, last_name, username, password, status)
VALUES ('Moneta', 'Admin', '7isToHelG0WSjbQuD0SRsdCj361Yr8fYdY/nWrBXI4A=', '$2a$10$HPMl9AY5hiaFbY9mMl4zVepaXPS3dStawxIxy/vs8oa9xkDBwiDmS',
        'ACTIVE');

-- Insert the user_id and role_id into user_role table
INSERT INTO user_role (user_id, role_id)
VALUES ((SELECT id FROM moneta_user WHERE username = '7isToHelG0WSjbQuD0SRsdCj361Yr8fYdY/nWrBXI4A='),
        (SELECT id FROM role WHERE name = 'ADMIN'));