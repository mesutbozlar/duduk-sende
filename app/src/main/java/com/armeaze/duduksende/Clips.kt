package com.armeaze.duduksende

import kotlin.random.Random

/*
 * ANİMASYONLU VİDEO POZİSYONLAR
 * -----------------------------
 * Gerçek maç görüntüsü kullanılmaz (telif). Pozisyonlar saha ölçülerinde (105 x 68 m) koordinat
 * "anahtar kareleri" olarak tanımlanır ve kodla çizilir. Hücum her zaman x = 105 (rakip kale) yönündedir.
 * Aynı veri 3 farklı kameradan çizilebildiği için VAR görünümü (çoklu açı) bedavaya gelir.
 * İleride gerçek video eklenecekse: VQ'ya videoUrl alanı eklenip bu oynatıcının yerine platform oynatıcı konur.
 */

/** Bir anahtar kare: t saniye, (x, y) metre. x: kale çizgisine doğru (0..105), y: genişlik (0..68). */
class Key(val t: Float, val x: Float, val y: Float)

/** team: 0 = hücum (mavi), 1 = savunma (kırmızı), 2 = savunma kalecisi (sarı), 3 = hakem (siyah). downAt: yere düştüğü an. */
class Track(val team: Int, val keys: List<Key>, val downAt: Float = -1f)

class Clip(
    val duration: Float,
    val x0: Float,
    val tracks: List<Track>,
    val ball: List<Key>,
    val keyT: Float,
    val offside: Boolean
)

/** Video sorusu. */
class VQ(
    val clipId: String, val diff: Int,
    val qEn: String, val qTr: String,
    val oEn: List<String>, val oTr: List<String>, val ans: Int,
    val wEn: String, val wTr: String
) {
    fun q(tr: Boolean) = if (tr) qTr else qEn
    fun o(tr: Boolean) = if (tr) oTr else oEn
    fun w(tr: Boolean) = if (tr) wTr else wEn
}

private fun k(t: Double, x: Double, y: Double) = Key(t.toFloat(), x.toFloat(), y.toFloat())
private fun stay(team: Int, x: Double, y: Double) = Track(team, listOf(k(0.0, x, y)))
private fun trk(team: Int, vararg keys: Key) = Track(team, keys.toList())
private fun down(team: Int, at: Double, vararg keys: Key) = Track(team, keys.toList(), at.toFloat())

object VideoBank {

    // Ofsayt çizgisini veren ortak savunma dizilişi: sondan ikinci rakip x = 91.0 (kaleci 103.5'te).
    private fun backLine(): List<Track> = listOf(
        stay(1, 91.0, 40.0), stay(1, 90.2, 24.0), stay(1, 89.5, 48.0), stay(1, 89.0, 12.0)
    )

    /** Sahne 1-2-5: aynı pas, hücumcunun pas anındaki x konumu değişir. */
    private fun offsideClip(aAtPass: Double, aStartX: Double): Clip = Clip(
        duration = 6.5f, x0 = 60f, keyT = 2.0f, offside = true,
        tracks = backLine() + listOf(
            trk(2, k(0.0, 103.5, 34.0), k(4.3, 103.5, 34.0), k(5.2, 103.5, 37.5)),
            trk(0, k(0.0, 64.0, 38.0), k(2.0, 68.0, 38.0), k(6.5, 70.0, 38.0)),
            trk(0, k(0.0, aStartX, 26.0), k(2.0, aAtPass, 29.0), k(3.4, 99.0, 31.0), k(4.4, 99.4, 31.2), k(6.5, 100.0, 31.0)),
            stay(0, 72.0, 52.0),
            trk(3, k(0.0, 76.0, 56.0), k(6.5, 90.0, 56.0))
        ),
        ball = listOf(
            k(0.0, 64.8, 38.0), k(2.0, 68.6, 38.0), k(3.4, 99.0, 31.0), k(4.3, 99.6, 31.0),
            k(5.1, 105.6, 35.0), k(5.6, 106.3, 35.6), k(6.5, 106.3, 35.6)
        )
    )

