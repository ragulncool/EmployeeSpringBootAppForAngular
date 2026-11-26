CREATE TABLE IF NOT EXISTS employee (
    emp_id INTEGER PRIMARY KEY,
    emp_name VARCHAR(200) NOT NULL,
    emp_dept VARCHAR(200) NOT NULL
);

CREATE TABLE IF NOT EXISTS app_user (
    username VARCHAR(64) PRIMARY KEY,
    password_hash VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS auth_token (
    token_hash CHAR(64) PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_auth_token_user FOREIGN KEY (username) REFERENCES app_user(username)
);
