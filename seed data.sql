-- Seed data only (스키마 생성 후 실행)
USE walletslotdb;

-- 안전하게 초기화
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE `transaction`;
TRUNCATE TABLE `slot_history`;
TRUNCATE TABLE `account_slot`;
TRUNCATE TABLE `ai_report`;
TRUNCATE TABLE `notification`;
TRUNCATE TABLE `push_endpoint`;
TRUNCATE TABLE `user_pin`;
TRUNCATE TABLE `user_consent`;
TRUNCATE TABLE `wishlist`;
TRUNCATE TABLE `email`;
TRUNCATE TABLE `account`;
TRUNCATE TABLE `bank`;
TRUNCATE TABLE `slot`;
TRUNCATE TABLE `pepper_keys`;
TRUNCATE TABLE `consent_form`;
TRUNCATE TABLE `user`;
SET FOREIGN_KEY_CHECKS = 1;

-- USER
INSERT INTO `user` (id, uuid, name, user_key, phone_number, gender, birth_date, base_day, job)
VALUES
(1, UUID(), '전해지', 'd022c753-e3f0-4d58-a405-ee8a058fd199', '01012345678', 'FEMALE', '2000-02-24 00:00:00', 27, 'STUDENT');

-- PEPPER_KEYS
INSERT INTO `pepper_keys` (id, key_alias, status)
VALUES
(1, 'pepper_v0', 'RETIRED'),
(2, 'pepper_v1', 'ACTIVATE');

-- USER_PIN
INSERT INTO `user_pin` (id, user_id, pepper_id, bcrypted_pin, cost)
VALUES
(1, 1, 2, '$2a$10$A3RKpRl.p/c0EmIxjDGKBeT5BlLb1/0.b4IPM07oPz2Eu5BtXxjgG', 12);

-- BANK
INSERT INTO `bank` (id, uuid, name, code, color)
VALUES
(1, UUID(), '한국은행', '001', NULL),
(2, UUID(), '산업은행', '002', NULL),
(3, UUID(), '기업은행', '003', NULL),
(4, UUID(), '국민은행', '004', NULL),
(5, UUID(), '농협은행', '011', NULL),
(6, UUID(), '우리은행', '020', NULL),
(7, UUID(), 'SC제일은행', '023', NULL),
(8, UUID(), '시티은행', '027', NULL),
(9, UUID(), '대구은행', '032', NULL),
(10, UUID(), '광주은행', '034', NULL),
(11, UUID(), '제주은행', '035', NULL),
(12, UUID(), '전북은행', '037', NULL),
(13, UUID(), '경남은행', '039', NULL),
(14, UUID(), '새마을금고', '045', NULL),
(15, UUID(), 'KEB하나은행', '081', NULL),
(16, UUID(), '신한은행', '088', NULL),
(17, UUID(), '카카오뱅크', '090', NULL),
(18, UUID(), '싸피은행', '999', NULL);

-- ACCOUNT
INSERT INTO `account` (id, uuid, user_id, bank_id, alias, encrypted_account_no, balance, is_primary, last_synced_transaction_unique_no)
VALUES
(1, UUID(), 1, 3, NULL, 'XBaVgD2G8YWC6otR70CIB+QEUyihPrjpOEKmwzPhgco=', 5000000, TRUE, 0),
(2, UUID(), 1, 4, '비상금통장', '+XuHwQ48eiy4J3rSoCToieQEUyihPrjpOEKmwzPhgco=', 2000000, FALSE, 0),
(3, UUID(), 1, 5, NULL, 'iNmlCNGeZOAHc7k6ar6PFOQEUyihPrjpOEKmwzPhgco=', 1000000, TRUE, 0),
(4, UUID(), 1, 5, NULL, '3zkMX7fMlQXCAsR/mEnkR+QEUyihPrjpOEKmwzPhgco=', 1000000, TRUE, 0),
(5, UUID(), 1, 5, NULL, 'FqNF3m7Kc6hNMZ5c+22vkuQEUyihPrjpOEKmwzPhgco=', 1000000, TRUE, 0),
(6, UUID(), 1, 5, NULL, 'tSlPBJlPzR182rcfPeGQleQEUyihPrjpOEKmwzPhgco=', 1000000, TRUE, 0),
(7, UUID(), 1, 5, NULL, 'E6QSXu7oSvcgX3wr8U8wKuQEUyihPrjpOEKmwzPhgco=', 1000000, TRUE, 0);

