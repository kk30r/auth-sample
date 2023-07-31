/**
 * oauth2_registered_client
 */
INSERT INTO oauth2_registered_client(
        id, client_id, client_secret, client_name, client_authentication_methods,
        authorization_grant_types,
        redirect_uris,
        post_logout_redirect_uris,
        scopes,
        client_settings,
        token_settings)
VALUES ('001', 'messaging-client', '{noop}secret', 'messaging-client', 'client_secret_post',
        'authorization_code,refresh_token,client_credentials',
        'http://127.0.0.1:8090/login/oauth2/code/messaging-client-oidc,http://127.0.0.1:8090/login/oauth2/code/auth-server-sample',
        'http://127.0.0.1:8090/',
        'openid,profile,message.read,message.write',
        '{"@class":"java.util.HashMap","settings.client.require-proof-key":false,"settings.client.require-authorization-consent":false}',
        '{"@class":"java.util.HashMap","settings.token.reuse-refresh-tokens":true,"settings.token.id-token-signature-algorithm":["org.springframework.security.oauth2.jose.jws.SignatureAlgorithm","RS256"],"settings.token.access-token-time-to-live":["java.time.Duration",300.000000000],"settings.token.access-token-format":{"@class":"org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat","value":"self-contained"},"settings.token.refresh-token-time-to-live":["java.time.Duration",3600.000000000],"settings.token.authorization-code-time-to-live":["java.time.Duration",300.000000000],"settings.token.device-code-time-to-live":["java.time.Duration",300.000000000]}');

/**
 * USERS
 */
INSERT INTO USERS(ID, USERNAME, PASSWORD) VALUES (1, 'user1', '{bcrypt}$2a$10$1.KSPVHICQS3HURyFQTSXuO9/zhhw181NV1sOu..PdJGoTXllmZtu');
INSERT INTO USERS(ID, USERNAME, PASSWORD) VALUES (2, 'user2', '{bcrypt}$2a$10$1.KSPVHICQS3HURyFQTSXuO9/zhhw181NV1sOu..PdJGoTXllmZtu');
INSERT INTO USERS(ID, USERNAME, PASSWORD) VALUES (3, 'user3', '{bcrypt}$2a$10$1.KSPVHICQS3HURyFQTSXuO9/zhhw181NV1sOu..PdJGoTXllmZtu');

/**
 * USER_INFO
 */
INSERT INTO USER_INFO(USERS_ID, FIRST_NAME, LAST_NAME, ADDRESS) VALUES (1, 'USER1_F', 'USER1_L', 'ADDRESS1');
INSERT INTO USER_INFO(USERS_ID, FIRST_NAME, LAST_NAME) VALUES (2, 'USER2_F', 'USER2_L');
INSERT INTO USER_INFO(USERS_ID, FIRST_NAME, ADDRESS) VALUES (3, 'USER3_F', 'ADDRESS2');

/**
 * USER_INFO
 */
INSERT INTO AUTHORITIES(USERS_ID, AUTHORITY) VALUES (1, 'ROLE_ADMIN');
INSERT INTO AUTHORITIES(USERS_ID, AUTHORITY) VALUES (1, 'ROLE_USER');
INSERT INTO AUTHORITIES(USERS_ID, AUTHORITY) VALUES (2, 'ROLE_USER');
INSERT INTO AUTHORITIES(USERS_ID, AUTHORITY) VALUES (3, 'ROLE_ADMIN');
