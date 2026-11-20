# WAY 서버 작업 지침

픽업 도메인, API 계약, 차량·동승자 권한, 실시간 통신을 설계하거나 변경하기 전에 [WAY 서버 맥락](docs/project-context.md)의 관련 항목과 연결된 명세를 확인한다.

## 서버 구조

WAY 서버는 `WayApplication`에서 시작하는 Spring Boot 애플리케이션이다. MVP는 차량 앱과 동승자 웹이 REST로 픽업 상태를 조회하고 WebSocket으로 변경을 받으며, 서버는 JPA로 MySQL에 데이터를 저장하는 구조로 설계했다. [서버 구조](README.md#서버-구조)

현재 소스의 주요 패키지는 다음과 같다.

```text
com.hyundai.way
├── WayApplication
├── domain
│   ├── pickup
│   │   ├── exception    
│   │   ├── domain       
│   │   ├── repository        
│   │   ├── service        
│   │   ├── controller
│   │   └── dto
│   │       ├── request
│   │       └── response     
│   └── vehicle
│       └── ...
└── global
    ├── config   
    ├── entity            
    └── exception         
```

- 업무 규칙, 엔티티, 도메인 예외는 소유 도메인에 둔다. 공통 설정은 `global.config`에 둔다.
- 업무 기능을 구현할 때 필요한 `controller`, `service`, `repository`, `dto.request`, `dto.response`는 해당 `domain.<업무>` 아래에 만든다. 컨트롤러는 요청 바인딩과 입력 검증, 서비스 호출, HTTP 응답 구성을 맡는다. 서비스는 유스케이스를 조율하고 저장소는 영속성 접근을 맡는다.

## 코드와 협업 컨벤션

- 클래스는 `PascalCase`, 메서드·변수는 `camelCase`, 상수는 `UPPER_SNAKE_CASE`를 쓴다. 의존성은 `private final` 필드에 생성자로 주입하고, 단순 생성자에는 `@RequiredArgsConstructor`를 사용한다. 서비스는 구체 클래스로 시작한다. [Java와 Spring 규칙](CONTRIBUTING.md#java와-spring)
- JPA 엔티티의 클래스 이름은 단수 명사로 짓는다. 필요한 접근자에 `@Getter`를 사용하고 기본 생성자는 `protected`로 둔다. 상태 변경에는 `startWaiting()`처럼 업무 의도가 드러나는 메서드를 사용한다.
- REST 경로는 `/api/v1`로 시작하고 컬렉션은 소문자 복수 명사, JSON 필드는 `camelCase`를 쓴다. 요청과 응답은 `record` DTO로 정의하고, 본문이 있는 성공 응답은 API별 DTO를 직접 반환한다. 업무 오류는 소유 도메인의 예외와 오류 코드로 정의하고 `GlobalExceptionHandler`에서 공통 오류 응답으로 처리한다. [REST API와 오류 규칙](CONTRIBUTING.md#rest-api와-오류)
- PR은 목적 하나를 담아 `main`을 대상으로 연다. 제목은 `[Feat]`, `[Fix]`, `[Chore]`, `[Docs]`, `[Refactor]` 중 하나로 시작하고 이모지는 생략한다. 변경 이유, 클라이언트 연동 영향, 검증 방법과 결과를 기록한다. 병합은 squash 방식으로 진행하고 PR 제목을 커밋 제목으로 사용한다. [브랜치와 PR 규칙](CONTRIBUTING.md#브랜치와-pr)

## 실행과 검증

저장소 루트에서 JDK 21과 Gradle Wrapper를 사용한다. 로컬 서버 기동(`./gradlew bootRun`)과 MySQL이 필요한 테스트의 연결 설정은 [README의 실행 절차](README.md#기술-스택과-실행)를 따른다. 변경 범위에 해당하는 테스트는 `./gradlew test --tests '<테스트 클래스 또는 패턴>'`으로 실행할 수 있다. CI는 MySQL을 연결해 `./gradlew build`를 실행하고 Docker 이미지 빌드를 확인한다. [.github/workflows/ci.yml](.github/workflows/ci.yml)

## 코드 리뷰

PR 리뷰를 시작하기 전에 [WAY 서버 코드 리뷰 규칙](docs/code-review.md)을 읽고 변경 범위에 해당하는 기준을 적용한다.
