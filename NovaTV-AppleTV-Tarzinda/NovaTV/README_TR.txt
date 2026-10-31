NOVA TV — SİNEMATİK ANDROID TV LAUNCHER
======================================

Bu sürüm, Apple TV / tvOS tarzındaki sade ve sinematik televizyon arayüzlerinden
İLHAM alan özgün bir Nova TV arayüzüdür. Apple logosu, Apple uygulama simgeleri veya
Apple'a ait görseller kullanılmaz.

TASARIM
-------
- Koyu, sinematik arka plan ve yumuşak ışık efektleri
- Büyük özellik/karşılama alanı
- Üstte Ana Sayfa / Uygulamalar / Ayarlar gezinmesi
- TV'deki uygulamaları otomatik listeleyen geniş ikon rafı
- Kumandayla gezinirken büyüyen ve vurgulanan uygulama kartları
- Bir uygulamanın üzerine gelince başlık ve açıklama alanı o uygulamaya dönüşür
- OK ile uygulama açılır; yön tuşlarıyla gezinilir
- Yerel Android çizimleri kullanılır; ağ bağlantısı veya üçüncü taraf UI kütüphanesi yoktur

PROJE BİLGİSİ
-------------
Paket: com.example.novatv
Dil: Java
Minimum Android: 6.0 (API 23)
Hedef SDK: 35
JDK: 17
Android Gradle Plugin: 8.13.2
Gradle: 8.13

APK OLUŞTURMA
-------------
1. Android Studio'yu aç ve New Project > No Activity oluştur.
2. Proje adı: NovaTV
3. Package name: com.example.novatv
4. Language: Java
5. Minimum SDK: API 23
6. Proje açılınca bu ZIP'in içindeki şu dosyaları aynı konumlara kopyalayıp mevcut dosyaların
   üzerine yaz:
   - app/src/main/AndroidManifest.xml
   - app/src/main/java/com/example/novatv/MainActivity.java
   - app/build.gradle.kts
   - app/proguard-rules.pro
   - kökteki build.gradle.kts
   - kökteki settings.gradle.kts
7. Android Studio'da Gradle Sync yap. Gerekirse Android SDK Platform 36'yı kur.
8. Build > Build Bundle(s) / APK(s) > Build APK(s) seç.
9. APK: app/build/outputs/apk/debug/app-debug.apk

Not: Bu kaynak ZIP'i Android Studio'nun gradlew çalıştırıcısını ve wrapper JAR'ını içermez.
Android Studio'nun oluşturduğu Gradle Wrapper dosyalarını silme/değiştirme.

VESTEL ANDROID TV'YE YÜKLEME
----------------------------
1. app-debug.apk dosyasını USB belleğe kopyala.
2. USB belleği Vestel TV'ye tak.
3. Dosya yöneticisinden APK'yı aç.
4. İstenirse dosya yöneticisine Bilinmeyen uygulamaları yükle izni ver.
5. Nova TV'yi yükle ve uygulamalar listesinden açarak test et.

VARSAYILAN ANA EKRAN
--------------------
TV destekliyorsa Ayarlar > Uygulamalar > Varsayılan uygulamalar > Ana ekran uygulaması
bölümünden Nova TV'yi seç. Alternatif olarak HOME tuşuna basıp açılan seçim ekranında
Nova TV > Her zaman seçeneğini kullan.

Bazı Vestel yazılım sürümleri üçüncü taraf HOME uygulamalarını seçmeye izin vermeyebilir.
Bu durumda Nova TV yine normal uygulama olarak kullanılabilir. Stok launcher'ı devre dışı
bırakmak ilk çözüm olarak önerilmez.

PERFORMANS
----------
Arayüz Android'in kendi View/Canvas bileşenleriyle çizilir. Ağ isteği, video arka planı,
ağır animasyon kütüphanesi veya sürekli çalışan servis içermez. Saat dakikada bir yenilenir.
Uygulama simgeleri sistemden alınır.
