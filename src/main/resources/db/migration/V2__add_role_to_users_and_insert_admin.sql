ALTER TABLE users
ADD COLUMN role varchar(20) NOT NULL DEFAULT 'USER';

INSERT INTO users (name, email, password, role)
VALUES ('System Admin', 'admin@email.com', '$2a$10$Subf3WBkYlDzB8FdAz3nNOLDuPVPTWWlZPoqWniv./3aHsYQEJiSm', 'ADMIN');