    private val clips: Map<String, Clip> = mapOf(
        // Ofsayt: hücumcu pas anında çizginin 1,4 m önünde
        "o1" to offsideClip(aAtPass = 92.4, aStartX = 86.0),
        // Ofsayt değil: pas anında çizginin 1,4 m gerisinde
        "o2" to offsideClip(aAtPass = 89.6, aStartX = 84.0),
        // Zor ofsayt: sadece 0,7 m önde
        "o5" to offsideClip(aAtPass = 91.7, aStartX = 84.0),

        // Ofsayt pozisyonundaki oyuncu oyuna karışmıyor, gol atan oyuncu ofsayt değil
        "o3" to Clip(
            duration = 6.5f, x0 = 60f, keyT = 2.0f, offside = true,
            tracks = backLine() + listOf(
                trk(2, k(0.0, 103.5, 34.0), k(4.4, 103.5, 34.0), k(5.2, 103.5, 31.0)),
                trk(0, k(0.0, 64.0, 38.0), k(2.0, 68.0, 38.0), k(6.5, 70.0, 38.0)),
                trk(0, k(0.0, 94.0, 8.0), k(6.5, 95.5, 6.0)),
                trk(0, k(0.0, 80.0, 40.0), k(2.0, 88.0, 38.5), k(3.4, 98.0, 36.0), k(4.4, 98.5, 36.0), k(6.5, 99.0, 36.0)),
                trk(3, k(0.0, 76.0, 56.0), k(6.5, 90.0, 56.0))
            ),
            ball = listOf(
                k(0.0, 64.8, 38.0), k(2.0, 68.6, 38.0), k(3.4, 98.0, 36.0), k(4.4, 98.6, 36.0),
                k(5.2, 105.6, 33.5), k(5.7, 106.3, 33.6), k(6.5, 106.3, 33.6)
            )
        ),

        // Taç atışından alınan top: ofsayt yok
        "o4" to Clip(
            duration = 6.0f, x0 = 66f, keyT = 1.5f, offside = true,
            tracks = listOf(
                stay(1, 91.0, 40.0), stay(1, 90.2, 56.0), stay(1, 89.5, 48.0), stay(1, 89.0, 64.0),
                trk(2, k(0.0, 103.5, 34.0), k(3.8, 103.5, 34.0), k(4.6, 103.5, 37.0)),
                trk(0, k(0.0, 78.0, 5.0), k(1.0, 82.0, 0.4), k(6.0, 82.0, 0.4)),
                trk(0, k(0.0, 93.0, 31.0), k(2.8, 96.0, 28.0), k(6.0, 96.5, 28.3)),
                trk(3, k(0.0, 76.0, 30.0), k(6.0, 88.0, 30.0))
            ),
            ball = listOf(
                k(0.0, 82.0, 0.5), k(1.5, 82.0, 0.5), k(2.8, 96.0, 28.0), k(3.6, 96.8, 28.5),
                k(4.4, 105.6, 33.5), k(5.0, 106.3, 33.8), k(6.0, 106.3, 33.8)
            )
        ),

        // Ceza sahası içinde çelme: penaltı
        "f1" to Clip(
            duration = 5.5f, x0 = 62f, keyT = 3.0f, offside = false,
            tracks = listOf(
                down(0, 3.1, k(0.0, 82.0, 28.0), k(3.0, 95.0, 34.0), k(5.5, 95.4, 34.2)),
                down(1, 3.3, k(0.0, 98.0, 45.0), k(2.0, 96.0, 40.0), k(3.0, 95.4, 35.4), k(5.5, 95.4, 35.6)),
                stay(1, 91.0, 22.0), stay(1, 92.0, 48.0),
                trk(2, k(0.0, 103.5, 34.0), k(3.0, 102.0, 34.0)),
                stay(0, 86.0, 46.0), stay(0, 84.0, 16.0),
                trk(3, k(0.0, 80.0, 52.0), k(4.0, 86.0, 50.0))
            ),
            ball = listOf(k(0.0, 83.4, 28.3), k(2.6, 96.0, 33.5), k(3.0, 96.8, 33.8), k(5.5, 99.5, 31.0))
        ),

        // Ceza sahası dışında çelme: direkt serbest vuruş
        "f2" to Clip(
            duration = 5.5f, x0 = 58f, keyT = 3.0f, offside = false,
            tracks = listOf(
                down(0, 3.1, k(0.0, 70.0, 30.0), k(3.0, 84.0, 34.0), k(5.5, 84.5, 34.2)),
                down(1, 3.3, k(0.0, 92.0, 44.0), k(2.0, 88.0, 40.0), k(3.0, 84.4, 35.4), k(5.5, 84.5, 35.6)),
                stay(1, 93.0, 20.0), stay(1, 92.0, 52.0),
                stay(2, 103.5, 34.0),
                stay(0, 80.0, 50.0), stay(0, 78.0, 18.0),
                trk(3, k(0.0, 72.0, 52.0), k(4.0, 80.0, 50.0))
            ),
            ball = listOf(k(0.0, 71.4, 30.2), k(2.6, 84.8, 33.8), k(3.0, 85.6, 34.0), k(5.5, 89.0, 33.0))
        ),

        // Net gol fırsatını engelleme: kırmızı kart
        "f3" to Clip(
            duration = 5.0f, x0 = 55f, keyT = 2.6f, offside = false,
            tracks = listOf(
                down(0, 2.6, k(0.0, 58.0, 34.0), k(2.5, 80.5, 34.0), k(4.8, 82.0, 34.0)),
                down(1, 2.7, k(0.0, 56.0, 37.0), k(2.5, 79.8, 35.5), k(4.8, 80.4, 35.5)),
                stay(1, 62.0, 12.0), stay(1, 64.0, 54.0), stay(1, 70.0, 60.0),
                stay(2, 103.5, 34.0),
                stay(0, 66.0, 20.0),
                trk(3, k(0.0, 70.0, 48.0), k(4.8, 86.0, 50.0))
            ),
            ball = listOf(k(0.0, 59.5, 34.0), k(2.4, 82.0, 34.0), k(2.6, 82.6, 34.0), k(5.0, 92.0, 34.0))
        ),

        // Savunmacı bilerek kaleciye atıyor, kaleci elle alıyor
        "f4" to Clip(
            duration = 5.0f, x0 = 78f, keyT = 1.4f, offside = false,
            tracks = listOf(
                stay(1, 92.0, 34.0),
                trk(2, k(0.0, 103.5, 36.0), k(2.6, 101.4, 34.0), k(4.8, 101.6, 34.0)),
                trk(0, k(0.0, 84.0, 28.0), k(1.4, 92.0, 30.0), k(2.6, 97.0, 32.0)),
                stay(1, 95.0, 22.0), stay(1, 95.0, 46.0),
                trk(3, k(0.0, 82.0, 46.0), k(4.8, 90.0, 46.0))
            ),
            ball = listOf(k(0.0, 92.8, 34.0), k(1.4, 92.8, 34.0), k(2.6, 101.2, 34.0), k(4.8, 101.6, 34.0))
        ),

        // Köşe vuruşundan doğrudan gol
        "c1" to Clip(
            duration = 6.0f, x0 = 70f, keyT = 1.5f, offside = false,
            tracks = listOf(
                trk(0, k(0.0, 104.3, 1.5), k(1.2, 105.0, 0.4), k(6.0, 105.0, 0.4)),
                stay(0, 98.0, 28.0), stay(0, 99.0, 40.0), stay(0, 97.0, 34.0),
                stay(1, 100.0, 30.0), stay(1, 101.0, 38.0), stay(1, 99.0, 24.0), stay(1, 96.0, 44.0),
                trk(2, k(0.0, 103.5, 34.0), k(2.9, 103.5, 34.0), k(3.7, 103.5, 31.0)),
                trk(3, k(0.0, 90.0, 56.0), k(6.0, 92.0, 56.0))
            ),
            ball = listOf(
                k(0.0, 105.0, 0.5), k(1.5, 105.0, 0.5), k(2.2, 101.0, 12.0), k(2.9, 99.5, 26.0),
                k(3.5, 102.5, 33.5), k(3.9, 106.2, 35.2), k(6.0, 106.4, 35.6)
            )
        ),

        // Taç atışı doğrudan rakip kaleye giriyor
        "c2" to Clip(
            duration = 6.0f, x0 = 72f, keyT = 1.3f, offside = false,
            tracks = listOf(
                trk(0, k(0.0, 98.0, 3.0), k(0.9, 100.0, 0.4), k(6.0, 100.0, 0.4)),
                stay(0, 94.0, 30.0), stay(0, 98.0, 46.0),
                stay(1, 96.0, 28.0), stay(1, 97.0, 40.0), stay(1, 95.0, 18.0), stay(1, 99.0, 50.0),
                stay(2, 103.5, 41.0),
                trk(3, k(0.0, 88.0, 50.0), k(6.0, 92.0, 50.0))
            ),
            ball = listOf(
                k(0.0, 100.0, 0.5), k(1.3, 100.0, 0.5), k(2.6, 105.8, 36.0), k(3.2, 106.4, 36.5), k(6.0, 106.4, 36.5)
            )
        ),

        // Avantaj: faul oluyor ama hücum devam ediyor
        "f5" to Clip(
            duration = 7.0f, x0 = 62f, keyT = 1.5f, offside = false,
            tracks = listOf(
                down(0, 1.5, k(0.0, 74.0, 26.0), k(1.4, 79.5, 26.0), k(3.0, 80.5, 25.5), k(7.0, 80.5, 25.5)),
                trk(1, k(0.0, 78.0, 31.0), k(1.4, 80.0, 27.0), k(7.0, 80.6, 27.0)),
                trk(0, k(0.0, 76.0, 38.0), k(2.4, 84.0, 35.0), k(4.2, 95.0, 34.0), k(5.2, 96.0, 34.0), k(7.0, 96.5, 34.0)),
                stay(1, 92.0, 18.0), stay(1, 93.0, 50.0), stay(1, 89.0, 46.0),
                trk(2, k(0.0, 103.5, 34.0), k(4.9, 103.5, 34.0), k(5.8, 103.5, 31.0)),
                trk(3, k(0.0, 78.0, 45.0), k(7.0, 92.0, 45.0))
            ),
            ball = listOf(
                k(0.0, 74.8, 26.2), k(1.4, 80.2, 26.3), k(1.5, 80.6, 27.0), k(2.4, 84.4, 34.8),
                k(4.2, 95.5, 34.0), k(4.9, 96.0, 34.0), k(5.8, 105.6, 36.5), k(6.3, 106.3, 36.6), k(7.0, 106.3, 36.6)
            )
        )
    )

