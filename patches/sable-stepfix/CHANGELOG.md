# sable-stepfix

## 1.0.2 - 2026-11-04

- Elytra Slot + Deeper Darker 영혼 겉날개 호환을 다시 뺐습니다. 서버에서만 고치면 쿨다운이 패치 없는 클라이언트로 넘어가
  클라이언트 쪽 Elytra Slot 이 같은 필드를 읽다 크래시할 수 있습니다. 따로 [elytraslot-deeperdarker](https://github.com/nexa-kr/nexa-patches/tree/main/patches/elytraslot-deeperdarker) 로 보류해 둡니다.

## 1.0.1 - 2026-11-04

- 이미 제거된 하위 레벨 강체의 속도를 물으면 `RuntimeException: Body has been removed` 대신 0 을 돌려줍니다
  (`ActiveSableCompanion.getVelocity`, 서버 "Ticking entity" 크래시). [sable#1454](https://github.com/ryanhcode/sable/issues/1454)
- Elytra Slot 9.0.2 + Deeper Darker 1.4.1 영혼 겉날개 호환을 넣었습니다(1.0.2 에서 뺌).

## 1.0.0 - 2026-10-16

- 물리 스텝 안에서 일어난 약한 블록 파괴를 스텝이 끝난 뒤로 미룹니다. 비행선이 나무나 작물에 부딪힐 때
  `Rapier3D.newVoxelCollider` 가 같은 스레드의 잠금을 기다리며 멈춰 서버가 watchdog 으로 꺼지던 문제입니다.
  [sable#1603](https://github.com/ryanhcode/sable/issues/1603), [sable#1632](https://github.com/ryanhcode/sable/pull/1632)
