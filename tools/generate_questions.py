#!/usr/bin/env python3
"""Generate a bilingual (EN/TR) question bank from the IFAB Laws text with Claude.

Usage (done for you by the GitHub Action "Generate questions"):
  ANTHROPIC_API_KEY=... python3 tools/generate_questions.py --laws tools/laws.txt --count 1000

Output: app/src/main/assets/questions.json  (the app loads it at startup)
IMPORTANT: AI-written questions can contain mistakes. Spot-check them before publishing.
"""
import argparse, json, os, random, re, time, urllib.request

MODEL = os.environ.get("MODEL", "claude-sonnet-5-5")
KEY = os.environ["ANTHROPIC_API_KEY"]
OUT = "app/src/main/assets/questions.json"

SYSTEM = """You are an expert IFAB referee instructor and a bilingual (English/Turkish) quiz writer.
From the Laws-of-the-Game text I give you, write {n} multiple-choice questions.
Rules:
- Use ONLY facts stated in the supplied text. Prefer match-situation questions ("A defender ... Decision?").
- 4 options, exactly one correct, plausible distractors. Put the correct option at a RANDOM index.
- cat: 0 Offside (Law 11), 1 Fouls & misconduct (Law 12), 2 Restarts (Laws 8,13,15,16,17),
  3 Penalties (Laws 14 and shoot-outs), 4 Basics (Laws 1-7, 9, 10), 5 Changes new in 2026/27.
- diff: 1 easy, 2 medium, 3 hard.
- Turkish must use correct football terms: ofsayt, direkt/endirekt serbest vuruş, penaltı atışı, köşe vuruşu,
  kale vuruşu, taç atışı, hakem atışı, sarı kart, kırmızı kart.
- wEn / wTr: one or two sentences explaining the correct answer.
Return ONLY a JSON array of objects with keys: cat, diff, qEn, qTr, oEn, oTr, ans, wEn, wTr."""


def call(text, n):
    body = json.dumps({
        "model": MODEL, "max_tokens": 8000,
        "system": SYSTEM.format(n=n),
        "messages": [{"role": "user", "content": "LAWS TEXT:\n" + text}],
    }).encode()
    req = urllib.request.Request("https://api.anthropic.com/v1/messages", data=body, headers={
        "x-api-key": KEY, "anthropic-version": "2023-06-01", "content-type": "application/json"})
    with urllib.request.urlopen(req, timeout=180) as r:
        data = json.load(r)
    raw = data["content"][0]["text"]
    m = re.search(r"\[[\s\S]*\]", raw)
    return json.loads(m.group(0)) if m else []


VERIFY = """You are a strict IFAB referee examiner. You get a LAWS TEXT and a numbered JSON array of quiz questions.
Return ONLY a JSON array with the 0-based indexes of the questions that pass ALL checks:
1) the marked correct answer ("ans") is unambiguously correct according to the LAWS TEXT,
2) every other option is clearly wrong,
3) the Turkish text says the same thing as the English text and uses correct football terms.
Drop anything doubtful. Example output: [0,2,3]"""


def verify(text, items):
    """Second pass: a separate call checks the questions against the Laws text. On any error nothing is accepted."""
    if not items:
        return []
    body = json.dumps({
        "model": MODEL, "max_tokens": 600,
        "system": VERIFY,
        "messages": [{"role": "user", "content": "LAWS TEXT:\n" + text + "\n\nQUESTIONS:\n" + json.dumps(items, ensure_ascii=False)}],
    }).encode()
    req = urllib.request.Request("https://api.anthropic.com/v1/messages", data=body, headers={
        "x-api-key": KEY, "anthropic-version": "2023-06-01", "content-type": "application/json"})
    try:
        with urllib.request.urlopen(req, timeout=180) as r:
            raw = json.load(r)["content"][0]["text"]
        m = re.search(r"\[[\s\S]*?\]", raw)
        idx = json.loads(m.group(0)) if m else []
        return [items[i] for i in idx if isinstance(i, int) and 0 <= i < len(items)]
    except Exception as e:
        print("verify failed, dropping this batch:", e)
        return []


def valid(q):
    try:
        return (len(q["oEn"]) == 4 and len(q["oTr"]) == 4 and 0 <= int(q["ans"]) < 4
                and q["qEn"].strip() and q["qTr"].strip() and 0 <= int(q["cat"]) <= 5)
    except Exception:
        return False


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--laws", default="tools/laws.txt")
    ap.add_argument("--count", type=int, default=300)
    ap.add_argument("--per-call", type=int, default=10)
    ap.add_argument("--no-verify", action="store_true", help="skip the second checking pass (cheaper, less reliable)")
    a = ap.parse_args()

    text = open(a.laws, encoding="utf-8", errors="ignore").read()
    size = 6000
    chunks = [text[i:i + size] for i in range(0, len(text), size) if text[i:i + size].strip()]
    bank, seen = [], set()
    if os.path.exists(OUT):
        bank = json.load(open(OUT, encoding="utf-8"))
        seen = {q["qEn"].lower() for q in bank}
    rounds = 0
    while len(bank) < a.count and rounds < len(chunks) * 4:
        chunk = chunks[rounds % len(chunks)] if rounds < len(chunks) else random.choice(chunks)
        rounds += 1
        try:
            items = call(chunk, a.per_call)
        except Exception as e:
            print("retry after error:", e)
            time.sleep(5)
            continue
        items = [q for q in items if valid(q)]
        if not a.no_verify:
            items = verify(chunk, items)
        for q in items:
            if valid(q) and q["qEn"].lower() not in seen:
                seen.add(q["qEn"].lower())
                q["id"] = "j%d" % (len(bank) + 1)
                q["diff"] = max(1, min(3, int(q.get("diff", 2))))
                bank.append(q)
        os.makedirs(os.path.dirname(OUT), exist_ok=True)
        json.dump(bank, open(OUT, "w", encoding="utf-8"), ensure_ascii=False, indent=1)
        print(len(bank), "questions")


main()
