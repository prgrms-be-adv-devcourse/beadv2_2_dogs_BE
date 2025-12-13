-- MySQL 데이터베이스 초기화 스크립트
-- 각 모듈별 데이터베이스 생성 및 권한 부여

-- 데이터베이스 생성 (없으면 생성)
CREATE DATABASE IF NOT EXISTS `baroauth` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `baroseller` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `barobuyer` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `baroorder` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `barosupport` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- barouser에게 모든 데이터베이스에 대한 모든 권한 부여
-- '%'는 모든 호스트에서 접속 가능하도록 설정
GRANT ALL PRIVILEGES ON `baroauth`.* TO 'barouser'@'%';
GRANT ALL PRIVILEGES ON `baroseller`.* TO 'barouser'@'%';
GRANT ALL PRIVILEGES ON `barobuyer`.* TO 'barouser'@'%';
GRANT ALL PRIVILEGES ON `baroorder`.* TO 'barouser'@'%';
GRANT ALL PRIVILEGES ON `barosupport`.* TO 'barouser'@'%';

-- localhost에서도 접속 가능하도록 권한 부여
GRANT ALL PRIVILEGES ON `baroauth`.* TO 'barouser'@'localhost';
GRANT ALL PRIVILEGES ON `baroseller`.* TO 'barouser'@'localhost';
GRANT ALL PRIVILEGES ON `barobuyer`.* TO 'barouser'@'localhost';
GRANT ALL PRIVILEGES ON `baroorder`.* TO 'barouser'@'localhost';
GRANT ALL PRIVILEGES ON `barosupport`.* TO 'barouser'@'localhost';

-- 권한 변경사항 즉시 적용
FLUSH PRIVILEGES;

