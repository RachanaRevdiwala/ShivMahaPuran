#!/usr/bin/env python3
"""
Builds the bundled content of the Shiv Maha Puran app.

Inputs (tools/source):
  shivpuran_wikitext.json     - Sanskrit Shiva Mahapurana, Sanskrit Wikisource (public domain)
  parts_i18n.json             - names/descriptions of the 12 parts in en/hi/gu (in-house)
  summaries/part_XX.json      - in-house chapter titles + summaries in en/hi/gu:
                                {"<chapter>": {"title": {en,hi,gu}, "summary": {en,hi,gu}}}
  meanings/part_XX.json       - in-house verse meanings for selected chapters:
                                {"<chapter>": {"<verse>": {en,hi,gu}}}
  library/*.json              - Shiva stotras for the Library (Sanskrit + meanings)

Output (shared/src/commonMain/composeResources/files/):
  shivpuran_core.json         - parts, chapter metadata/summaries, library books
  verses/part_XX.json         - Sanskrit verses of each part (seeded in the background)

The app re-seeds whenever CONTENT_VERSION changes (bump ContentSeeder.BUNDLED_CONTENT_VERSION too).

Usage:  python3 tools/build_content.py
"""
import json
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import parse_shivpuran  # noqa: E402

CONTENT_VERSION = 2
ROOT = Path(__file__).resolve().parent
SRC = ROOT / "source"
FILES = ROOT.parent / "shared/src/commonMain/composeResources/files"
DEV_DIGITS = str.maketrans("0123456789", "०१२३४५६७८९")

ORDINALS = {}  # not needed: colophons are taken from the source text


def load_json(path: Path, default):
    return json.load(open(path, encoding="utf-8")) if path.exists() else default


def main():
    parts = parse_shivpuran.load()
    parts_i18n = load_json(SRC / "parts_i18n.json", {})
    out_parts, out_chapters = [], []
    verse_files = {}
    missing_summaries = []
    chapter_id = 0
    verse_id = 0

    for p in parts:
        pid = p["id"]
        meta = parts_i18n.get(str(pid), {})
        summaries = load_json(SRC / "summaries" / f"part_{pid:02d}.json", {})
        meanings = load_json(SRC / "meanings" / f"part_{pid:02d}.json", {})
        out_parts.append({
            "id": pid, "samhita": p["samhita"], "khanda": p["khanda"],
            "nameSanskrit": p["nameSanskrit"],
            "name": meta.get("name", {"en": p["nameSanskrit"], "hi": p["nameSanskrit"], "gu": p["nameSanskrit"]}),
            "description": meta.get("description", {"en": "", "hi": "", "gu": ""}),
            "chapterCount": len(p["chapters"]),
        })
        part_verses = []
        for c in p["chapters"]:
            chapter_id += 1
            s = summaries.get(str(c["number"]), {})
            if not s.get("summary"):
                missing_summaries.append(f"{pid}.{c['number']}")
            chapter_meanings = meanings.get(str(c["number"]), {})
            out_chapters.append({
                "id": chapter_id, "part": pid, "number": c["number"],
                "titleSanskrit": c["title"],
                "synopsisSanskrit": c["synopsis"],
                "title": s.get("title", {}),
                "summary": s.get("summary", {}),
                "verseCount": len(c["verses"]),
                "colophon": c["colophon"],
                "hasMeanings": bool(chapter_meanings),
            })
            for n, v in enumerate(c["verses"], start=1):
                verse_id += 1
                item = {"id": verse_id, "chapter": chapter_id, "number": n, "sanskrit": v["text"]}
                if v["speaker"]:
                    item["speaker"] = v["speaker"]
                if str(n) in chapter_meanings:
                    item["meaning"] = chapter_meanings[str(n)]
                part_verses.append(item)
        verse_files[pid] = part_verses

    books = []
    for f in sorted((SRC / "library").glob("*.json")):
        books.append(json.load(open(f, encoding="utf-8")))
    books.sort(key=lambda b: b.get("order", 99))

    # Integrity checks — fail loudly rather than ship broken scripture.
    assert len(out_chapters) == 457, f"expected 457 chapters, got {len(out_chapters)}"
    assert all(c["verseCount"] > 0 for c in out_chapters), "a chapter has no verses"
    for c in out_chapters:
        for lang in ("en", "hi", "gu"):
            if c["summary"]:
                assert c["summary"].get(lang), f"chapter {c['part']}.{c['number']} summary missing {lang}"
                assert c["title"].get(lang), f"chapter {c['part']}.{c['number']} title missing {lang}"

    FILES.mkdir(parents=True, exist_ok=True)
    (FILES / "verses").mkdir(exist_ok=True)
    core = {
        "contentVersion": CONTENT_VERSION,
        "source": "Sanskrit text: Shiva Mahapurana, Sanskrit Wikisource (public domain). "
                  "Titles, summaries, part descriptions, verse meanings and stotra meanings: in-house.",
        "parts": out_parts, "chapters": out_chapters, "books": books,
    }
    with open(FILES / "shivpuran_core.json", "w", encoding="utf-8") as f:
        json.dump(core, f, ensure_ascii=False, indent=1)
    total = 0
    for pid, verses in verse_files.items():
        out = FILES / "verses" / f"part_{pid:02d}.json"
        with open(out, "w", encoding="utf-8") as f:
            json.dump({"contentVersion": CONTENT_VERSION, "part": pid, "verses": verses}, f, ensure_ascii=False)
        total += out.stat().st_size
    print(f"Wrote shivpuran_core.json ({(FILES / 'shivpuran_core.json').stat().st_size / 1e6:.2f} MB): "
          f"{len(out_parts)} parts, {len(out_chapters)} chapters, {len(books)} books")
    print(f"Wrote verses/ ({total / 1e6:.2f} MB): {verse_id} verses")
    if missing_summaries:
        print(f"NOTE: {len(missing_summaries)} chapters have no summary yet (app shows the Sanskrit synopsis): "
              f"{', '.join(missing_summaries[:8])}{' …' if len(missing_summaries) > 8 else ''}", file=sys.stderr)


if __name__ == "__main__":
    main()
