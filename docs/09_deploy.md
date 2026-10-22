# 09. EC2 서버 준비 (t3.small 한 대에 Oracle + 앱)

메모리가 2GB 라서 **스왑 2GB + Oracle 메모리 줄이기 + JVM 힙 제한** 세 가지를 꼭 한다.
아래 명령은 모두 EC2(Amazon Linux 2023, `ec2-user`)에서 실행한다.

## 현재 서버 (2026-10-12 구성 완료)

| 항목 | 값 |
|---|---|
| 리전 | 서울 (ap-northeast-2) |
| 인스턴스 | t3.small (2 vCPU, 2GB), Amazon Linux 2023, 키 페어 없음 |
| 디스크 | gp3 20GB (처음 8GB → 콘솔에서 확장) |
| IP | 탄력적 IP `43.200.133.12` (껐다 켜도 유지) |
| SSH | 22(브라우저 Instance Connect용), **2222**(학교 네트워크가 22번을 막아서 추가) |
| SSH 키 | PC `%USERPROFILE%\.ssh\harenopost` (ed25519, `~/.ssh/authorized_keys` 에 등록) |
| 보안 그룹 | 22(Instance Connect), 2222(내 IP), **2200(전체, Oracle)**, 8080 또는 80(전체) |
| DB | Docker `gvenzl/oracle-xe:21-slim`, 호스트 포트 **2200** → 컨테이너 1521, 서비스 이름 `XEPDB1`, 계정 `harenopost`, SGA 640M |
| 비용 | 계속 켜 두면 월 약 $25 (t3.small + EBS + IPv4). 끝나면 인스턴스 종료 + **탄력적 IP 릴리스** |

### DB 접속 (터널 불필요)

학교 네트워크가 1521 을 막고 2200 은 열려 있어서, Oracle 을 **2200 번으로 외부에 공개**했다(RyokanMaker 와 같은 방식).
외부에 열려 있으므로 SYS/SYSTEM, harenopost 비밀번호는 **추측하기 어려운 값**으로 바꿔 두었다(2026-10-13).

| 어디서 | URL |
|---|---|
| PC (SQL Developer, STS) | 호스트 `43.200.133.12`, 포트 `2200`, 서비스 이름 `XEPDB1` → `jdbc:oracle:thin:@43.200.133.12:2200/XEPDB1` |
| EC2 안의 앱 (배포) | `jdbc:oracle:thin:@localhost:2200/XEPDB1` |

서버에 들어갈 때: `ssh -p 2222 -i "$env:USERPROFILE\.ssh\harenopost" ec2-user@43.200.133.12`
다른 장소(집 등)에서 SSH 가 안 되면 보안 그룹 2222 규칙의 "내 IP" 를 다시 맞춘다.

---

아래는 처음 구성할 때의 절차다.

## 1. 시간대 · 스왑

```bash
sudo timedatectl set-timezone Asia/Tokyo
date   # JST 로 나오는지 확인

sudo dd if=/dev/zero of=/swapfile bs=128M count=16   # 2GB
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile swap swap defaults 0 0' | sudo tee -a /etc/fstab
free -h   # Swap 2.0Gi 확인
```

## 2. Docker · Java 21

```bash
sudo dnf install -y docker java-21-amazon-corretto-headless
sudo systemctl enable --now docker
sudo usermod -aG docker ec2-user
exit   # 다시 접속해야 docker 그룹이 적용된다
```

## 3. Oracle XE 실행

```bash
docker run -d --name oracle --restart unless-stopped \
  -p 2200:1521 \
  -e ORACLE_PASSWORD='system비밀번호' \
  -e APP_USER=harenopost -e APP_USER_PASSWORD='앱비밀번호' \
  -e TZ=Asia/Tokyo \
  -v oracle-data:/opt/oracle/oradata \
  gvenzl/oracle-xe:21-slim

docker logs -f oracle   # "DATABASE IS READY TO USE!" 가 나오면 Ctrl+C
```

