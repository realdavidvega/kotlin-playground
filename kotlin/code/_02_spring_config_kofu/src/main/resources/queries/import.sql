create table Person
(
    ID         INTEGER not null,
    FIRST_NAME VARCHAR not null,
    LAST_NAME  VARCHAR not null,
    BIRTHDATE  DATE
);
insert into Person(ID, FIRST_NAME, LAST_NAME, BIRTHDATE) values (1, 'David', 'Vega', '1995-03-24');
insert into Person(ID, FIRST_NAME, LAST_NAME, BIRTHDATE) values (2, 'John', 'Cena', '1970-01-01');
insert into Person(ID, FIRST_NAME, LAST_NAME) values (3, 'Will', 'Smith');