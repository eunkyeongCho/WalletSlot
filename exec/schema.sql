DROP DATABASE IF EXISTS walletslotdb;
CREATE DATABASE walletslotdb;
USE walletslotdb;

-- =========================
-- user
-- =========================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  `uuid` VARCHAR(64) NOT NULL,
  `name` VARCHAR(64) NOT NULL,
  `user_key` VARCHAR(255) NULL,
  `phone_number` VARCHAR(64) NOT NULL,
  `gender` ENUM('FEMALE', 'MAN') NOT NULL,
  `birth_date` DATETIME NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `base_day` TINYINT UNSIGNED COMMENT '기준일 입력받는 시점이 회원가입 이후인 것 같아서 NULLABLE',
  `job` ENUM('STUDENT', 'HOMEMAKER', 'OFFICE_WORKER', 'SOLDIER', 'SELF_EMPLOYED', 'FREELANCER', 'UNEMPLOYED', 'OTHER'),
  UNIQUE KEY `uq_user_uuid` (`uuid`)
) ENGINE=InnoDB;

-- =========================
-- pepper_keys / user_pin
-- =========================
DROP TABLE IF EXISTS `pepper_keys`;
CREATE TABLE `pepper_keys` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `key_alias` VARCHAR(64) UNIQUE NOT NULL COMMENT '키 본문은 KMS/HSM보관',
  `status` ENUM('ACTIVATE', 'RETIRED', 'REVOKED') NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `rotated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

DROP TABLE IF EXISTS `user_pin`;
CREATE TABLE `user_pin` (
  `id` INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT UNSIGNED NOT NULL,
  `pepper_id` INT UNSIGNED NOT NULL,
  `bcrypted_pin` VARCHAR(64) NOT NULL,
  `cost` TINYINT UNSIGNED NOT NULL COMMENT '$2b$12$...에서 12를 추출해 정수로 저장',
  `failed_count` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '5번 넘게 틀리면 잠김같은 기능',
  `locked_until` DATETIME COMMENT '비밀번호 자주 틀리면 잠금',
  `last_changed_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '비밀번호 변경안한지 얼마나 됐어요',
  `last_verified_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '휴면계정 확인',
  CONSTRAINT `fk_user_pin_user_id`
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
      ON DELETE CASCADE
      ON UPDATE CASCADE,
  CONSTRAINT `fk_user_pin_pepper_keys_id`
    FOREIGN KEY (`pepper_id`) REFERENCES `pepper_keys`(`id`)
      ON DELETE CASCADE
      ON UPDATE CASCADE
) ENGINE=InnoDB;

-- =========================
-- push_endpoint
-- =========================
DROP TABLE IF EXISTS `push_endpoint`;
CREATE TABLE `push_endpoint` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT UNSIGNED,
  `device_id` VARCHAR(64) NOT NULL,
  `platform` ENUM('ANDROID', 'IOS') NOT NULL,
  `token` VARCHAR(255) NULL,
  `status` ENUM('ACTIVE', 'LOGGED_OUT', 'ACCOUNT_LOCKED', 'USER_WITHDRAW') NOT NULL DEFAULT 'ACTIVE',
  `is_push_enabled` BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT `fk_push_endpoint_user_id`
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
      ON DELETE CASCADE
      ON UPDATE CASCADE
) ENGINE=InnoDB;

-- =========================
-- consent_form / user_consent
-- =========================
DROP TABLE IF EXISTS `consent_form`;
CREATE TABLE `consent_form` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `uuid` varchar(64),
  `title` TEXT NOT NULL
) ENGINE=InnoDB;

DROP TABLE IF EXISTS `user_consent`;
CREATE TABLE `user_consent` (
  `id` INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  `uuid` VARCHAR(64) NOT NULL,
  `user_id` INT UNSIGNED NOT NULL,
  `consent_form_id` INT UNSIGNED NOT NULL,
  `agreed_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` ENUM('ACTIVE', 'EXPIRED', 'REVOKED') NOT NULL DEFAULT 'ACTIVE',
  `expired_at` DATETIME NOT NULL,
  `revoked_at` DATETIME NULL,
  CONSTRAINT `fk_user_consent_user_id`
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
      ON DELETE CASCADE
      ON UPDATE CASCADE,
  CONSTRAINT `fk_consent_form_id`
    FOREIGN KEY (`consent_form_id`) REFERENCES `consent_form`(`id`)
      ON DELETE CASCADE
      ON UPDATE CASCADE
) ENGINE=InnoDB;

-- =========================
-- slot / bank
-- =========================
DROP TABLE IF EXISTS `slot`;
CREATE TABLE `slot` (
  `id` INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  `uuid` VARCHAR(64) NOT NULL,
  `name` VARCHAR(128) NOT NULL,
  `is_saving` BOOLEAN NOT NULL DEFAULT FALSE,
  `rank` INT UNSIGNED COMMENT '사람들이 많이 사용하는 순위'
) ENGINE=InnoDB;

DROP TABLE IF EXISTS `bank`;
CREATE TABLE `bank` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `uuid` VARCHAR(64) NOT NULL,
  `name` VARCHAR(64) NOT NULL,
  `code` VARCHAR(64) NOT NULL,
  `color` VARCHAR(64)
) ENGINE=InnoDB;

-- =========================
-- account
-- =========================
DROP TABLE IF EXISTS `account`;
CREATE TABLE `account` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `uuid` VARCHAR(64) NOT NULL,
  `user_id` INT UNSIGNED NOT NULL,
  `bank_id` INT UNSIGNED NOT NULL,
  `alias` VARCHAR(128),
  `encrypted_account_no` VARCHAR(255) NOT NULL,
  `balance` BIGINT UNSIGNED NOT NULL DEFAULT 0,
  `is_primary` BOOLEAN NOT NULL DEFAULT FALSE,
  `last_synced_transaction_unique_no` VARCHAR(255),
  `last_synced_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT `fk_account_user_id`
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
      ON DELETE CASCADE
      ON UPDATE CASCADE,
  CONSTRAINT `fk_account_bank_id`
    FOREIGN KEY (`bank_id`) REFERENCES `bank`(`id`)
      ON DELETE CASCADE
      ON UPDATE CASCADE,
  UNIQUE KEY `uk_account_user_uuid` (`user_id`, `uuid`)   -- 🔒 사용자별 account.uuid 유니크
) ENGINE=InnoDB;

