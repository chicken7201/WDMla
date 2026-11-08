# GTNH 2.9.0 신규 통합 판정

조사 원본은 `C:\Users\cmsbo\OneDrive\Dokumen\Minecraft\mod_test\GTNH walia test\`에서 읽기만 했다. 구현 대상은 이 WDMla 저장소다. 기준 브랜치는 `master`이며 작업 시작 시 변경사항은 없었다. 원격 `origin`은 `chicken7201/WDMla`, `upstream`은 `GTNewHorizons/WDMla`였다.

`PORTING_CANDIDATES_V2.md`와 대상 JAR의 `javap` 출력, 현재 `src/main/java/com/gtnewhorizons/wdmla/plugin` 등록 코드를 대조했다. 기존 범용 ItemStorage/FluidStorage/Progress Provider와 Forestry, Railcraft, IC2, Blood Magic, Natura, HarvestCraft, Storage Drawers 통합을 검토했다. 아래 후보의 정확한 타일 클래스를 대상으로 한 WDMla Provider는 없었다. 기존 아이템 저장소가 보일 수 있어도 작업 상태나 재료 목표량을 대신하지는 않는다.

| 후보 | 판정 | 확인한 근거 및 표시 범위 |
|---|---|---|
| Avaritia `TileEntityCompressor` | IMPLEMENT | `getProgress()`와 `getTarget()`이 공개 메서드다. 바이트코드에서 투입 재료 수에 따라 progress가 증가하고 CompressorManager 비용이 target이 됨을 확인했다. 재료 현재/필요 수량만 표시한다. |
| Witching Gadgets `TileEntitySpinningWheel` | IMPLEMENT | 공개 `progress`, `maxProgress` 필드와 `isActive()`를 확인했다. `getProgressScaled(int)`도 이 두 필드를 나누어 계산한다. 활성 작업에서만 진행 막대를 표시한다. |
| Advanced Solar Panels `TileEntityMolecularTransformer` | NEEDS_RUNTIME_TEST | `isActive()`, `lastProgress`, `energyBuffer`, `lastRecipeEnergyUsed`가 있으나 갱신/상한/오류 상태의 의미와 기존 IC2 에너지 표시 중복을 확정하지 못했다. |
| Cooking for Blockheads `TileToaster` | NOT_WORTH_IMPLEMENTING | 공개 `isActive()`와 인벤토리는 확인했으나 별도 처리 시간 API가 없다. 아이템 저장소와 상태 한 줄 이상의 유용한 정보가 확인되지 않았다. |
| Draconic Evolution `TileReactorEnergyInjector`, `TileReactorStabilizer` | NEEDS_RUNTIME_TEST | 둘 다 `isActive()`, `isValid`, `getMaster()`가 있으나 부품 상태만으로 원자로 안전 수치를 설명할 수 없다. 구조 연결과 에너지 표시 정책을 게임에서 확인해야 한다. |
| Loot Games `LegacyGameOfLightTile` | NOT_WORTH_IMPLEMENTING | 공개 `getIsActive()` 외에 안전하게 표시할 진행 단계 API를 확인하지 못했다. 활성 여부 한 줄은 게임 플레이 가치가 낮고 추가 상태 노출은 정답 누출 가능성이 있다. |
| SGCraft `SGBaseTE` | NEEDS_RUNTIME_TEST | `state`, `isConnected()`, `isActive()`를 확인했다. 주소 접근 정책과 상태 전환을 검증하기 전에는 연결/다이얼링/주소를 표시하지 않는다. |
| Hardcore Ender Expansion `TileEntityAccumulationTable`, `TileEntityTransportBeacon` | NEEDS_RUNTIME_TEST | 테이블의 `isWorking()`은 공개, 비컨의 `isWorking()`은 **protected**로 확인됐다. `hasEnergy()`는 비컨에 있으나 사용자 가치와 기존 자원 표시 중복은 게임 확인이 필요하다. |

`NEEDS_RUNTIME_TEST`는 아직 구현 대상으로 확정하지 않았다. 해당 모드들을 억지로 `isActive()` 한 줄 Provider로 대체하지 않았다.
