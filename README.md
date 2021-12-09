# Intrusion Detection System

### 프로젝트 정보

```
프로젝트 명
Intrusion Detection System(침입 탐지 시스템)

클라이언트
Arduino Nano 33 IoT

서버
자바 소켓 서버
아파치 톰켓 웹 애플리케이션 서버
```

```
지도교수
경성대학교 컴퓨터공학과 최재원
경성대학교 정보통신공학과 신광호

참여학생
경성대학교 컴퓨터공학과 이상호
경성대학교 컴퓨터공학과 임주형

개발기간
전체 시스템 계획 및 개발 - 2020.09 ~ 2021.01
코드 리뷰 및 향후 계획 수립 - 2021.02 ~ 2021.06
전체 시스템 모듈화 / 리팩토링 - 2021.07 ~ 2021.12

```


#

### 연구과제 

```
클라이언트
1. 자바 소켓 서버에 연결요청한다.
2. 클라이언트의 가속도 센서를 이용하여 문의 움직임을 측정한다.
3. 문의 움직임이 감지될 때 마다 열림/닫힘 상태와 센서값을 서버로 전송한다.
4. 서버의 *메시지를 받아 행동을 처리한다.

* 메시지
- 방범모드: 문의 움직임이 감지되었을 때 LED, 부저를 울려 경고신호를 발생시킨다.
- 비방범모드: 문의 움직임이 감지되어도 경고신호를 발생시키지 않는다.
- 신호발생: LED, 부저를 울려 경고신호를 발생시킨다.
- 제거: 서버와 연결을 끊는다.
- 초기화: 서버와 연결을 끊고 내부 변수를 초기화시킨 뒤 다시 서버에 연결요청한다.
```

```
자바 소켓 서버
1. 자바 소켓 서버를 열고 클라이언트의 접속 요청을 대기한다.
2. 클라이언트의 데이터를 수신하고 메시지를 전송한다.
3. 데이터베이스를 연결하여 클라이언트의 데이터를 저장한다.
```

```
웹 애플리케이션 서버
1. 데이터베이스를 통해 클라이언트의 ID, 상태정보등을 가져온다.
2. 웹 페이지를 통해 클라이언트의 상태정보를 볼 수 있게 한다.
3. 웹 페이지를 통해 클라이언트를 제어할 수 있도록 한다.
```

#

### 사용 기술

```
Arduino
  WiFiNINA
  IMU(acceleration/gyroscope)

Java 
 Web sockect, socket communication
 JDBC / Connection pool
 Servlet

Web
 HTML, CSS, Java script
 JQuery
 AJAX
 JSP
 
```

#

### 경성대학교 산학협력단 지원