-- =========================
-- account_slot / slot_history
-- =========================
DROP TABLE IF EXISTS `account_slot`;
CREATE TABLE `account_slot` (
  `id` INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  `uuid` VARCHAR(64) NOT NULL,
  `account_id` INT UNSIGNED NOT NULL,
  `slot_id` INT UNSIGNED NOT NULL,
  `initial_budget` BIGINT UNSIGNED NOT NULL DEFAULT 0,
  `current_budget` BIGINT UNSIGNED NOT NULL DEFAULT 0,
  `spent` BIGINT UNSIGNED NOT NULL DEFAULT 0,
  `budget_change_count` INT UNSIGNED NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_budget_exceeded` BOOLEAN NOT NULL DEFAULT FALSE,
  `is_custom` BOOLEAN NOT NULL DEFAULT FALSE,
  `custom_name` VARCHAR(64),
  `is_alert_sent` BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT `fk_account_slot_account_id`
    FOREIGN KEY (`account_id`) REFERENCES `account`(`id`)
      ON DELETE CASCADE
      ON UPDATE CASCADE,
  CONSTRAINT `fk_account_slot_slot_id`
    FOREIGN KEY (`slot_id`) REFERENCES `slot`(`id`)
      ON DELETE CASCADE
      ON UPDATE CASCADE
) ENGINE=InnoDB;

DROP TABLE IF EXISTS `slot_history`;
CREATE TABLE `slot_history` (
  `id` INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  `uuid` VARCHAR(64) NOT NULL,
  `account_slot_id` INT UNSIGNED NOT NULL,
  `changed_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `old_budget` BIGINT UNSIGNED NOT NULL DEFAULT 0,
  `new_budget` BIGINT UNSIGNED NOT NULL DEFAULT 0,
  CONSTRAINT `fk_slot_history_account_slot_id`
    FOREIGN KEY (`account_slot_id`) REFERENCES `account_slot`(`id`)
      ON DELETE CASCADE
      ON UPDATE CASCADE
) ENGINE=InnoDB;

