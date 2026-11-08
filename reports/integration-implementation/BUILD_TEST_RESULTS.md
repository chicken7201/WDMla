# 빌드 및 테스트 결과

| 명령/검증 | 결과 |
|---|---|
| `git status --short` (시작 전) | 변경 없음 |
| `javap -classpath <조사 JAR> -p/-c <대상 클래스>` | 8개 후보 10개 타일의 공개/보호 API 확인. Avaritia와 물레의 진행 의미는 바이트코드로 확인 |
| `.\gradlew.bat build` (기본 샌드박스) | Gradle 9.4.0 다운로드 시 네트워크 권한 오류로 중단. 소스 컴파일 전 실패 |
| `.\gradlew.bat build` (권한 상승, 최초) | 성공. Java 컴파일, Checkstyle, 기존 자동 테스트, JAR 생성 완료 |
| `.\gradlew.bat build` (최종 수정 후 재실행) | **BUILD SUCCESSFUL** (1분 28초, 23개 태스크). 기존 테스트 전부 통과. 영어/한국어 번역 키 3개씩 존재 확인 |

기존 `IWailaVariableWidthTooltipRenderer.java:30`의 찾을 수 없는 JavaDoc 참조 경고 3건과 Gradle 10 호환성 관련 deprecation 안내가 있었다. 빌드 실패는 없었다. 신규 Provider의 실제 게임 동작을 검증하는 자동 테스트는 없다.

실제 Minecraft 실행은 하지 않았다. 싱글플레이 및 멀티플레이는 모두 미검증이다.