-- 0을 명시적으로 넣기 위해 필요 (미분류 슬롯 id=0)
SET @OLD_SQL_MODE := @@sql_mode;
SET sql_mode = CONCAT(@@sql_mode, IF(@@sql_mode='', '', ','), 'NO_AUTO_VALUE_ON_ZERO');

-- SLOT
INSERT INTO `slot` (id, uuid, name, is_saving, `rank`)
VALUES
(0, UUID(), '미분류', FALSE, NULL),
(1, UUID(), '식비', FALSE, NULL),
(2, UUID(), '교통비', FALSE, NULL),
(3, UUID(), '의류/잡화', FALSE, NULL),
(4, UUID(), '카페/간식', FALSE, NULL),
(5, UUID(), '여가비', FALSE, NULL),
(6, UUID(), '의료/건강', FALSE, NULL),
(7, UUID(), '저축', TRUE, NULL),
(8, UUID(), '자동차비', FALSE, NULL),
(9, UUID(), '미용', FALSE, NULL),
(10, UUID(), '취미', FALSE, NULL),
(11, UUID(), '보험비', FALSE, NULL),
(12, UUID(), '통신비', FALSE, NULL),
(13, UUID(), '주거비', FALSE, NULL),
(14, UUID(), '구독비', FALSE, NULL),
(15, UUID(), '육아비', FALSE, NULL),
(16, UUID(), '용돈/선물', FALSE, NULL),
(17, UUID(), '반려동물', FALSE, NULL),
(18, UUID(), '데이트', FALSE, NULL),
(19, UUID(), '세금', FALSE, NULL),
(20, UUID(), '교육비', FALSE, NULL),
(21, UUID(), '경조사', FALSE, NULL),
(22, UUID(), '회비', FALSE, NULL),
(23, UUID(), '후원', FALSE, NULL),
(24, UUID(), '여행/숙박', FALSE, NULL);

-- ACCOUNT_SLOT
INSERT INTO `account_slot` (id, uuid, account_id, slot_id, initial_budget, current_budget, spent, budget_change_count, is_budget_exceeded, is_custom, custom_name)
VALUES
(1, UUID(), 1, 1, 100000, 150000, 4500, 1, FALSE, FALSE, NULL),
(2, UUID(), 1, 2, 300000, 300000, 300000, 0, FALSE, FALSE, NULL),
(3, UUID(), 1, 0, 30000, 30000, 0, 0, FALSE, FALSE, NULL),
(4, UUID(), 2, 1, 100000, 100000, 0, 0, FALSE, FALSE, NULL),
(5, UUID(), 2, 2, 50000, 100000, 0, 1, FALSE, FALSE, NULL),
(6, UUID(), 2, 0, 30000, 30000, 0, 0, FALSE, FALSE, NULL),
(7, UUID(), 3, 1, 100000, 100000, 0, 0, FALSE, FALSE, NULL),
(8, UUID(), 3, 0, 50000, 50000, 0, 0, FALSE, FALSE, NULL),
(9, UUID(), 4, 0, 30000, 60000, 0, 1, FALSE, FALSE, NULL),
(10, UUID(), 4, 4,  120000, 120000, 0, 0, FALSE, FALSE, NULL),  -- 카페/간식
(11, UUID(), 5, 14,  15000,  15000,  0, 0, FALSE, FALSE, NULL), -- 구독비
(12, UUID(), 5, 0,       50200,      0,  0, 0, FALSE, FALSE, NULL), -- 미분류(테스트용)
(13, UUID(), 6, 5,       2200,      0,  0, 0, FALSE, FALSE, NULL), 
(14, UUID(), 6, 9,       11131,      0,  0, 0, FALSE, FALSE, NULL), 
(15, UUID(), 6, 0,       335555,      0,  0, 0, FALSE, FALSE, NULL), 
(16, UUID(), 7, 1,       552125,      0,  0, 0, FALSE, FALSE, NULL), 
(17, UUID(), 7, 0,       52222,      0,  0, 0, FALSE, FALSE, NULL), 
(18, UUID(), 7, 2,       10000,      0,  0, 0, FALSE, FALSE, NULL),
(19, UUID(), 1, 4, 120000, 120000, 0, 0, FALSE, FALSE, NULL),
(20, UUID(), 1, 14, 15000, 15000, 0, 0, FALSE, FALSE, NULL);

