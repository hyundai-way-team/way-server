# WAY 서버 기여 가이드

## 브랜치와 PR

1. 최신 `main`에서 작업 브랜치를 만든다. PR 하나에 한 가지 변경 목적을 담는다.
2. 작업 브랜치를 푸시하고 `main`을 대상으로 PR을 연다. 제목은 `[Feat] 픽업 조회 API 추가`처럼 `[Feat]`, `[Fix]`, `[Chore]`, `[Docs]`, `[Refactor]` 중 하나로 시작한다. 이모지는 생략한다.
3. PR에 변경 이유, 연동 영향, 검증 방법과 결과를 적는다. 작업을 완료하는 이슈는 `Closes #12`, 참고할 이슈는 `Refs #13`처럼 연결한다.
4. 서버 담당자가 검증 결과를 확인한 뒤 squash merge 한다. 필수 검사가 설정되어 있으면 통과를 확인한다. PR 제목을 squash 커밋 제목으로 사용한다.

REST API, WebSocket 메시지, 접근 권한 또는 픽업 상태 전이가 바뀌면 관련 팀 명세를 확인한다. 차량 앱이나 동승자 웹에서 대응할 내용이 있다면 해당 담당자와 변경 내용을 맞추고 PR에 기록한다. 명세는 [WAY 서버 맥락](docs/project-context.md)에서 찾을 수 있다.

## 패키지 구조

```text
com.hyundai.way
├── WayApplication
├── domain
│   ├── pickup
│   │   ├── controller
│   │   ├── service
│   │   ├── repository
│   │   ├── entity
│   │   └── dto
│   │       ├── request
│   │       └── response
│   └── vehicle
└── global
    ├── config
    ├── exception
    └── entity
```

패키지는 해당 역할을 맡는 코드가 생길 때 만든다. 업무 엔티티, 규칙, 오류 정의는 다른 도메인에서도 사용하더라도 소유 도메인에 둔다. 여러 도메인이 함께 쓰는 코드는 역할에 맞는 패키지에 둔다. 공통 설정은 `global.config`, 공통 오류 정의와 예외 처리는 `global.exception`, 생성·수정 시각을 공유하는 `BaseTimeEntity`는 `global.entity`에 둔다.

## Java와 Spring

| 항목 | 기준 |
| --- | --- |
| 클래스 | `PascalCase`. 예를 들어 `PickupController`, `PickupService`, `PickupRepository`를 사용한다. |
| 메서드·변수 | `camelCase`. 예를 들어 `createPickup`, `pickupId`를 사용한다. Spring Data Repository의 메서드는 프레임워크 명명 규칙을 따른다. |
| 상수 | `UPPER_SNAKE_CASE`를 사용한다. |
| 요청·응답 DTO | `dto.request`와 `dto.response`에 각각 `CreatePickupRequest`, `PickupResponse`처럼 용도가 드러나는 이름으로 `record`를 선언한다. API에는 DTO를 사용한다. |
| Controller | 요청 바인딩, 입력 검증, 서비스 호출, HTTP 응답 구성을 담당한다. |
| Service | 구체 클래스로 시작하고 별도 계약이나 구현 교체가 필요할 때 인터페이스를 분리한다. |
| 의존성 주입 | `private final` 필드에 생성자로 주입한다. 단순 생성자는 `@RequiredArgsConstructor`를 사용하고, 로깅이 필요하면 `@Slf4j`를 사용한다. |

엔티티는 단수 명사로 이름을 짓고 일반 클래스에 `@Entity`를 선언한다. 필요한 접근자에 `@Getter`를 사용하고, 기본 생성자는 `@NoArgsConstructor(access = AccessLevel.PROTECTED)`로 둔다. DTO와 엔티티에는 `@Data`를 사용하지 않는다. 엔티티 상태는 `startWaiting()`처럼 변경 의도가 드러나는 메서드로 바꾼다. Builder는 생성 인자가 많아 의미를 읽기 어려울 때 사용하고, `equals`와 `hashCode`는 엔티티의 식별 기준과 사용처에 맞춰 설계한다.

## REST API와 오류

- REST 경로는 `/api/v1`으로 시작한다. 컬렉션 경로에는 `/pickups`처럼 소문자 복수 명사를 쓰고, JSON 필드에는 `pickupId`처럼 `camelCase`를 쓴다.
- 일반 성공은 `200 OK`, 리소스 생성은 `201 Created`, 본문 없이 성공을 알릴 때는 `204 No Content`를 사용한다. 생성 응답에는 리소스 식별자를 포함한다.
- 본문이 있는 성공 응답은 API별 응답 DTO를 JSON 본문으로 직접 반환한다.
- 값이 없는 응답 필드는 `null`, 빈 목록은 `[]`로 표현한다. 날짜는 `2026-11-17`과 같은 ISO 날짜 형식으로, 실제 발생 시각은 UTC 기준 ISO 8601 문자열로 전달한다.
- 입력 검증 실패는 `400 Bad Request`로 응답한다. 예상하지 못한 오류는 `500 Internal Server Error`와 일반 안내 문구를 반환하고, 내부 예외 정보는 서버 로그에 기록한다.
- 실패 응답은 오류에 맞는 HTTP 상태를 반환하고, JSON 최상위에 오류 코드인 `code`와 안내 문구인 `message`를 담는다. 클라이언트는 `code`로 오류별 동작을 결정하고 `message`는 표시에 사용한다.
- 필드 검증 실패일 때만 최상위에 `errors` 배열을 포함한다. 각 항목에 잘못된 필드 경로인 `field`와 필드별 안내 문구인 `message`를 담는다.
- API별 요청·응답 예시는 [팀 API 명세](docs/project-context.md)를 확인한다.

## 변경 검증

[README의 실행 및 테스트 절차](README.md#기술-스택과-실행)를 따라 변경 범위를 검증한다. PR에는 실행한 명령이나 재현 절차와 관찰한 결과를 적고, 차량 앱·동승자 웹에서 확인한 내용이 있으면 함께 기록한다.
