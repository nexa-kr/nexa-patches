# nexa-patches

[NEXA](https://nexa.kr) 마인크래프트 서버를 운영하다 만난 모드 버그를 고치는 작은 NeoForge 모드들입니다.
원작자에게도 이슈나 PR 로 알리고, 원작자 쪽에서 고쳐지면 패치는 내립니다.

Small NeoForge mods that patch crashes we hit on NEXA servers. Every fix is reported upstream,
and a patch is retired once the upstream release fixes it.

| 패치 | 상태 | 설치 | 대상 |
|---|---|---|---|
| [sable-stepfix](patches/sable-stepfix) | 사용 중 | 서버만 | Sable 2.0.6 (MC 1.21.1) |
| [elytraslot-deeperdarker](patches/elytraslot-deeperdarker) | 보류 | 서버·클라이언트 | Elytra Slot 9.0.2 + Deeper Darker 1.4.1 |

받기: [Releases](https://github.com/nexa-kr/nexa-patches/releases). 버전별 변경은 각 패치의 `CHANGELOG.md` 에 있습니다.

## 빌드

```bash
python3 tools/build.py sable-stepfix   # patches/sable-stepfix/build/*.jar
python3 tools/build.py --all           # 사용 중(active)인 패치 전부
```

Python 3.11 이상과 JDK 21 이 필요합니다. gradle 은 쓰지 않습니다. 클래스패스는 NeoForge 설치 프로그램으로 깐 서버와
Modrinth 에서 받은 모드 jar(sha512 확인)로 만들고, `~/.cache/nexa-patches` 에 캐시합니다.

## 패치 구성

```
patches/<id>/
  patch.toml     대상 모드와 확인한 버전, 고치는 크래시, 원작자 이슈·PR
  CHANGELOG.md   릴리스한 패치만
  src/main/      mixin 소스
```

`patch.toml` 의 `supported_versions` 는 우리가 이 패치로 확인한 대상 모드 버전이고, `fixed_in` 은 원작자가 고친 버전입니다.
대상 모드가 `supported_versions` 밖으로 올라가면 고쳐졌다고 보지 않고, 확인할 때까지 그 업데이트를 미룹니다.
패치를 내리는 건 `fixed_in` 이 채워졌을 때뿐입니다.

## 릴리스

`patch.toml` 과 `neoforge.mods.toml` 의 버전을 올리고 `CHANGELOG.md` 에 적은 뒤 `<id>/v<버전>` 태그를 올리면
CI 가 빌드해서 jar 와 `.sha256` 을 릴리스에 붙입니다.

## 원작자에게 알릴 때

그 저장소의 CONTRIBUTING·이슈 양식·CLA 를 먼저 따릅니다. 같은 이슈나 PR 이 이미 있으면 새로 만들지 않고,
새로 보탤 정보가 있을 때만 댓글을 답니다. 증상만 막는 수정이면 그렇다고 밝히고 원작자가 원하는 방향부터 묻습니다.

## 라이선스

[MIT](LICENSE). 원래 모드의 코드나 jar 는 이 저장소에 들어 있지 않습니다.