-- SLOT_HISTORY
INSERT INTO `slot_history` (id, uuid, account_slot_id, old_budget, new_budget)
VALUES
(1, UUID(), 1, 100000, 150000),
(2, UUID(), 5, 50000, 100000),
(3, UUID(), 9, 30000, 60000),
(4, UUID(), 10, 100000, 120000);

-- TRANSACTION
INSERT INTO `transaction` (id, uuid, account_id, account_slot_id, unique_no, type, opponent_account_no, summary, amount, balance, transaction_at)
VALUES
(1, UUID(), 1, 1, 10001, '출금', NULL, '스타벅스 도안DT점', 4500, 4995500, '2025-09-10 12:30:00'),
(2, UUID(), 1, 2, 10002, '출금(이체)', 444433332222, '박형복', 300000, 4695500, '2025-09-11 08:10:00'),
(3,  UUID(), 1, 1, 80001, '출금',         NULL, '맥도날드 둔산점',     12000, 5318000, '2025-08-02 12:10:00'),
(4,  UUID(), 1, 1, 80002, '출금',         NULL, '스타벅스 둔산점',      5500,  5312500, '2025-08-05 09:05:00'),
(5,  UUID(), 1, 1, 80003, '출금',         NULL, '버거킹 용문점',        8900,  5303600, '2025-08-10 18:40:00'),
(6,  UUID(), 1, 1, 80004, '출금',         NULL, '회사 구내식당',        75000, 5228600, '2025-08-14 12:00:00'),
(7,  UUID(), 1, 1, 80005, '출금',         NULL, 'BBQ 도안점',          68600, 5160000, '2025-08-20 20:15:00'),

-- [교통비: 총 180,000 = 절약 120,000]
(8,  UUID(), 1, 2, 80006, '출금',         NULL, '교통카드 충전',        50000, 5110000, '2025-08-01 08:00:00'),
(9,  UUID(), 1, 2, 80007, '출금',         NULL, 'KTX 서울',            60000, 5050000, '2025-08-09 07:30:00'),
(10, UUID(), 1, 2, 80008, '출금',         NULL, '택시',                35000, 5015000, '2025-08-15 23:50:00'),
(11, UUID(), 1, 2, 80009, '출금',         NULL, '버스',                35000, 4980000, '2025-08-28 19:12:00'),

-- [의류/잡화: 총 20,000 = 절약 10,000]
(12, UUID(), 1, 3, 80010, '출금',         NULL, '유니클로 타임월드',    20000, 4960000, '2025-08-18 16:20:00'),

-- TRANSACTION 추가 (8월 카페/간식: 총 160,000 → 예산 120,000 초과 40,000)
(13, UUID(), 1, 19, 80011, '출금', NULL, '스타벅스 도안DT점', 45000, 4955000, '2025-08-03 10:15:00'),
(14, UUID(), 1, 19, 80012, '출금', NULL, '투썸플레이스 시청점', 38000, 4917000, '2025-08-07 16:40:00'),
(15, UUID(), 1, 19, 80013, '출금', NULL, '메가커피 둔산점',    27000, 4890000, '2025-08-12 14:05:00'),
(16, UUID(), 1, 19, 80014, '출금', NULL, '스타벅스 둔산점',   50000, 4840000, '2025-08-22 08:55:00'),

-- TRANSACTION 추가 (8월 구독비: 총 14,500 → 예산 15,000 이내)
(17, UUID(), 1, 20, 80015, '출금', NULL, '넷플릭스',    9500,  4830500, '2025-08-05 03:00:00'),
(18, UUID(), 1, 20, 80016, '출금', NULL, '유튜브 프리미엄', 5000,  4825500, '2025-08-18 03:00:00'),

-- TRANSACTION 추가 (8월 미분류: 총 23,900 → 분배/요약에서 제외되는 케이스)
(19, UUID(), 1, 3, 80017, '출금', NULL, '편의점 기타',  9900,  4815600, '2025-08-06 21:10:00'),
(20, UUID(), 1, 3, 80018, '출금', NULL, '기타 소액지출', 14000, 4801600, '2025-08-27 11:22:00'),

