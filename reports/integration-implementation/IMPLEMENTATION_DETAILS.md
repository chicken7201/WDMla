# 구현 세부사항

코드는 `src/main/java/com/gtnewhorizons/wdmla/plugin/newintegrations/`에 있다.

| Provider | 서버 조회 | 클라이언트 표시 | 설정/번역 |
|---|---|---|---|
| `AvaritiaCompressorProvider` | `getProgress()` / `getTarget()`을 캐시한 리플렉션 메서드로 읽음 | 재료 수량/필요 수량 진행 막대, 목표가 0이면 숨김 | `avaritia:compressor_progress`, `provider.wdmla.avaritia.compressor_progress`, `hud.msg.wdmla.avaritia.ingredients` |
| `SpinningWheelProvider` | 공개 `progress` / `maxProgress`를 캐시한 필드로 읽음 | 처리 진행 막대, 진행값 또는 최대값이 0이면 숨김 | `witchinggadgets:spinning_wheel_progress`, `provider.wdmla.witchinggadgets.spinning_wheel_progress`, 기존 `hud.msg.wdmla.progress` |

`OptionalProgressPlugin`은 기존 `@WDMlaPlugin` 스캐너가 발견한다. 공통 등록에서 대상 클래스를 `Class.forName`으로 찾고, 없거나 링크할 수 없으면 등록을 건너뛴다. 클라이언트 컴포넌트는 기본 `Block` 대상에 등록되지만 정확한 타일 클래스명과 서버 NBT 키가 일치할 때만 렌더링한다. 두 Provider는 서로 다른 UID와 NBT 키를 쓴다.

서버는 필요한 정수 두 개만 전송한다. 음수 진행값과 0 이하 최대값은 생략하고, 최대값 초과 진행값은 최대값으로 제한한다. 무효 타일은 조회하지 않는다. 일반 인벤토리나 전체 타일 NBT는 전송하지 않는다. 기존 WDMla 자동 생성 설정에서 각 Provider를 끌 수 있다. 영어/한국어 키는 기존 언어 파일에 추가했다.
