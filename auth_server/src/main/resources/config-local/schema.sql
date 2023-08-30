
CREATE TABLE IF NOT EXISTS oauth2_registered_client (
        id varchar(100) NOT NULL,
        client_id varchar(100) NOT NULL,
        client_id_issued_at timestamp DEFAULT CURRENT_TIMESTAMP,
        client_secret varchar(200) DEFAULT NULL,
        client_secret_expires_at timestamp DEFAULT NULL,
        client_name varchar(200) NOT NULL,
        client_authentication_methods varchar(1000) NOT NULL,
        authorization_grant_types varchar(1000) NOT NULL,
        redirect_uris varchar(1000) DEFAULT NULL,
        post_logout_redirect_uris varchar(1000) DEFAULT NULL,
        scopes varchar(1000) NOT NULL,
        client_settings varchar(2000) NOT NULL,
        token_settings varchar(2000) NOT NULL,
        PRIMARY KEY (id)
    );

/**
 * referenced org/springframework/security/core/userdetails/jdbc/users.ddl
 */
CREATE TABLE IF NOT EXISTS USERS(
	ID INT UNSIGNED NOT NULL AUTO_INCREMENT,
	USERNAME VARCHAR(50) NOT NULL,
	PASSWORD VARCHAR(500) NOT NULL,
        ENABLED boolean default (1) NOT NULL,
        HOGE boolean default (1) NOT NULL,
        PRIMARY KEY (id),
        UNIQUE KEY `IDX_USERNAME` (`USERNAME`)
    );

CREATE TABLE IF NOT EXISTS USER_INFO(
 	USERS_ID INT UNSIGNED NOT NULL, -- USERS.ID
 	FIRST_NAME VARCHAR(50),
 	LAST_NAME VARCHAR(50),
        ADDRESS VARCHAR(50),
        ADDRESS2 VARCHAR(50),
        PRIMARY KEY (USERS_ID),
        FOREIGN KEY (USERS_ID) REFERENCES USERS(ID)
     );

CREATE TABLE IF NOT EXISTS AUTHORITIES(
	USERS_ID INT UNSIGNED NOT NULL, -- USERS.ID
	AUTHORITY VARCHAR(50),
        FOREIGN KEY (USERS_ID) REFERENCES USERS(ID),
        UNIQUE KEY `IDX_ID_AUTH` (`USERS_ID`, `AUTHORITY`)
    );

CREATE TABLE IF NOT EXISTS USER_ACCOUNT_LOCK(
	ID INT UNSIGNED NOT NULL AUTO_INCREMENT,
	USERNAME VARCHAR(50) NOT NULL,
 	FAILURE_COUNT INT UNSIGNED DEFAULT 1 NOT NULL,
        PRIMARY KEY (USERNAME)
    );

