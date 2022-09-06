DROP TABLE IF EXISTS users, items, bookings, comments, requests;

CREATE TABLE IF NOT EXISTS users
(
    id    BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    name  VARCHAR(64)                            NOT NULL,
    email VARCHAR(64) UNIQUE                     NOT NULL,
    CONSTRAINT pk_user PRIMARY KEY (id)
    );

CREATE TABLE IF NOT EXISTS requests
(
    id        BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    description      VARCHAR(512)                            NOT NULL,
    requester_id    BIGINT                                   NOT NULL,
    created   TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_requests PRIMARY KEY (id),
    CONSTRAINT fk_requests_requester_id FOREIGN KEY (requester_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS items
(
    id          BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    name        VARCHAR(64)                             NOT NULL,
    description VARCHAR(64)                             NOT NULL,
    available   BOOLEAN                                 NOT NULL,
    owner_id    BIGINT                                  NOT NULL,
    request_id  BIGINT,
    CONSTRAINT pk_item PRIMARY KEY (id),
    CONSTRAINT fk_items_owner_id FOREIGN KEY (owner_id) REFERENCES users (id),
    CONSTRAINT fk_items_request_id FOREIGN KEY (request_id) REFERENCES requests (id)
    );

CREATE TABLE IF NOT EXISTS bookings
(
    id         BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    start_time TIMESTAMP                               NOT NULL,
    end_time   TIMESTAMP                               NOT NULL,
    item_id    BIGINT                                  NOT NULL,
    booker_id  BIGINT                                  NOT NULL,
    status     VARCHAR(64)                             NOT NULL,
    CONSTRAINT pk_booking PRIMARY KEY (id),
    CONSTRAINT fk_bookings_item_id FOREIGN KEY (item_id) REFERENCES items (id),
    CONSTRAINT fk_bookings_booker_id FOREIGN KEY (booker_id) REFERENCES users (id)
    );

CREATE TABLE IF NOT EXISTS comments
(
    id        BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    text      VARCHAR(512)                            NOT NULL,
    item_id   BIGINT                                  NOT NULL,
    author_id BIGINT                                  NOT NULL,
    created   TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_comments PRIMARY KEY (id),
    CONSTRAINT fk_comments_item_id FOREIGN KEY (item_id) REFERENCES items (id),
    CONSTRAINT fk_comments_author_id FOREIGN KEY (author_id) REFERENCES users (id)
    );

