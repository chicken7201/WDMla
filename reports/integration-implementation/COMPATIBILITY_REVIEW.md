# 호환성 검토

- WDMla의 `IWDMlaCommonRegistration`, `IWDMlaClientRegistration`, `IBlockComponentProvider`, `IServerDataProvider`와 기존 툴팁 진행 컴포넌트를 재사용한다. Waila 호환 API와 기존 Provider 코드는 수정하지 않았다.
- 선택적 모드 클래스를 Java 타입으로 직접 참조하지 않는다. 대상 클래스는 등록 시에만 로드한다. 미설치 또는 링크 실패 시 해당 서버 Provider를 건너뛰며, 클라이언트 Provider는 아무것도 표시하지 않는다.
- 기존 범용 아이템/액체 저장소와 별도 UID를 사용한다. 두 신규 막대는 재료 누적량 및 물레의 처리 진행량에만 해당한다. 대상 타일에 같은 정보를 제공하는 기존 등록은 현재 소스에서 찾지 못했다.
- 타일이 null 또는 invalid일 때 조회하지 않는다. 서버 데이터는 정수 두 개이며 월드/멀티블록 탐색, 아이템 직렬화, 게임 상태 변경이 없다.
- 정적 검증만으로 각 선택적 모드의 실제 GTNH 서버/클라이언트 부팅 및 멀티플레이 동작을 보장할 수 없다. 실행 점검은 별도 체크리스트에 기록했다.
- `.github/workflows/release.yml`, 외부 JAR, 조사 프로젝트, `.venv`는 수정하지 않았다.
