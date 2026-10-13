-- =====================================================================
--  ハレの日ポスト - Oracle DDL (2026-10-12 기준)
--  순서: (1) 삭제 → (2) 시퀀스 → (3) 테이블 → (4) 인덱스 → (5) 초기 데이터
--  시간은 모두 일본 시간(JST). 서버/JVM 시간대를 Asia/Tokyo 로 맞출 것
-- =====================================================================

-- (1) 삭제 (처음 실행 시 에러는 무시) - 자식 테이블부터
DROP TABLE tbl_report PURGE;
DROP TABLE tbl_card   PURGE;
DROP TABLE tbl_post   PURGE;
DROP TABLE tbl_design PURGE;
DROP TABLE tbl_user PURGE;

DROP SEQUENCE seq_user;
DROP SEQUENCE seq_post;
DROP SEQUENCE seq_card;
DROP SEQUENCE seq_design;
DROP SEQUENCE seq_report;

-- (2) 시퀀스 (JPA: @SequenceGenerator(allocationSize = 1))
CREATE SEQUENCE seq_user START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_post   START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_card   START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_design START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE seq_report START WITH 1 INCREMENT BY 1 NOCACHE;

-- (3) 테이블

-- 회원 (관리자는 role = 'ROLE_ADMIN')
CREATE TABLE tbl_user (
    user_idx    NUMBER       PRIMARY KEY,
    userid      VARCHAR2(20)   NOT NULL UNIQUE,
    passwd      VARCHAR2(100)  NOT NULL,                       -- BCrypt
    nickname    VARCHAR2(30)   NOT NULL,
    email       VARCHAR2(100)  NOT NULL UNIQUE,
    birthday    DATE,
    role        VARCHAR2(20)   DEFAULT 'ROLE_USER' NOT NULL
                CHECK (role IN ('ROLE_USER', 'ROLE_ADMIN')),
    regdate     TIMESTAMP      DEFAULT SYSTIMESTAMP NOT NULL,
    last_login  TIMESTAMP
);

-- 카드 디자인 (관리자가 업로드)
CREATE TABLE tbl_design (
    design_idx   NUMBER         PRIMARY KEY,
    name         VARCHAR2(50)   NOT NULL,
    theme        VARCHAR2(20)   CHECK (theme IN ('SHOGATSU', 'BIRTHDAY', 'CHRISTMAS')),  -- NULL = 공통
    origin_name  VARCHAR2(255),
    save_name    VARCHAR2(255)  UNIQUE,
    sort_order   NUMBER         DEFAULT 0 NOT NULL,
    use_yn       CHAR(1)        DEFAULT 'Y' NOT NULL CHECK (use_yn IN ('Y', 'N')),
    regdate      TIMESTAMP      DEFAULT SYSTIMESTAMP NOT NULL
);

-- 포스트
CREATE TABLE tbl_post (
    post_idx    NUMBER         PRIMARY KEY,
    code        VARCHAR2(20)   NOT NULL UNIQUE,
    user_idx    NUMBER       NOT NULL,
    title       VARCHAR2(60)   NOT NULL,
    theme       VARCHAR2(20)   NOT NULL CHECK (theme IN ('SHOGATSU', 'BIRTHDAY', 'CHRISTMAS')),
    open_date   TIMESTAMP      NOT NULL,
    decos       VARCHAR2(100),                                 -- 장식 코드 CSV 예: 'KAGAMI,KADOMATSU' (없으면 NULL). 우체통 색은 빨강 고정이라 컬럼 없음
    name_tag    VARCHAR2(30),                                  -- 이름표 최대 8자
    regdate     TIMESTAMP      DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT fk_post_user FOREIGN KEY (user_idx)
        REFERENCES tbl_user(user_idx) ON DELETE CASCADE
);

-- 카드
CREATE TABLE tbl_card (
    card_idx     NUMBER          PRIMARY KEY,
    post_idx     NUMBER          NOT NULL,
    design_idx   NUMBER,
    sender_name  VARCHAR2(60)    NOT NULL,
    contents     VARCHAR2(1000)  NOT NULL,
    is_read      CHAR(1)         DEFAULT 'N' NOT NULL CHECK (is_read IN ('Y', 'N')),
    ip           VARCHAR2(50)    NOT NULL,
    regdate      TIMESTAMP       DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT fk_card_post   FOREIGN KEY (post_idx)
        REFERENCES tbl_post(post_idx) ON DELETE CASCADE,
    CONSTRAINT fk_card_design FOREIGN KEY (design_idx)
        REFERENCES tbl_design(design_idx) ON DELETE SET NULL
);

-- 신고
CREATE TABLE tbl_report (
    report_idx  NUMBER         PRIMARY KEY,
    card_idx    NUMBER         NOT NULL,
    reason      VARCHAR2(500)  NOT NULL,
    status      VARCHAR2(10)   DEFAULT 'WAIT' NOT NULL CHECK (status IN ('WAIT', 'DONE')),
    regdate     TIMESTAMP      DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT fk_report_card FOREIGN KEY (card_idx)
        REFERENCES tbl_card(card_idx) ON DELETE CASCADE
);

-- (4) 인덱스
CREATE INDEX idx_post_user   ON tbl_post(user_idx);
CREATE INDEX idx_card_post     ON tbl_card(post_idx, regdate);
CREATE INDEX idx_card_ip       ON tbl_card(ip, regdate);      -- 도배 검사
CREATE INDEX idx_report_status ON tbl_report(status);

-- (5) 초기 데이터
-- 관리자 계정: passwd 는 BCrypt 결과값으로 교체
--   System.out.println(new BCryptPasswordEncoder().encode("비밀번호"));
INSERT INTO tbl_user(user_idx, userid, passwd, nickname, email, role)
VALUES (seq_user.NEXTVAL, 'admin', '여기에_BCrypt_결과값', '管理者', 'admin@example.com', 'ROLE_ADMIN');

COMMIT;
