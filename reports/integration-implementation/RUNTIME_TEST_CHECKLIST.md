# 실제 게임 검증 체크리스트

GTNH 2.9.0에서 WDMla와 대상 모드의 서버/클라이언트 버전을 맞춘 뒤 확인한다. `latest.log`에서 `Optional progress API changed`, `Unable to read optional progress tile`, `skipped plugin`, `NoClassDefFoundError`를 확인한다.

| 대상 블록 | 준비/상태 | 기대 표시 |
|---|---|---|
| Avaritia Compressor | 블록을 설치하고 유효한 압축 레시피의 재료를 일부 투입 | 재료 현재/필요 수량 막대. 추가 투입 때 증가하고 완료 뒤 새 작업에 맞게 갱신 |
| Avaritia Compressor | 레시피/목표가 없는 빈 상태 | 재료 막대 없음. 일반 아이템 저장 표시는 기존 설정대로 동작 |
| Witching Gadgets Spinning Wheel | 유효한 재료를 넣고 작업 시작 | 처리 진행 막대가 증가. 완료 후 사라지거나 다음 작업에 맞게 초기화 |
| Witching Gadgets Spinning Wheel | 빈 상태 또는 정지 상태 | 진행값이 0인 경우 막대 없음. 종료 후 진행값이 남는다면 실제 작업 상태와 대조 |

각 블록에서 설정 화면의 해당 Provider를 껐다 켜며 막대가 사라지고 다시 나타나는지 확인한다. 해당 모드를 각각 제거한 환경에서는 WDMla 로딩이 성공하고 관련 막대가 표시되지 않아야 한다.

싱글플레이: **미실행**. 위 시나리오를 로컬 월드에서 확인한다.

멀티플레이: **미실행**. 전용 서버에서 두 블록을 다른 플레이어와 함께 관찰하고, 진행 수치와 서버 값이 일치하며 패킷/클래스 로딩 오류가 없는지 확인한다. 모드가 서버와 클라이언트 중 한쪽에만 없을 때는 모드팩 자체의 설치 요구사항을 먼저 따른다.

나머지 `NEEDS_RUNTIME_TEST` 후보는 원래 모드 GUI와 WDMla/기존 Waila 표시를 비교해 상태값의 의미, 중복, 접근 권한을 확인한 뒤 구현 여부를 다시 판정한다.
