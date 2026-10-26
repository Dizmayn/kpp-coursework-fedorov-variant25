-- Курсова робота, варіант 25: Інформаційна система погодження внутрішніх документів
-- Файл відтворює БД повністю; усі зміни структури вносити саме сюди.

DROP DATABASE IF EXISTS document_approval;
CREATE DATABASE document_approval CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE document_approval;

CREATE TABLE author (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name  VARCHAR(160) NOT NULL,
    department VARCHAR(160) NOT NULL,
    email      VARCHAR(120) NOT NULL
);

CREATE TABLE reviewer (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(160) NOT NULL,
    job_title VARCHAR(160) NOT NULL,
    email     VARCHAR(120) NOT NULL
);

CREATE TABLE document (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    author_id            BIGINT       NOT NULL,
    title                VARCHAR(255) NOT NULL,
    doc_type             VARCHAR(60)  NOT NULL,
    deadline_date        DATE         NOT NULL,
    criticality          INT          NOT NULL,
    dependent_processes  INT          NOT NULL DEFAULT 0,
    FOREIGN KEY (author_id) REFERENCES author(id),
    CHECK (criticality BETWEEN 0 AND 100),
    CHECK (dependent_processes >= 0)
);

CREATE TABLE approval_request (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_id    BIGINT        NOT NULL,
    reviewer_id    BIGINT        NOT NULL,
    state          VARCHAR(20)   NOT NULL DEFAULT 'DRAFT',
    review_comment VARCHAR(500)  NULL,
    f1             DECIMAL(5,2)  NOT NULL DEFAULT 0,
    f2             DECIMAL(5,2)  NOT NULL DEFAULT 0,
    f3             DECIMAL(5,2)  NOT NULL DEFAULT 0,
    integral_index DECIMAL(5,2)  NOT NULL DEFAULT 0,
    FOREIGN KEY (document_id) REFERENCES document(id),
    FOREIGN KEY (reviewer_id) REFERENCES reviewer(id),
    CHECK (state IN ('DRAFT', 'SUBMITTED', 'REVIEW', 'APPROVED')),
    CHECK (f1 BETWEEN 0 AND 100 AND f2 BETWEEN 0 AND 100 AND f3 BETWEEN 0 AND 100)
);

-- Початкові демонстраційні дані
INSERT INTO author (full_name, department, email) VALUES
('Коваленко Ірина Петрівна', 'Кафедра комп''ютерних та інформаційних технологій і систем', 'kovalenko@example.com'),
('Мельник Олег Васильович', 'Навчальний відділ', 'melnyk@example.com'),
('Шевченко Наталія Іванівна', 'Бухгалтерія', 'shevchenko@example.com');

INSERT INTO reviewer (full_name, job_title, email) VALUES
('Бондаренко Андрій Сергійович', 'Проректор з навчальної роботи', 'bondarenko@example.com'),
('Ткаченко Людмила Михайлівна', 'Начальник юридичного відділу', 'tkachenko@example.com'),
('Гончаренко Віктор Павлович', 'Головний бухгалтер', 'honcharenko@example.com');

INSERT INTO document (author_id, title, doc_type, deadline_date, criticality, dependent_processes) VALUES
(1, 'Наказ про затвердження графіка захисту курсових робіт', 'Наказ', '2026-11-02', 80, 6),
(2, 'Положення про організацію освітнього процесу', 'Положення', '2026-12-15', 90, 9),
(3, 'Кошторис витрат на навчальні лабораторії', 'Кошторис', '2026-10-30', 70, 4),
(1, 'Службова записка щодо оновлення розкладу занять', 'Службова записка', '2026-11-20', 40, 2);

-- F1..F3 та I (0..100) обчислено станом на 2026-10-20
INSERT INTO approval_request (document_id, reviewer_id, state, review_comment, f1, f2, f3, integral_index) VALUES
(1, 1, 'REVIEW', NULL, 56.67, 80.00, 60.00, 65.67),
(2, 2, 'SUBMITTED', NULL, 0.00, 90.00, 90.00, 54.00),
(3, 3, 'DRAFT', NULL, 66.67, 70.00, 40.00, 61.17),
(4, 1, 'APPROVED', NULL, 0.00, 40.00, 20.00, 19.00);
