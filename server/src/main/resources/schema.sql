create table if not exists users
(
    id    bigserial primary key,
    name  varchar(255) not null,
    email varchar(512) not null unique
);

create index if not exists idx_users_email on users (email);

-- ======================================================================
-- ItemRequest -> requests
-- ======================================================================
create table if not exists requests
(
    id           bigserial primary key,
    description  text                        not null,
    requestor_id bigint                      not null references users (id),
    created      timestamp without time zone not null
);

create index if not exists idx_requests_requestor_id on requests (requestor_id);
create index if not exists idx_requests_created on requests (created desc);
create index if not exists idx_requests_requestor_created on requests (requestor_id, created desc);

create table if not exists items
(
    id          bigserial primary key,
    owner_id    bigint not null references users (id),
    request_id  bigint references requests (id),
    name        varchar(255),
    description text,
    use_count   bigint not null default 0,
    category    varchar(255),
    available   boolean
);

create index if not exists idx_items_owner_id on items (owner_id);
create index if not exists idx_items_request_id on items (request_id);
create index if not exists idx_items_available on items (available);
create index if not exists idx_items_category on items (category);

create table if not exists comments
(
    id        bigserial primary key,
    text      varchar(1000)               not null,
    item_id   bigint                      not null references items (id) on delete cascade,
    author_id bigint                      not null references users (id),
    created   timestamp without time zone not null
);

create index if not exists idx_comments_item_created on comments (item_id, created desc);
create index if not exists idx_comments_author_id on comments (author_id);

create table if not exists bookings
(
    booking_id    bigserial primary key,
    item_id       bigint                      not null references items (id),
    booker_id     bigint                      not null references users (id),
    start_booking timestamp without time zone not null,
    end_booking   timestamp without time zone not null,
    status        varchar(32)                 not null,

    constraint chk_booking_dates check (end_booking > start_booking)
);

create index if not exists idx_bookings_item_id on bookings (item_id);
create index if not exists idx_bookings_booker_id on bookings (booker_id);
create index if not exists idx_bookings_item_start on bookings (item_id, start_booking);
