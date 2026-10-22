#!/usr/bin/env python3
"""Prints a compact digest of one part (synopsis, colophon, opening verses) for writing chapter summaries.

Usage: python3 tools/dump_part.py PART_ID [VERSES_PER_CHAPTER]
"""
import json, sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
FILES = ROOT / "shared/src/commonMain/composeResources/files"
pid = int(sys.argv[1]); n = int(sys.argv[2]) if len(sys.argv) > 2 else 2
core = json.loads((FILES / "shivpuran_core.json").read_text())
verses = json.loads((FILES / f"verses/part_{pid:02d}.json").read_text())
by_ch = {}
for v in verses["verses"]:
    by_ch.setdefault(v["chapter"], []).append(v)
for c in core["chapters"]:
    if c["part"] != pid:
        continue
    vs = by_ch.get(c["id"], [])
    speakers = []
    for v in vs:
        if v.get("speaker") and v["speaker"] not in speakers:
            speakers.append(v["speaker"])
    print(f"## {c['number']} ({c['verseCount']}v) {c['titleSanskrit']} | {c['synopsisSanskrit']}")
    print("   colophon:", (c.get("colophon") or "")[-160:])
    print("   speakers:", " / ".join(speakers[:6]))
    for v in vs[:n]:
        print("   >", v["sanskrit"].replace("\n", " ")[:220])
