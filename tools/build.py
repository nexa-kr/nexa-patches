#!/usr/bin/env python3
"""patches/<id>/patch.toml 을 읽어 패치 jar 를 만든다(gradle 없이 javac).

    python3 tools/build.py sable-stepfix          # -> patches/sable-stepfix/build/<mod_id>-<version>.jar
    python3 tools/build.py --all                  # status = "active" 인 패치 전부

클래스패스는 공개 원본에서만 받는다.
- NeoForge 서버 설치(maven.neoforged.net 설치 프로그램 --install-server, 바닐라 서버는 Mojang 에서 받음)의 libraries/
- Modrinth 모드 jar(api.modrinth.com, sha512 확인)
받은 것은 캐시(NEXA_PATCHES_CACHE, 기본 ~/.cache/nexa-patches)에 둔다. 서버나 비밀값은 쓰지 않는다.
필요: python 3.11+, JDK(JAVA_HOME 또는 PATH 의 javac·jar·java, 버전은 patch.toml build.java 이상).
"""
from __future__ import annotations

import argparse
import hashlib
import json
import os
import re
import shutil
import subprocess
import sys
import tomllib
import urllib.parse
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
CACHE = Path(os.environ.get("NEXA_PATCHES_CACHE", Path.home() / ".cache" / "nexa-patches"))
UA = "nexa-kr/nexa-patches (https://github.com/nexa-kr/nexa-patches)"
NEOFORGE_MAVEN = "https://maven.neoforged.net/releases/net/neoforged/neoforge"


def fail(msg: str) -> None:
    print(f"오류: {msg}", file=sys.stderr)
    sys.exit(1)


def jdk_tool(name: str) -> str:
    home = os.environ.get("JAVA_HOME")
    if home and (Path(home) / "bin" / name).exists():
        return str(Path(home) / "bin" / name)
    found = shutil.which(name)
    if not found:
        fail(f"{name} 를 찾지 못했어요(JAVA_HOME 이나 PATH 확인)")
    return found


def fetch(url: str, dest: Path) -> None:
    dest.parent.mkdir(parents=True, exist_ok=True)
    tmp = dest.with_suffix(dest.suffix + ".part")
    req = urllib.request.Request(url, headers={"User-Agent": UA})
    with urllib.request.urlopen(req, timeout=120) as res, open(tmp, "wb") as out:
        shutil.copyfileobj(res, out)
    tmp.replace(dest)


def neoforge_server(version: str) -> Path:
    """설치 프로그램으로 서버를 한 번 설치해 두고 그 디렉터리를 돌려준다."""
    target = CACHE / "neoforge" / version
    marker = target / ".installed"
    if marker.exists():
        return target
    installer = CACHE / "installers" / f"neoforge-{version}-installer.jar"
    if not installer.exists():
        fetch(f"{NEOFORGE_MAVEN}/{version}/neoforge-{version}-installer.jar", installer)
    if target.exists():
        shutil.rmtree(target)
    target.mkdir(parents=True)
    print(f"NeoForge {version} 서버 설치(처음 한 번) ...", flush=True)
    subprocess.run(
        [jdk_tool("java"), "-jar", str(installer), "--install-server", str(target)],
        check=True,
        stdout=subprocess.DEVNULL,
        cwd=installer.parent,  # 설치 프로그램이 현재 디렉터리에 .log 를 남긴다
    )
    marker.write_text(version)
    return target


def modrinth_jar(project: str, version_number: str, loader: str, minecraft: str) -> Path:
    dest = CACHE / "modrinth" / project / f"{version_number}.jar"
    if dest.exists():
        return dest
    q = urllib.parse.urlencode({"loaders": json.dumps([loader]), "game_versions": json.dumps([minecraft])})
    req = urllib.request.Request(f"https://api.modrinth.com/v2/project/{project}/version?{q}", headers={"User-Agent": UA})
    with urllib.request.urlopen(req, timeout=60) as res:
        versions = json.load(res)
    match = next((v for v in versions if v["version_number"] == version_number), None)
    if match is None:
        fail(f"Modrinth {project} {version_number} ({loader} {minecraft}) 버전이 없어요")
    file = next((f for f in match["files"] if f.get("primary")), match["files"][0])
    fetch(file["url"], dest)
    got = hashlib.sha512(dest.read_bytes()).hexdigest()
    if got != file["hashes"]["sha512"]:
        dest.unlink()
        fail(f"Modrinth {project} {version_number} sha512 가 맞지 않아요")
    return dest


def mods_toml_version(src: Path) -> str:
    text = (src / "main/resources/META-INF/neoforge.mods.toml").read_text()
    m = re.search(r'^version\s*=\s*"([^"]+)"', text, re.M)
    if not m:
        fail("neoforge.mods.toml 에 version 이 없어요")
    return m.group(1)


def build(patch_id: str) -> Path:
    pdir = ROOT / "patches" / patch_id
    meta = tomllib.loads((pdir / "patch.toml").read_text())
    if meta.get("status") != "active":
        fail(f"{patch_id} 는 status = {meta.get('status')!r} 라 빌드하지 않아요")
    src = pdir / "src"
    version = meta["version"]
    if mods_toml_version(src) != version:
        fail(f"patch.toml version {version} 과 neoforge.mods.toml version 이 달라요")

    b = meta["build"]
    server = neoforge_server(b["neoforge"])
    cp = []
    for rel in b.get("libraries", []):
        jar = server / "libraries" / rel
        if not jar.exists():
            fail(f"NeoForge 설치에 {rel} 이 없어요")
        cp.append(jar)
    for dep in b.get("modrinth", []):
        cp.append(modrinth_jar(dep["project"], dep["version"], meta["loader"], meta["minecraft"]))

    out = pdir / "build"
    if out.exists():
        shutil.rmtree(out)
    classes = out / "classes"
    classes.mkdir(parents=True)
    sources = sorted(str(p) for p in (src / "main/java").rglob("*.java"))
    subprocess.run(
        [jdk_tool("javac"), "--release", str(b["java"]), "-proc:none", "-nowarn",
         "-cp", os.pathsep.join(map(str, cp)), "-d", str(classes), *sources],
        check=True,
    )
    shutil.copytree(src / "main/resources", classes, dirs_exist_ok=True)
    jar = out / f"{meta['mod_id']}-{version}.jar"
    # 같은 원본이면 같은 jar 가 나오게 항목 시각을 고정한다.
    subprocess.run([jdk_tool("jar"), "--create", "--date=2000-01-01T00:00:00Z", "--file", str(jar), "-C", str(classes), "."], check=True)
    digest = hashlib.sha256(jar.read_bytes()).hexdigest()
    print(f"{digest}  {jar.relative_to(ROOT)}")
    return jar


def main() -> None:
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("patch", nargs="?", help="patches/ 아래 패치 id")
    ap.add_argument("--all", action="store_true", help="status = active 인 패치 전부")
    args = ap.parse_args()
    if args.all:
        ids = [p.parent.name for p in sorted((ROOT / "patches").glob("*/patch.toml"))
               if tomllib.loads(p.read_text()).get("status") == "active"]
    elif args.patch:
        ids = [args.patch]
    else:
        ap.error("패치 id 나 --all 을 주세요")
    for pid in ids:
        build(pid)


if __name__ == "__main__":
    main()
