#!/usr/bin/env python3
"""
Parses the Sanskrit Shiva Mahapurana (Sanskrit Wikisource, public domain) from
tools/source/shivpuran_wikitext.json into a structured form:

  parts (samhita / khanda)  ->  chapters (adhyaya)  ->  verses (shloka)

For every chapter it also extracts the Sanskrit chapter title from the closing colophon
("… मुनिप्रश्नवर्णनो नाम प्रथमोऽध्यायः") and the one-line Sanskrit synopsis from the
part's contents page (विषयानुक्रमणिका).

Usage:  python3 tools/parse_shivpuran.py        (prints a coverage report)
"""
import json
import re
from pathlib import Path

SRC = Path(__file__).resolve().parent / "source/shivpuran_wikitext.json"
DEV_DIGITS = str.maketrans("०१२३४५६७८९", "0123456789")
PREFIX = "शिवपुराणम्/"

# (part id, samhita, khanda, wikisource path, expected chapters, Sanskrit name)
PARTS = [
    (1, 1, 0, "संहिता १ (विश्वेश्वरसंहिता)", 25, "विद्येश्वरसंहिता"),
    (2, 2, 1, "संहिता २ (रुद्रसंहिता)/खण्डः १ (सृष्टिखण्डः)", 20, "रुद्रसंहिता · सृष्टिखण्डः"),
    (3, 2, 2, "संहिता २ (रुद्रसंहिता)/खण्डः २ (सतीखण्डः)", 43, "रुद्रसंहिता · सतीखण्डः"),
    (4, 2, 3, "संहिता २ (रुद्रसंहिता)/खण्डः ३ (पार्वतीखण्डः)", 55, "रुद्रसंहिता · पार्वतीखण्डः"),
    (5, 2, 4, "संहिता २ (रुद्रसंहिता)/खण्डः ४ (कुमारखण्डः)", 20, "रुद्रसंहिता · कुमारखण्डः"),
    (6, 2, 5, "संहिता २ (रुद्रसंहिता)/खण्डः ५ (युद्धखण्डः)", 59, "रुद्रसंहिता · युद्धखण्डः"),
    (7, 3, 0, "संहिता ३ (शतरुद्रसंहिता)", 42, "शतरुद्रसंहिता"),
    (8, 4, 0, "संहिता ४ (कोटिरुद्रसंहिता)", 43, "कोटिरुद्रसंहिता"),
    (9, 5, 0, "संहिता ५ (उमासंहिता)", 51, "उमासंहिता"),
    (10, 6, 0, "संहिता ६ (कैलाससंहिता)", 23, "कैलाससंहिता"),
    (11, 7, 1, "संहिता ७ (वायवीयसंहिता)/पूर्व भागः", 35, "वायवीयसंहिता · पूर्वभागः"),
    (12, 7, 2, "संहिता ७ (वायवीयसंहिता)/उत्तर भागः", 41, "वायवीयसंहिता · उत्तरभागः"),
]

# A line that ends a verse: "... १० ।", "... 1.1.१० ।", "।। १ ।।", "॥ १,७.३०", "॥ २.१.५ ॥"
VERSE_END = re.compile(r"(?:[।॥|]{0,2}\s*)(?:[0-9०-९]+[.,])*([0-9०-९]+)\s*[।॥|]*\s*$")
SPEAKER = re.compile(r"^[।॥\s]*(?:[^\s।॥]+\s+){0,2}[^\s।॥]*(?:वाच|ऊचुः|ऊचतुः|ऊचे|उवाचतुः)\s*[।॥:ः]*\s*$")
COLOPHON = re.compile(r"इति\s*श्री.*?ध्यायः")
INVOCATION = re.compile(r"^(श्रीगणेशाय|श्री\s*गणेशाय|श्रीगुरुभ्यो|श्रिसरस्वत्यै|अथ |श्रीशिवमहापुराणम्|ॐ नमः)")


def _strip_markup(text: str) -> str:
    text = re.sub(r"<[^>]+>", "\n", text)
    text = re.sub(r"\[\[[^\]|]*\|([^\]]*)\]\]", r"\1", text)
    text = re.sub(r"\[\[([^\]]*)\]\]", r"\1", text)
    text = re.sub(r"\{\{[^{}]*\}\}", "", text)
    text = re.sub(r"'{2,}", "", text)
    text = text.replace("‍", "").replace("‌", "").replace("\xa0", " ")
    return text


def _clean_verse(lines: list[str]) -> str:
    out = []
    for line in lines:
        line = re.sub(r"\s*(?:[0-9०-९]+[.,])+[0-9०-९]+\s*", " ", line)   # inline references like 1.1.१०
        line = re.sub(r"\s*[।॥|]{1,2}\s*[0-9०-९]+\s*[।॥|]{0,2}\s*$", "", line)
        line = re.sub(r"\s+[0-9०-९]+\s*[।॥|]*\s*$", "", line)
        line = re.sub(r"\s+", " ", line).strip(" ।॥|")
        if line:
            out.append(line + " ।")
    if out:
        out[-1] = out[-1][:-2] + " ॥"
    return "\n".join(out)


