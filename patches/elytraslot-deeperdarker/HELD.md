# 보류: Elytra Slot 9.0.2 × Deeper Darker 1.4.1 영혼 겉날개 (2026-11-04)

`patch.toml` 의 `status = "held"` 라 `tools/build.py` 가 빌드하지 않는다. sable-stepfix 1.0.1 에 들어갔다가 1.0.2 에서 뺐다.

- 문제: Elytra Slot 이 없어진 `static int DeeperDarkerConfig.soulElytraCooldown` 을 읽는다
  (서버 `SoulElytraBoostPayload.lambda$handle$0`, 클라이언트 `DeeperDarkerPlugin.tick`). 1.4.1 은 `CONFIG.soulElytraCooldown.getAsInt()`.
- 서버만 고치면 안 되는 이유: 지금은 서버가 45줄에서 먼저 실패해 쿨다운이 안 걸린다. 서버를 고치면 쿨다운이 클라이언트로 가고,
  미패치 클라이언트의 `DeeperDarkerPlugin.tick` 이 쿨다운 중 같은 필드를 읽다 `NoSuchFieldError` 로 죽을 수 있다.
- 다시 낼 조건: 클라이언트에도 `DeeperDarkerPluginMixin` 을 배포(서버 전용 sable-stepfix jar 와 분리한 이 패치 jar, `side = "both"`)하고
  클라에서 쿨다운 표시까지 확인한 뒤, 서버 `SoulElytraBoostPayloadMixin` 을 같이 켠다.
- 다시 낼 때 할 일: 자기 `neoforge.mods.toml`(새 modId)과 `[build]`(Modrinth `elytra-slot` 9.0.2+1.21.1·`deeperdarker`
  1.4.1-neoforge-1.21.1 를 클래스패스에)를 만들고, 패키지를 `kr.nexa.sablestepfix.compat` 에서 새 이름으로 옮긴다.