-- =========================
-- transaction
-- =========================
DROP TABLE IF EXISTS `transaction`;
CREATE TABLE `transaction` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `uuid` VARCHAR(64) NOT NULL,
  `account_id` INT UNSIGNED NOT NULL,
  `account_slot_id` INT UNSIGNED NOT NULL,
  `unique_no` BIGINT UNSIGNED NOT NULL COMMENT 'transactionUniqueNo',
  `type` VARCHAR(64) NOT NULL COMMENT 'transactionType, transactionTypeName',
  `opponent_account_no` VARCHAR(255) COMMENT 'transactionAccountNo',
  `summary` VARCHAR(255) NOT NULL COMMENT 'transactionSummary',
  `amount` BIGINT UNSIGNED NOT NULL COMMENT 'transactionBalance',
  `balance` BIGINT UNSIGNED NOT NULL,
  `transaction_at` DATETIME NOT NULL COMMENT 'transactionDate, transactionTime',
  CONSTRAINT `fk_transaction_account_id`
    FOREIGN KEY (`account_id`) REFERENCES `account`(`id`)
      ON DELETE CASCADE
      ON UPDATE CASCADE,
  CONSTRAINT `fk_transaction_account_slot_id`
    FOREIGN KEY (`account_slot_id`) REFERENCES `account_slot`(`id`)
      ON DELETE CASCADE
      ON UPDATE CASCADE,
  KEY `idx_tx_account_time` (`account_id`, `transaction_at`)   -- 🔎 월별 검색 최적화
) ENGINE=InnoDB;

-- =========================
-- ai_report
-- =========================
DROP TABLE IF EXISTS `ai_report`;
CREATE TABLE `ai_report` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `uuid` VARCHAR(64) NOT NULL,
  `account_id` INT UNSIGNED NOT NULL,
  `content` JSON NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT `fk_ai_report_account_id`
    FOREIGN KEY (`account_id`) REFERENCES `account`(`id`)
      ON DELETE CASCADE
      ON UPDATE CASCADE,
  KEY `idx_ai_report_account_created` (`account_id`, `created_at`)  -- 🔎 최신 보고서 조회
) ENGINE=InnoDB;

-- =========================
-- notification
-- =========================
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
  `id` INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  `uuid` VARCHAR(64) NOT NULL,
  `user_id` INT UNSIGNED NOT NULL,
  `title` VARCHAR(255) NOT NULL,
  `body` TINYTEXT,
  `is_delivered` BOOLEAN,
  `delivered_at` DATETIME,
  `is_read` BOOLEAN DEFAULT FALSE NOT NULL,
  `read_at` DATETIME,
  `type` ENUM('SYSTEM', 'DEVICE', 'BUDGET', 'SLOT', 'UNCATEGORIZED', 'TRANSACTION', 'MARKETING') DEFAULT NULL,
  `tx_id` INT UNSIGNED NULL,
  CONSTRAINT `fk_notification_user_id`
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
      ON DELETE CASCADE
      ON UPDATE CASCADE,
  CONSTRAINT `fk_notification_tx_id`
    FOREIGN KEY (`tx_id`) REFERENCES `transaction`(`id`)
      ON DELETE SET NULL
      ON UPDATE CASCADE,
  UNIQUE KEY `uq_notification_user_type_tx` (`user_id`,`type`,`tx_id`),
  KEY `idx_notification_user_isread_type` (`user_id`,`is_read`,`type`)
) ENGINE=InnoDB;



-- =========================
-- wishlist
-- =========================
DROP TABLE IF EXISTS `wishlist`;
CREATE TABLE `wishlist` (
  `id` INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  `uuid` VARCHAR(64) NOT NULL,
  `user_id` INT UNSIGNED NOT NULL,
  `name` VARCHAR(64) NOT NULL,
  `price` BIGINT UNSIGNED NOT NULL DEFAULT 0,
  `image` BLOB NULL,
  CONSTRAINT `fk_wishlist_user_id`
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
      ON DELETE CASCADE
      ON UPDATE CASCADE
) ENGINE=InnoDB;