def parse_chapter(wikitext: str):
    body = wikitext
    m = re.search(r"<poem>(.*)</poem>", wikitext, re.S)
    if m:
        body = m.group(1)
    text = _strip_markup(body)
    colophon = ""
    cm = COLOPHON.search(text.replace("\n", " "))
    if cm:
        colophon = re.sub(r"\s+", " ", cm.group(0)).strip()
    lines = [re.sub(r"\s+", " ", l).strip() for l in text.split("\n")]
    lines = [l for l in lines if l and l not in ("।", "॥")]

    verses, pending, speaker = [], [], None
    for line in lines:
        if line.startswith("इति श्री") or line.startswith("इति श्रीशिव"):
            break
        if INVOCATION.match(line) and not verses and not pending:
            continue
        if SPEAKER.match(line):
            speaker = re.sub(r"[।॥:\s]+$", "", re.sub(r"^[।॥\s]+", "", line))
            continue
        pending.append(line)
        m = VERSE_END.search(line)
        has_number = m is not None and re.search(r"[0-9०-९]\s*[।॥|]*\s*$", line) is not None
        number = int(m.group(1).translate(DEV_DIGITS)) if has_number else None
        # Some chapters number each half-verse with the full verse reference ("॥ ७.१,२.१" twice):
        # a one-line verse followed by a line with the same number is the same shloka.
        if (has_number and len(pending) == 1 and verses and verses[-1]["number"] == number
                and verses[-1]["lines"] == 1 and speaker is None):
            verses[-1]["raw"].append(line)
            verses[-1]["lines"] = 2
            verses[-1]["text"] = _clean_verse(verses[-1]["raw"])
            pending = []
            continue
        if has_number or len(pending) >= 4:
            verse = _clean_verse(pending)
            if verse:
                verses.append({"speaker": speaker, "text": verse, "number": number, "lines": len(pending), "raw": list(pending)})
            pending, speaker = [], None
    if pending:
        verse = _clean_verse(pending)
        if verse:
            verses.append({"speaker": speaker, "text": verse, "number": None, "lines": len(pending), "raw": pending})
    return [{"speaker": v["speaker"], "text": v["text"]} for v in verses], colophon


def title_from_colophon(colophon: str) -> str:
    m = re.search(r"([^\s]+?)\s*नाम\s", colophon + " ")
    if not m:
        return ""
    word = m.group(1)
    word = re.sub(r"ो$", "म्", word) if word.endswith("ो") else word   # "...वर्णनो" -> "...वर्णनम्"
    return word


def parse_toc(wikitext: str) -> dict:
    text = _strip_markup(wikitext)
    synopsis = {}
    for raw in re.split(r"\n\s*\n", text):
        line = re.sub(r"\s+", " ", raw).strip()
        m = re.match(r"^([०-९0-9]+)\s+(.+)$", line)
        if m:
            n = int(m.group(1).translate(DEV_DIGITS))
            synopsis[n] = re.sub(r"\s*-\s+", "", m.group(2)).strip(" ।")
    return synopsis


def load() -> list[dict]:
    pages = json.load(open(SRC, encoding="utf-8"))
    parts = []
    for pid, samhita, khanda, path, expected, name in PARTS:
        toc = parse_toc(pages.get(PREFIX + path + "/विषयानुक्रमणिका", ""))
        chapters = []
        for n in range(1, expected + 1):
            key = f"{PREFIX}{path}/अध्यायः {str(n).zfill(2).translate(str.maketrans('0123456789', '०१२३४५६७८९'))}"
            wikitext = pages.get(key)
            if wikitext is None:
                chapters.append({"number": n, "missing": True, "verses": [], "colophon": "", "title": "", "synopsis": toc.get(n, "")})
                continue
            verses, colophon = parse_chapter(wikitext)
            chapters.append({
                "number": n, "verses": verses, "colophon": colophon,
                "title": title_from_colophon(colophon), "synopsis": toc.get(n, ""),
            })
        parts.append({"id": pid, "samhita": samhita, "khanda": khanda, "nameSanskrit": name, "chapters": chapters})
    return parts


if __name__ == "__main__":
    parts = load()
    total_ch = total_v = 0
    for p in parts:
        vs = [len(c["verses"]) for c in p["chapters"]]
        missing = [c["number"] for c in p["chapters"] if c.get("missing")]
        titles = sum(1 for c in p["chapters"] if c["title"])
        syn = sum(1 for c in p["chapters"] if c["synopsis"])
        thin = [c["number"] for c in p["chapters"] if 0 < len(c["verses"]) < 8]
        total_ch += len(p["chapters"]); total_v += sum(vs)
        print(f"{p['id']:2d} {p['nameSanskrit'][:24]:24s} ch={len(p['chapters']):3d} verses={sum(vs):5d} "
              f"titles={titles:3d} synopsis={syn:3d} missing={missing} thin={thin[:6]}")
    print("TOTAL chapters", total_ch, "verses", total_v)
