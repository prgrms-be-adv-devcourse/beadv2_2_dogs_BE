#!/bin/bash
# MySQL 컨테이너 시작 시 항상 실행되는 스크립트
# 누락된 데이터베이스를 자동으로 생성하고 권한을 부여합니다.
# 이 스크립트는 기존 데이터를 보호하면서 누락된 데이터베이스만 생성합니다.

set -e

# MySQL이 준비될 때까지 대기
until mysqladmin ping -h localhost --silent; do
  echo "Waiting for MySQL to be ready..."
  sleep 2
done

echo "MySQL is ready. Checking and creating databases if needed..."

# MySQL에 연결하여 데이터베이스 생성 및 권한 부여
mysql -u root -p"${MYSQL_ROOT_PASSWORD:-rootpassword}" <<EOF
-- 데이터베이스 생성 (없으면 생성)
CREATE DATABASE IF NOT EXISTS \`baroauth\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS \`baroseller\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS \`barobuyer\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS \`baroorder\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS \`barosupport\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- barouser에게 모든 데이터베이스에 대한 모든 권한 부여
GRANT ALL PRIVILEGES ON \`baroauth\`.* TO 'barouser'@'%';
GRANT ALL PRIVILEGES ON \`baroseller\`.* TO 'barouser'@'%';
GRANT ALL PRIVILEGES ON \`barobuyer\`.* TO 'barouser'@'%';
GRANT ALL PRIVILEGES ON \`baroorder\`.* TO 'barouser'@'%';
GRANT ALL PRIVILEGES ON \`barosupport\`.* TO 'barouser'@'%';

-- localhost에서도 접속 가능하도록 권한 부여
GRANT ALL PRIVILEGES ON \`baroauth\`.* TO 'barouser'@'localhost';
GRANT ALL PRIVILEGES ON \`baroseller\`.* TO 'barouser'@'localhost';
GRANT ALL PRIVILEGES ON \`barobuyer\`.* TO 'barouser'@'localhost';
GRANT ALL PRIVILEGES ON \`baroorder\`.* TO 'barouser'@'localhost';
GRANT ALL PRIVILEGES ON \`barosupport\`.* TO 'barouser'@'localhost';

-- 권한 변경사항 즉시 적용
FLUSH PRIVILEGES;
EOF

echo "Database initialization completed."