-- =========================
-- email
-- =========================
DROP TABLE IF EXISTS `email`;
CREATE TABLE `email` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT UNSIGNED,
  `name` VARCHAR(64) NOT NULL,
  `email` VARCHAR(128) NOT NULL,
  `is_primary` TINYINT(1) NOT NULL DEFAULT 0,
  `verified_at` DATETIME NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT `fk_email_user_id`
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
      ON DELETE CASCADE
      ON UPDATE CASCADE,
  UNIQUE KEY `uq_user_email` (`user_id`, `email`),
  INDEX `idx_email_user_created` (`user_id`, `created_at`)   -- ↔ 일부 MySQL에서 DESC 무시 대비
) ENGINE=InnoDB;

-- =========================
-- refresh_token
-- =========================
DROP TABLE IF EXISTS `refresh_token`;
CREATE TABLE `refresh_token` (
  `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT UNSIGNED NOT NULL,
  `device_id` VARCHAR(100) NOT NULL,
  `family_id` CHAR(36) NOT NULL,
  `jti` CHAR(36) NOT NULL UNIQUE,
  `status` ENUM('ACTIVE','USED','REVOKED') NOT NULL,
  `expires_at` DATETIME NOT NULL,
  `rotated_from_jti` CHAR(36) NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_used_at` DATETIME NULL,
  INDEX `idx_user_device_status` (`user_id`, `device_id`, `status`),
  INDEX `idx_rt_family` (`family_id`),
  CONSTRAINT `fk_rt_user`
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB;

-- =========================
-- phone_verify_ticket
-- =========================
CREATE TABLE IF NOT EXISTS `phone_verify_ticket` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `token_hash`   BINARY(32)      NOT NULL,     -- 토큰은 해시만 저장(평문 금지)
  `phone`        VARCHAR(20)     NOT NULL,
  `purpose`      ENUM('SIGNUP','RESET','ETC') NOT NULL,
  `issued_at`    DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `expires_at`   DATETIME(6)     NOT NULL,
  `consumed_at`  DATETIME(6)     NULL,
  UNIQUE KEY `uk_token_hash` (`token_hash`),
  KEY `idx_phone_purpose_active` (`phone`, `purpose`, `consumed_at`, `expires_at`),
  KEY `idx_expires` (`expires_at`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS merchant_slot_decision (
    merchant_code     VARCHAR(64) PRIMARY KEY,
    merchant_name     VARCHAR(255) NULL,
    slot_id           INT UNSIGNED,
    slot_name         VARCHAR(100),
    slot_confidence   DECIMAL(4,2),
    method            VARCHAR(64) NOT NULL,
    decided_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                                 ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_msd_slot FOREIGN KEY (slot_id)
        REFERENCES slot(id)
        ON UPDATE CASCADE
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================
-- otp_code (SMS/FCM 인증 코드 저장)
-- =========================
DROP TABLE IF EXISTS `otp_code`;
CREATE TABLE `otp_code` (
  `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `phone`      VARCHAR(20)     NOT NULL,                                       -- 숫자만 저장 권장
  `purpose`    ENUM('LOGIN','DEVICE_VERIFY','PIN_RESET','SIGNUP') NOT NULL,    -- 목적 추가 가능
  `device_id`  VARCHAR(100)    NOT NULL DEFAULT '-',                           -- DEVICE_VERIFY면 단말 구분, 그 외 '-'
  `code_hash`  VARCHAR(64)     NOT NULL COMMENT 'HMAC-SHA256(Base64URL, no padding)',
  `issued_at`  DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `expires_at` DATETIME(6)     NOT NULL,                                       -- NOW(6) + INTERVAL 3 MINUTE 등 BE에서 세팅
  `attempts`   INT UNSIGNED    NOT NULL DEFAULT 0,
  `sent`       TINYINT(1)      NOT NULL DEFAULT 0,                             -- 발송 성공 시 1
  `status`     ENUM('PENDING','SENT','USED','EXPIRED') NOT NULL DEFAULT 'PENDING',
  UNIQUE KEY `uq_otp_key` (`phone`,`purpose`,`device_id`),                     -- 단일 활성 OTP 보장
  KEY `idx_otp_expires` (`expires_at`)
) ENGINE=InnoDB;