    fun clip(id: String): Clip = clips.getValue(id)

    private fun yn4(a: String, b: String, c: String, d: String) = listOf(a, b, c, d)

    val questions: List<VQ> = listOf(
        VQ("o1", 2,
            "Watch the pass and the goal. What should the referee team decide?",
            "Pası ve golü izle. Hakem ekibi ne karar vermelidir?",
            yn4("Goal is allowed", "Offside – goal disallowed, indirect free kick", "Direct free kick to the defenders", "Dropped ball"),
            yn4("Gol geçerli", "Ofsayt – gol sayılmaz, endirekt serbest vuruş", "Savunmaya direkt serbest vuruş", "Hakem atışı"), 1,
            "When the pass was played, the attacker was about 1.4 m ahead of the second-last opponent (the last outfield defender). Use VAR: top view and the offside line at the moment of the pass.",
            "Pas atıldığı anda hücum oyuncusu sondan ikinci rakibin (son saha oyuncusu) yaklaşık 1,4 m önündeydi. VAR'da üst açıyı ve pas anındaki ofsayt çizgisini kullan."),
        VQ("o2", 2,
            "Watch the pass and the goal. What should the referee team decide?",
            "Pası ve golü izle. Hakem ekibi ne karar vermelidir?",
            yn4("Goal is allowed", "Offside – goal disallowed, indirect free kick", "Direct free kick to the defenders", "Dropped ball"),
            yn4("Gol geçerli", "Ofsayt – gol sayılmaz, endirekt serbest vuruş", "Savunmaya direkt serbest vuruş", "Hakem atışı"), 0,
            "At the moment of the pass the attacker was about 1.4 m behind the second-last opponent. He ran past the line only after the ball was played, which is legal.",
            "Pas anında hücum oyuncusu sondan ikinci rakibin yaklaşık 1,4 m gerisindeydi. Çizgiyi ancak top oynandıktan sonra geçti; bu yasaldır."),
        VQ("o5", 3,
            "A very tight one. Was the attacker offside when the pass was played?",
            "Çok kıl payı bir pozisyon. Pas atıldığında hücum oyuncusu ofsayt mıydı?",
            yn4("Goal is allowed", "Offside – goal disallowed, indirect free kick", "Direct free kick to the defenders", "Dropped ball"),
            yn4("Gol geçerli", "Ofsayt – gol sayılmaz, endirekt serbest vuruş", "Savunmaya direkt serbest vuruş", "Hakem atışı"), 1,
            "He was only about 0.7 m ahead of the second-last opponent, but any part of the head, body or legs ahead counts. Freeze the frame at the pass in the VAR top view.",
            "Sadece yaklaşık 0,7 m önündeydi ama baş, gövde veya bacağın herhangi bir parçası önde olması yeter. VAR üst açıda pas anını dondur."),
        VQ("o3", 3,
            "One attacker stands in an offside position far from the play. His teammate (onside) scores. Decision?",
            "Bir hücum oyuncusu oyundan uzakta ofsayt pozisyonunda duruyor. Onside olan takım arkadaşı gol atıyor. Karar?",
            yn4("Goal is allowed", "Offside – indirect free kick", "Direct free kick", "Penalty kick"),
            yn4("Gol geçerli", "Ofsayt – endirekt serbest vuruş", "Direkt serbest vuruş", "Penaltı atışı"), 0,
            "Being in an offside position is not an offence. The wide player did not touch the ball, interfere with an opponent or gain an advantage.",
            "Ofsayt pozisyonunda olmak ihlal değildir. Kanattaki oyuncu topa dokunmadı, rakibe müdahale etmedi ve avantaj sağlamadı."),
        VQ("o4", 2,
            "The attacker is far ahead of every defender when the throw-in is taken, then scores. Decision?",
            "Taç atışı kullanılırken hücum oyuncusu tüm savunmacıların çok önünde; sonra gol atıyor. Karar?",
            yn4("Goal is allowed", "Offside – indirect free kick", "Direct free kick", "Throw-in is retaken"),
            yn4("Gol geçerli", "Ofsayt – endirekt serbest vuruş", "Direkt serbest vuruş", "Taç atışı tekrarlanır"), 0,
            "There is no offside offence when receiving the ball directly from a throw-in.",
            "Topu doğrudan taç atışından alan oyuncu ofsayttan cezalandırılmaz."),
        VQ("f1", 1,
            "The defender slides in and trips the attacker. Decision?",
            "Savunmacı kayarak hücum oyuncusuna çelme takıyor. Karar?",
            yn4("Penalty kick", "Direct free kick on the edge of the area", "Indirect free kick", "Play on"),
            yn4("Penaltı atışı", "Ceza sahası çizgisinden direkt serbest vuruş", "Endirekt serbest vuruş", "Oyun devam"), 0,
            "The careless trip happened inside the penalty area, so the restart is a penalty kick.",
            "Dikkatsiz çelme ceza sahasının içinde yapıldı; bu yüzden penaltı atışı verilir."),
        VQ("f2", 1,
            "The defender slides in and trips the attacker. Decision?",
            "Savunmacı kayarak hücum oyuncusuna çelme takıyor. Karar?",
            yn4("Penalty kick", "Direct free kick", "Indirect free kick", "Play on"),
            yn4("Penaltı atışı", "Direkt serbest vuruş", "Endirekt serbest vuruş", "Oyun devam"), 1,
            "The contact happened outside the penalty area (look at the line), so it is a direct free kick from the place of the offence.",
            "Temas ceza sahasının dışında oldu (çizgiye bak); bu yüzden ihlal yerinden direkt serbest vuruş verilir."),
        VQ("f3", 2,
            "The attacker is clean through on goal when the last defender pulls him down outside the area. What sanction?",
            "Hücum oyuncusu tek başına kaleye giderken son savunmacı onu ceza sahası dışında düşürüyor. Yaptırım nedir?",
            yn4("No card", "Yellow card", "Red card", "Verbal warning only"),
            yn4("Kart yok", "Sarı kart", "Kırmızı kart", "Sadece sözlü uyarı"), 2,
            "Denying an obvious goal-scoring opportunity (distance to goal, direction of play, ball control, no other defender in position) by a foul outside the area is a red card, plus a direct free kick.",
            "Ceza sahası dışında faulle net gol fırsatını engellemek (kaleye uzaklık, oyun yönü, top kontrolü, araya girecek başka savunmacı yok) kırmızı kart ve direkt serbest vuruş gerektirir."),
        VQ("f4", 1,
            "A defender deliberately kicks the ball to his own goalkeeper, who picks it up with his hands. Decision?",
            "Bir savunmacı topu bilerek kendi kalecisine vuruyor ve kaleci elle alıyor. Karar?",
            yn4("Indirect free kick to the attackers", "Penalty kick", "Direct free kick to the attackers", "Play on"),
            yn4("Hücum takımına endirekt serbest vuruş", "Penaltı atışı", "Hücum takımına direkt serbest vuruş", "Oyun devam"), 0,
            "The goalkeeper may not handle a ball deliberately kicked to him by a teammate. Indirect free kick, taken from where the keeper touched it (goal area line rule applies).",
            "Kaleci, takım arkadaşının bilerek ayakla verdiği topu elle tutamaz. Endirekt serbest vuruş verilir (kale alanı içindeyse kale alanı çizgisinden kullanılır)."),
        VQ("c1", 1,
            "The corner kick curls straight into the goal without anyone touching it. Decision?",
            "Köşe vuruşu kimseye değmeden doğrudan kaleye giriyor. Karar?",
            yn4("Goal", "Corner kick is retaken", "Goal kick", "Indirect free kick"),
            yn4("Gol", "Köşe vuruşu tekrarlanır", "Kale vuruşu", "Endirekt serbest vuruş"), 0,
            "A goal can be scored directly from a corner kick against the opponents.",
            "Köşe vuruşundan doğrudan rakip kaleye gol atılabilir."),
        VQ("c2", 2,
            "A long throw-in flies directly into the opponents' goal without touching anyone. Decision?",
            "Uzun bir taç atışı kimseye değmeden doğrudan rakip kaleye giriyor. Karar?",
            yn4("Goal", "Goal kick", "Corner kick", "Throw-in is retaken"),
            yn4("Gol", "Kale vuruşu", "Köşe vuruşu", "Taç atışı tekrarlanır"), 1,
            "A goal cannot be scored directly from a throw-in. If it enters the opponents' goal, a goal kick is awarded.",
            "Taç atışından doğrudan gol olmaz. Top rakip kaleye girerse kale vuruşu verilir."),
        VQ("f5", 2,
            "An attacker is tripped but his team keeps a promising attack. What should the referee do at the moment of the foul?",
            "Bir hücum oyuncusu çelmeleniyor ama takımı umut vadeden atağa devam ediyor. Hakem faul anında ne yapmalıdır?",
            yn4("Play advantage (play on)", "Stop play: direct free kick", "Stop play: indirect free kick", "Dropped ball"),
            yn4("Avantaj uygula (oyun devam)", "Oyunu durdur: direkt serbest vuruş", "Oyunu durdur: endirekt serbest vuruş", "Hakem atışı"), 0,
            "The teammate gained possession with a clear run at goal, so the advantage is worth more than a free kick. The referee can still sanction the foul later.",
            "Takım arkadaşı net bir kaleye koşuyla topa sahip oldu; avantaj serbest vurundan daha değerli. Hakem faulü sonra yine cezalandırabilir.")
    )

    fun pick(n: Int): List<VQ> = questions.shuffled().take(n)
    fun randomMirror(): Boolean = Random.nextBoolean()
}
