# Düdük Sende  —  © 2026 Armeaze Company
Kotlin + Jetpack Compose. TR/EN. Package: com.armeaze.duduksende

## APK derleme
GitHub Actions -> "Build APK" -> son çalışma -> Artifacts -> DudukSende debug APK.

## Sonradan takılacak yerler (Services.kt)
- Online skor tablosu: `LeaderboardRepo`   (şimdi: cihazdaki yerel skor)
- Online düello eşleşme: `DuelRepo`         (şimdi: antrenman botu)
- Reklam: `AdProvider`                      (şimdi: reklamsız)
- Yapay zekâ soru çeşitlemesi: `AiConfig.PROXY_URL` (boşsa kapalı) -> tools/ai-proxy/worker.js

## 1000+ soru hattı
1. IFAB 2026/27 Oyun Kuralları PDF'ini `tools/laws.pdf` olarak repoya yükle.
2. Repo Settings -> Secrets -> `ANTHROPIC_API_KEY` ekle.
3. Actions -> "Generate questions" -> Run workflow (count: 1000).
4. Üretilen `app/src/main/assets/questions.json` otomatik commit edilir ve uygulama bunu açılışta yükler.
5. Yayından önce soruları örnekleyerek kontrol et (yapay zekâ hata yapabilir).

## v1.3 yenilikleri
- "Hakem topu" -> "Hakem atışı" (tüm Türkçe metinler).
- +62 yeni el yazması soru (MoreQuestions.kt). Yeni sorular listenin SONUNA eklenir.
- Video quiz: kodla çizilen 12 animasyonlu pozisyon (Clips.kt, ClipView.kt, VideoQuiz.kt). Telifsizdir.
- VAR incelemesi: 3 kamera (yayın / üstten / yakın), ağır çekim, kaydırma, kritik ana git, ofsayt çizgisi. Her quizde 2 VAR hakkı.
- Görsel zenginlik: kategori illüstrasyonları, cevap animasyonu, konfeti (Fx.kt).

## v1.4 yenilikleri
- Kural kitabı (Rules.kt): IFAB 17 kuralın TR/EN kısa özeti, arama kutusu. Soru cevabından sonra "İlgili kural" penceresi.
- Gelişim ekranı (Progress.kt): konu bazlı başarı, en zayıf konu + tek dokunuşla antrenman, "Yanlışlarım" (doğru bilene kadar toplanır), son 10 quiz geçmişi.
- Quiz oluşturucu (Progress.kt): konu, zorluk ve soru sayısı (5/10/20) seçip kendi quizini kur.
- Store.kt: recordAnswer / wrongIds / catStats / history eklendi. Eski kayıtlar korunur.

## iPhone'a hazırlık
Telefona özel kod yalnızca Platform.kt (depolama, tarih, dil), Sfx.kt (ses/titreşim) ve Ui.kt içindeki shareText'tedir.
Sonraki adım: proje Compose Multiplatform'a çevrilecek (ortak kod + androidMain + iosMain). iOS derlemesi GitHub Actions macOS sunucusunda yapılır; App Store için Apple Developer hesabı gerekir.
Not: Questions.kt şu an org.json kullanıyor; iOS'a geçerken kotlinx.serialization ile değiştirilecek.
