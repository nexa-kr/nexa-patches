# NEXA 모드 패치

[NEXA](https://nexa.kr) 서버에서 만난 모드 크래시·호환 문제를 고치는 작은 패치 모드 모음입니다.
고친 내용은 원작자에게도 이슈·PR 로 알리고, 원작자 쪽에서 고쳐졌다고 확인되면 패치를 은퇴시킵니다.

Small fix-up mods for crashes and compatibility issues we hit on NEXA servers. Each fix is reported upstream;
a patch is retired once the upstream fix is confirmed.

## 구성

```
patches/<id>/
  patch.toml     패치 메타데이터(대상 모드·확인한 버전·고치는 크래시·원작자 링크)
  src/           모드 원본(NeoForge mixin)
tools/build.py   patch.toml 을 읽어 jar 를 만든다(공개 원본만 씀)
```

| 패치 | 상태 | 쪽 | 대상 |
|---|---|---|---|
| [`sable-stepfix`](patches/sable-stepfix) | active | 서버 | Sable 2.0.6 (MC 1.21.1 NeoForge) |
| [`elytraslot-deeperdarker`](patches/elytraslot-deeperdarker) | held | 양쪽 | Elytra Slot 9.0.2 × Deeper Darker 1.4.1 |

## 빌드

```bash
python3 tools/build.py sable-stepfix   # patches/sable-stepfix/build/<mod_id>-<version>.jar 와 sha256 출력
python3 tools/build.py --all           # status = "active" 전부
```

Python 3.11+ 와 JDK(`patch.toml` 의 `build.java` 이상)가 필요합니다. 클래스패스는 NeoForge 설치 프로그램의
서버 설치(`maven.neoforged.net`, 바닐라 서버는 Mojang 에서)와 Modrinth 모드 jar(sha512 확인)로 만들고
`~/.cache/nexa-patches`(`NEXA_PATCHES_CACHE`)에 둡니다. jar 항목 시각을 고정해 같은 원본이면 클래스 파일이 같게 나옵니다
(`MANIFEST.MF` 의 `Created-By` 는 JDK 빌드마다 달라 jar 전체 sha256 은 JDK 가 같을 때만 같습니다).

## 릴리스

태그 `<id>/v<version>`(예: `sable-stepfix/v1.0.2`)을 올리면 CI 가 빌드해 GitHub 릴리스에 jar 와 `.sha256` 을 붙입니다.
태그의 버전은 `patch.toml` 과 같아야 합니다.

## patch.toml

| 키 | 뜻 |
|---|---|
| `id`, `name`, `mod_id`, `version` | 패치 식별자, 표시 이름, 모드 id, 버전(`neoforge.mods.toml` 과 같아야 함) |
| `status` | `active`(빌드·배포) · `held`(보류, 빌드 안 함) · `retired`(원작자가 고침) |
| `side` | `server` · `client` · `both`. `both` 는 서버와 플레이어 쪽에 같이 들어가야 합니다 |
| `[build]` | `java`, `neoforge`(설치 버전), `libraries`(그 설치의 `libraries/` 아래 경로), `[[build.modrinth]]`(project·version) |
| `[[targets]]` | `mod`, `supported_versions`, `fixed_in` |
| `[[fixes]]` | 고치는 문제 하나: `id`, `since`, `summary`, `crash`(예외·스택 프레임), `upstream`(원작자 이슈·PR 링크 목록) |

`supported_versions` 와 `fixed_in` 은 뜻이 다릅니다.

- `supported_versions`: 이 패치가 맞다고 **우리가 확인한** 대상 모드 버전입니다. 대상 모드가 이 밖의 버전으로 바뀌면
  "미확인"일 뿐 고쳐졌다는 뜻이 아닙니다. 그 모드 업데이트를 보류하고 검토합니다(패치를 빼지 않음).
- `fixed_in`: 원작자가 고친 버전입니다. 원작자 커밋과 재현 시험으로 확인한 버전만 적습니다. 이것만 패치 은퇴 조건입니다.

## 원작자에게 알리기

패치는 임시방편이고, 목표는 원작자 쪽에서 고쳐지는 것입니다. 올리기 전에 지키는 것:

1. **그 저장소의 규칙이 먼저입니다.** `CONTRIBUTING.md`, 이슈·PR 양식, CLA, "질문·제안은 Discord" 같은 안내를 읽고 따릅니다.
   예: Sable 은 PR 을 내면 기여 코드를 PolyForm Shield 로, 나중에 MIT 로도 라이선스할 권리를 원작자에게 주고(CLA 서명),
   본인이 쓴 코드만 낼 수 있습니다.
2. **먼저 찾습니다.** 같은 이슈·PR 이 있으면 새로 만들지 않습니다. 새 정보(다른 호출 경로, 재현 방법, 운영 근거)가 있을 때만
   댓글을 달고, 없으면 반응(👍)만 남깁니다. 같은 수정 PR 이 이미 있으면 우리 PR 을 내지 않습니다.
3. **PR 하나에 문제 하나.** 원래 코드 스타일과 기본 브랜치를 따르고, 문제·변경·시험을 적습니다.
   시험한 범위는 있는 그대로 씁니다(예: "같은 로직의 mixin 으로만 확인, 이 PR 빌드는 직접 돌려 보지 않음").
4. **증상을 덮는 수정은 그렇다고 밝힙니다.** 방어 코드는 PR 부터 내지 않고, 이슈에서 원작자가 원하는 방향을 먼저 묻습니다.
5. **재촉하지 않습니다.** 새 근거가 있을 때 한 번 덧붙이는 정도만 합니다.
6. 크래시 리포트를 붙일 때는 서버 주소·플레이어 이름·경로를 지웁니다. 이 저장소 홍보는 하지 않습니다.

각 패치의 `[[fixes]].upstream` 에 올린 이슈·PR 링크를 적어 둡니다.

## 라이선스

이 저장소의 코드는 [MIT](LICENSE) 입니다. 패치가 고치는 원래 모드는 각자의 라이선스를 따르고,
이 저장소에는 원래 모드의 코드나 jar 가 들어 있지 않습니다(빌드할 때 Modrinth 에서 받음).
