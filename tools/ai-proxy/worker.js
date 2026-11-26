// Cloudflare Worker: keeps your Anthropic API key OFF the phone.
// Deploy: Cloudflare dashboard -> Workers -> Create -> paste this file.
// Settings -> Variables: ANTHROPIC_KEY (secret), APP_TOKEN (secret, any random string).
// Then put the worker URL and the same token into AiConfig in Services.kt.
const SYSTEM = `You rewrite football (soccer) Laws-of-the-Game quiz questions into a fresh variant.
Rules:
- Keep the SAME Law and the SAME correct decision (IFAB Laws of the Game 2026/27). Never change the rule being tested.
- Change the scenario details: players, teams, minute, field position, wording.
- Keep the same number of options. Exactly one option is correct and clearly correct under the Laws.
- Shuffle the option order and set "ans" to the index of the correct option.
- Provide both English (qEn,oEn,wEn) and Turkish (qTr,oTr,wTr). The Turkish must use correct football terms
  (ofsayt, direkt/endirekt serbest vuruş, penaltı atışı, köşe vuruşu, kale vuruşu, taç atışı, hakem atışı, sarı/kırmızı kart).
- wEn/wTr: one or two sentences explaining the correct answer.
- Output ONLY one JSON object with keys: qEn,qTr,oEn,oTr,ans,wEn,wTr. No markdown.`;

export default {
  async fetch(request, env) {
    if (request.method !== "POST") return new Response("POST only", { status: 405 });
    if (env.APP_TOKEN && request.headers.get("Authorization") !== `Bearer ${env.APP_TOKEN}`) {
      return new Response("Unauthorized", { status: 401 });
    }
    const len = Number(request.headers.get("content-length") || 0);
    if (len > 6000) return new Response("Too large", { status: 413 });
    let q;
    try { q = await request.json(); } catch { return new Response("Bad JSON", { status: 400 }); }
    if (!q || !Array.isArray(q.oEn) || !Array.isArray(q.oTr) || typeof q.qEn !== "string") {
      return new Response("Bad request", { status: 400 });
    }
    const base = { qEn: q.qEn, qTr: q.qTr, oEn: q.oEn, oTr: q.oTr, ans: q.ans, wEn: q.wEn, wTr: q.wTr };
    const r = await fetch("https://api.anthropic.com/v1/messages", {
      method: "POST",
      headers: {
        "x-api-key": env.ANTHROPIC_KEY,
        "anthropic-version": "2023-06-01",
        "content-type": "application/json",
      },
      body: JSON.stringify({
        model: "claude-haiku-5-5",
        max_tokens: 900,
        system: SYSTEM,
        messages: [{ role: "user", content: "Base question:\n" + JSON.stringify(base) }],
      }),
    });
    if (!r.ok) return new Response("Upstream error", { status: 502 });
    const data = await r.json();
    const text = (data.content && data.content[0] && data.content[0].text) || "";
    const m = text.match(/\{[\s\S]*\}/);
    if (!m) return new Response("No JSON", { status: 502 });
    // Validate the model output before it reaches the phone.
    let out;
    try { out = JSON.parse(m[0]); } catch { return new Response("Bad model JSON", { status: 502 }); }
    const ok = out && Array.isArray(out.oEn) && Array.isArray(out.oTr) &&
      out.oEn.length === out.oTr.length && out.oEn.length === q.oEn.length &&
      Number.isInteger(out.ans) && out.ans >= 0 && out.ans < out.oEn.length &&
      typeof out.qEn === "string" && typeof out.qTr === "string" && out.qEn && out.qTr;
    if (!ok) return new Response("Invalid variant", { status: 502 });
    return new Response(JSON.stringify(out), { headers: { "Content-Type": "application/json" } });
  },
};
