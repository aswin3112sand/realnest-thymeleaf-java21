INSERT INTO users (name, email, password, role)
SELECT 'Admin', 'admin3112@gmail.com', '$2b$12$GleCkz75lyEoxvC1Pssm7O0G09Cfvf.n7jZGplhDBk1GPLH24G9xG', 'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin3112@gmail.com');