- `APP_USER` 가 XEPDB1 안에 `harenopost` 계정을 만들고 권한을 준다(ORA-01017 방지)
- 비밀번호 환경변수는 **처음 만들 때만** 쓰인다. 이후 컨테이너를 다시 만들 때는 `-e ORACLE_PASSWORD ...` 없이 `-p 2200:1521 -e TZ=Asia/Tokyo -v oracle-data:...` 만 주면 된다
- 데이터·비밀번호·메모리 설정은 `oracle-data` 볼륨에 남는다(컨테이너를 지워도 유지)
- 보안 그룹에 `사용자 지정 TCP 2200 / 0.0.0.0/0` 추가

### 비밀번호 바꾸기

```bash
docker exec oracle resetPassword '새관리자비밀번호'   # SYS, SYSTEM
docker exec -it oracle sqlplus / as sysdba
```

```sql
ALTER SESSION SET CONTAINER = XEPDB1;
ALTER USER harenopost IDENTIFIED BY "새앱비밀번호";
EXIT
```

`EXIT` 다음 줄에 다른 명령을 붙여 넣으면 SQL 로 해석되니, 프롬프트가 `$` 로 돌아온 뒤 docker 명령을 친다.

## 4. Oracle 메모리 줄이기 (2GB 서버용)

```bash
docker exec -it oracle sqlplus / as sysdba
```

```sql
ALTER SYSTEM SET sga_max_size=640M SCOPE=SPFILE;
ALTER SYSTEM SET sga_target=640M SCOPE=SPFILE;
ALTER SYSTEM SET pga_aggregate_target=128M SCOPE=SPFILE;
EXIT
```

```bash
docker restart oracle
docker logs -f oracle   # 다시 READY 확인
free -h                 # available 이 600MB 이상 남는지 확인
```

## 5. SSH 접속 · 테이블 만들기

### 5-1. SSH 키 등록 (키 페어 없이 만든 인스턴스)

PC PowerShell 에서 키를 만들고 공개키를 출력한다.

```bash
ssh-keygen -t ed25519 -f "$env:USERPROFILE\.ssh\harenopost"
Get-Content "$env:USERPROFILE\.ssh\harenopost.pub"
```

브라우저 Instance Connect 창에서 공개키를 등록한다(주석 부분은 빼도 된다).

```bash
mkdir -p ~/.ssh && chmod 700 ~/.ssh
echo 'ssh-ed25519 AAAA... harenopost' >> ~/.ssh/authorized_keys && chmod 600 ~/.ssh/authorized_keys
```

### 5-2. SSH 를 2222 번에서도 받기 (22 번이 막힌 네트워크용)

```bash
printf 'Port 22\nPort 2222\n' | sudo tee /etc/ssh/sshd_config.d/10-port.conf
sudo sshd -t && sudo systemctl restart sshd
sudo ss -tlnp | grep sshd   # :22 와 :2222
```

보안 그룹에 `사용자 지정 TCP 2222 / 내 IP` 추가.

### 5-3. 테이블 만들기

SQL Developer: 호스트 `43.200.133.12`, 포트 `2200`, **서비스 이름** `XEPDB1`, 사용자 `harenopost` → `sql/schema.sql` 실행.
관리자 계정의 passwd 는 BCrypt 값으로 바꿔서 넣는다.

로컬 개발도 EC2 DB 를 그대로 쓴다. `application-secret.yml`:

```yaml
spring:
  datasource:
    url: jdbc:oracle:thin:@43.200.133.12:2200/XEPDB1
    username: harenopost
    password: "앱비밀번호"
```

> 처음에는 1521 을 `127.0.0.1` 에만 열고 SSH 터널(`ssh -p 2222 ... -N -L 1521:localhost:1521 ...`)로 접속했지만,
> 매번 터널을 켜야 해서 2200 직접 접속으로 바꿨다. 다시 닫고 싶으면 `-p 127.0.0.1:2200:1521` 로 컨테이너를 다시 만들고 보안 그룹 2200 규칙을 지운 뒤 터널을 쓴다.

## 6. 앱 실행 (2026-10-19 배포 완료)

공개 주소: **http://43.200.133.12:8080/** (보안 그룹 8080 전체 허용)

### 서버 구성