-- (옵션) 7월 데이터 몇 건 추가 – 월 필터링 잘 되는지 확인용
(21, UUID(), 1, 19, 70001, '출금', NULL, '스타벅스 도안DT점', 4200,  4797400, '2025-07-25 09:10:00'),
(22, UUID(), 1,  2, 70002, '출금', NULL, 'KTX 부산',        61000, 4736400, '2025-07-29 06:50:00');

-- CONSENT_FORM
INSERT INTO `consent_form` (id, uuid, title)
VALUES
(1, UUID(), '개인정보 수집 및 이용 동의'),
(2, UUID(), '서비스 이용 약관');

-- USER_CONSENT
INSERT INTO `user_consent` (id, uuid, user_id, consent_form_id, expired_at, status)
VALUES
(1, UUID(), 1, 1, '2026-09-17 00:20:01', 'ACTIVE');

-- PUSH_ENDPOINT
INSERT INTO `push_endpoint` (id, user_id, device_id, platform, token, status, is_push_enabled)
VALUES
(1, 1, 'device-1234', 'ANDROID', 'eHGzIgD5Sz--716JANZ5V4:APA91bFRTcdxU_jAVtOlm5PWiH45WK4422QAE551LQQeJFVm8mD8aTABSya3mXi3kt5iX7I_db7WKZ-Ymz82MSVVBEVWIQpxXAV67k5c-avbRiM8ZOPfjr0', 'ACTIVE', TRUE);

-- NOTIFICATION
-- 팀 규칙: DB에는 트랜잭션 id(tx_id) 저장, 응답 DTO에는 transactionUuid로 변환해 내려감
INSERT INTO `notification`
(id, uuid, user_id, title, body, is_delivered, delivered_at, is_read, read_at, type, tx_id)
VALUES
-- 일반 BUDGET 알림(트랜잭션 연계 없음 → tx_id = NULL)
(1, UUID(), 1, '예산 초과 알림', '식비 예산을 초과했습니다.', TRUE, '2025-09-20 15:00:03', FALSE, NULL, 'BUDGET', NULL),

-- 미분류 알림 2건: 트랜잭션 id 19/20을 참조 (account_slot_id=3 → 슬롯=미분류)
(2, UUID(), 1, '분류되지 않은 지출이 있어요', '편의점 기타 9,900원 • 2025-08-06 21:10:00\n카테고리를 지정해 주세요.', FALSE, NULL, FALSE, NULL, 'UNCATEGORIZED', 19),
(3, UUID(), 1, '분류되지 않은 지출이 있어요', '기타 소액지출 14,000원 • 2025-08-27 11:22:00\n카테고리를 지정해 주세요.', FALSE, NULL, FALSE, NULL, 'UNCATEGORIZED', 20);

-- WISHLIST
INSERT INTO `wishlist` (id, uuid, user_id, name, price, image)
VALUES
(1, UUID(), 1, '아이패드', 1200000, NULL);

-- EMAIL
INSERT INTO `email` (id, user_id, name, email, is_primary, verified_at, created_at)
VALUES
(1, 1, '전해지', 'wjsgowl0224@naver.com', 0, '2024-07-10 10:00:00', '2024-07-10 10:00:00');

-- AI_REPORT (옵션)
-- INSERT INTO `ai_report` (id, uuid, account_id, content)
-- VALUES
-- (1, UUID(), 1, JSON_OBJECT('summary', '이번달 식비 과다', 'advice', '다음달 식비 예산 상향 또는 지출 절감'));

-- 확인용
SELECT * FROM `user`;
SELECT * FROM `pepper_keys`;
SELECT * FROM `user_pin`;
SELECT * FROM `bank`;
SELECT * FROM `account`;
SELECT * FROM `slot`;
SELECT * FROM `account_slot`;
SELECT * FROM `slot_history`;
SELECT * FROM `transaction`;
SELECT * FROM `consent_form`;
SELECT * FROM `user_consent`;
SELECT * FROM `push_endpoint`;
SELECT * FROM `notification`;
SELECT * FROM `wishlist`;
SELECT * FROM `email`;
SELECT * FROM `ai_report`;

-- (참고) 알림-트랜잭션 매핑 확인용 조인
-- SELECT n.id, n.type, n.tx_id, t.uuid AS transaction_uuid, t.summary, t.transaction_at
-- FROM notification n LEFT JOIN `transaction` t ON t.id = n.tx_id
-- ORDER BY n.id;