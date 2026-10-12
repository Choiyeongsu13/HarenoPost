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
| 보안 그룹 | 22(Instance Connect), 2222(내 IP), 8080 또는 80(전체). **1521 은 열지 않는다** |
| DB | Docker `gvenzl/oracle-xe:21-slim`, 서비스 이름 `XEPDB1`, 계정 `harenopost`, SGA 640M |
| 비용 | 계속 켜 두면 월 약 $25 (t3.small + EBS + IPv4). 끝나면 인스턴스 종료 + **탄력적 IP 릴리스** |

### 매일 개발 시작할 때 (PC PowerShell, 창은 켜 둔다)

```bash
ssh -p 2222 -i "$env:USERPROFILE\.ssh\harenopost" -N -L 1521:localhost:1521 ec2-user@43.200.133.12
```

서버에 들어갈 때: `ssh -p 2222 -i "$env:USERPROFILE\.ssh\harenopost" ec2-user@43.200.133.12`
다른 장소(집 등)에서 접속이 안 되면 보안 그룹 2222 규칙의 "내 IP" 를 다시 맞춘다.

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
  -p 127.0.0.1:1521:1521 \
  -e ORACLE_PASSWORD='system비밀번호' \
  -e APP_USER=harenopost -e APP_USER_PASSWORD='앱비밀번호' \
  -e TZ=Asia/Tokyo \
  -v oracle-data:/opt/oracle/oradata \
  gvenzl/oracle-xe:21-slim

docker logs -f oracle   # "DATABASE IS READY TO USE!" 가 나오면 Ctrl+C
```

- `APP_USER` 가 XEPDB1 안에 `harenopost` 계정을 만들고 권한을 준다(ORA-01017 방지)
- `127.0.0.1:1521` 로만 열어서 외부에서는 접속할 수 없다
- 데이터는 `oracle-data` 볼륨에 남는다(컨테이너를 지워도 유지)

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

## 5. 테이블 만들기 (내 PC 에서 SSH 터널)

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

### 5-3. 터널 열기

내 PC 에서 터널을 켜 둔다(창을 닫으면 끊긴다).

```bash
ssh -p 2222 -i "$env:USERPROFILE\.ssh\harenopost" -N -L 1521:localhost:1521 ec2-user@43.200.133.12
```

SQL Developer: 호스트 `localhost`, 포트 `1521`, **서비스 이름** `XEPDB1`, 사용자 `harenopost` → `sql/schema.sql` 실행.
관리자 계정의 passwd 는 BCrypt 값으로 바꿔서 넣는다.

로컬 개발도 같은 터널로 EC2 DB 를 쓴다. `application-secret.yml`:

```yaml
spring:
  datasource:
    url: jdbc:oracle:thin:@localhost:1521/XEPDB1
    username: harenopost
    password: 앱비밀번호
```

## 6. 앱 실행 (10/24)

```bash
mkdir -p ~/harenopost/upload
export DB_URL='jdbc:oracle:thin:@localhost:1521/XEPDB1'
export DB_USERNAME=harenopost
export DB_PASSWORD='앱비밀번호'
export MAIL_USERNAME='보내는주소@gmail.com'
export MAIL_PASSWORD='구글앱비밀번호'
export UPLOAD_DIR=/home/ec2-user/harenopost/upload

nohup java -Xms256m -Xmx512m -Duser.timezone=Asia/Tokyo \
  -jar HarenoPost-1.0.jar --spring.profiles.active=prod > app.log 2>&1 &
```

- `-Xmx512m`: Oracle 과 메모리를 나눠 쓰기 위해 힙을 제한한다
- 환경변수는 `~/.bashrc` 가 아닌 별도 파일(`~/harenopost/env`, 권한 600)에 두고 `source` 하면 git 이나 기록에 남지 않는다

## 문제가 생기면

| 증상 | 확인 |
|---|---|
| 앱이 갑자기 죽음 | `dmesg \| grep -i kill` 로 OOM 확인 → 스왑·4번 설정 확인 |
| ORA-01017 | 서비스 이름이 `XEPDB1` 인지(SID `XE` 아님), 사용자·비밀번호 대소문자 |
| ORA-12541 / 연결 거부 | `docker ps` 로 oracle 실행 중인지, 터널이 켜져 있는지 |
| ssh `Connection timed out` | 네트워크가 22 번을 막는지(`-p 2222` 사용), 보안 그룹 "내 IP" 가 지금 IP 인지 |
| STS 실행 시 `-parameters` 500 에러 | `@PathVariable("code")` 처럼 이름을 적었는지 (Eclipse 컴파일러는 파라미터 이름을 남기지 않음) |
| 시간이 9시간 어긋남 | `date`, `docker exec oracle date`, JVM `-Duser.timezone` |