| 파일 | 내용 |
|---|---|
| `~/harenopost/HarenoPost.jar` | `./gradlew bootJar` 결과. **`application-secret.yml` 은 jar 에 넣지 않는다**(build.gradle 의 bootJar exclude) |
| `~/harenopost/env` (권한 600) | 비밀 정보. `SPRING_PROFILES_ACTIVE=prod`, `DB_URL=jdbc:oracle:thin:@localhost:2200/XEPDB1`, `DB_USERNAME`, `DB_PASSWORD`, `MAIL_USERNAME`, `MAIL_PASSWORD`(공백이 있으면 `"..."`), `UPLOAD_DIR=/home/ec2-user/harenopost/upload` |
| `~/harenopost/upload/` | 카드 디자인 이미지 (UUID 파일명) |
| `/etc/systemd/system/harenopost.service` | 아래 서비스. 재부팅·비정상 종료 시 자동으로 다시 뜬다 |

```ini
[Service]
User=ec2-user
WorkingDirectory=/home/ec2-user/harenopost
EnvironmentFile=/home/ec2-user/harenopost/env
ExecStart=/usr/bin/java -Xms256m -Xmx512m -Duser.timezone=Asia/Tokyo -jar /home/ec2-user/harenopost/HarenoPost.jar
Restart=on-failure
RestartSec=15        # 재부팅 직후 Oracle 이 아직 뜨는 중이면 15초 뒤 다시 시도
```

- `-Xmx512m`: Oracle 과 메모리를 나눠 쓰기 위해 힙을 제한한다 (배포 후 available 약 330MB, 스왑 거의 안 씀)
- env 파일은 셸 명령줄에 비밀번호를 쓰지 않도록 `scp` 로 올린다. Windows PowerShell 에서 `Get-Content | ssh` 로 보내면 맨 앞에 BOM 이 붙어 첫 변수 이름이 깨지므로 쓰지 않는다

### 다시 배포할 때 (PC PowerShell)

```bash
./gradlew bootJar
```

```bash
scp -P 2222 -i "$env:USERPROFILE\.ssh\harenopost" build\libs\HarenoPost.jar ec2-user@43.200.133.12:harenopost/HarenoPost.jar
```

```bash
ssh -p 2222 -i "$env:USERPROFILE\.ssh\harenopost" ec2-user@43.200.133.12 'sudo systemctl restart harenopost'
```

20초쯤 뒤 `http://43.200.133.12:8080/` 확인. 로그: `sudo journalctl -u harenopost -f`

### 기본 카드 디자인

배포 서버의 관리자 화면(`/Admin/admin_designs`)에서 테마별 2장씩 등록했다(お正月: 紅白·金びょうぶ / 誕生日: いちご·そらいろ / クリスマス: ヒイラギ·星空).
로컬 PC 에서 올리면 이미지 파일이 PC 에만 저장되고 DB 는 서버와 같이 쓰므로, **디자인은 반드시 서버에서 올린다.**

### 학교 네트워크에서 서버를 시험할 때

학교 방화벽이 평문 HTTP 로 비밀번호가 담긴 폼을 보내면 그 연결을 몇 분간 끊는 경우가 있었다(서버는 정상).
자동 점검은 SSH 터널(`ssh -p 2222 ... -N -L 18080:localhost:8080 ...`)로 `http://localhost:18080` 에 보내서 했다.
실제 서비스로 쓰려면 도메인 + HTTPS(nginx + Let's Encrypt)를 붙이는 것이 좋다.

## 문제가 생기면

| 증상 | 확인 |
|---|---|
| 앱이 갑자기 죽음 | `dmesg \| grep -i kill` 로 OOM 확인 → 스왑·4번 설정 확인 |
| ORA-01017 | 서비스 이름이 `XEPDB1` 인지(SID `XE` 아님), 사용자·비밀번호 대소문자 |
| ORA-12541 / 연결 거부 | `docker ps` 로 oracle 실행 중인지(`0.0.0.0:2200->1521`), 보안 그룹 2200 규칙, 인스턴스가 켜져 있는지 |
| ssh `Connection timed out` | 네트워크가 22 번을 막는지(`-p 2222` 사용), 보안 그룹 "내 IP" 가 지금 IP 인지 |
| STS 실행 시 `-parameters` 500 에러 | `@PathVariable("code")` 처럼 이름을 적었는지 (Eclipse 컴파일러는 파라미터 이름을 남기지 않음) |
| 시간이 9시간 어긋남 | `date`, `docker exec oracle date`, JVM `-Duser.timezone` |
