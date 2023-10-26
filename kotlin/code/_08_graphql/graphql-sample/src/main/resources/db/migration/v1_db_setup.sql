create table games (
    id integer primary key,
    name text not null,
    genre text not null,
    platform text not null,
    publisher text not null,
    developer text not null,
    release_date date not null,
    rating number not null,
    description text not null
);
