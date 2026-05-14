<div align="right">
  <a href="README.md">🇬🇧 English</a>
</div>

# ⚡ Elektrikli Araç Şarj İstasyonu Ağ Yönetim Sistemi

![Live Status](https://img.shields.io/badge/Status-Live-success?style=for-the-badge)
![Frontend](https://img.shields.io/badge/Frontend-Vercel-black?style=for-the-badge&logo=vercel)
![Backend](https://img.shields.io/badge/Backend-Render-46E3B7?style=for-the-badge&logo=render)
![Database](https://img.shields.io/badge/Database-Supabase-3ECF8E?style=for-the-badge&logo=supabase)

Elektrikli araç şarj istasyonu ağını yönetmek, rezervasyon ve ödemeleri idare etmek ve şarj oturumlarını takip etmek için geliştirilmiş kapsamlı bir sistem. Sistem, rol tabanlı erişim kontrolü ile elektrikli araç sürücülerine, istasyon operatörlerine ve yöneticilere (admin) hizmet verir.

## 🚀 Canlı Demo
- **Frontend (Vercel)**: [https://ev-charging-station-lake.vercel.app/](https://ev-charging-station-lake.vercel.app/)
- **Backend API (Render)**: [https://ev-charging-station-8c5y.onrender.com](https://ev-charging-station-8c5y.onrender.com)

---

## 🌟 Temel Özellikler

### 🔌 İstasyon Kataloğu ve Şarj Cihazı Türleri
Konum bilgileri, şarj cihazı türleri (AC/DC), güç çıkışı (22kW, 50kW, 150kW), kWh başına fiyatlandırma, bağlantı türleri (Type 2, CCS, CHAdeMO) ve çalışma saatleri dahil olmak üzere şarj istasyonlarının veritabanını yönetir.

### 🗺️ İnteraktif Harita ve İstasyon Bulucu
Tüm istasyonları göstermek için interaktif bir harita (Leaflet) entegre edilmiştir. Kullanıcılar mevcut konumlarını görebilir, yakındaki istasyonları gerçek zamanlı uygunluk durumuyla (**Yeşil:** Uygun, **Sarı:** Dolu, **Kırmızı:** Çevrimdışı) inceleyebilir ve seçilen istasyona yol tarifi alabilir.

### 🚗 Araç Kaydı
Kullanıcıların elektrikli araçlarını (marka, model, batarya kapasitesi, bağlantı tipi, plaka) sisteme kaydetmelerine olanak tanır. Sistem, rezervasyon yaparken araç-şarj cihazı uyumluluğunu doğrular.

### 📅 Şarj Rezervasyonu ve Çakışma Yönetimi
Kullanıcılar harita üzerinden bir istasyon seçip şarj slotu rezerve edebilir. Sistem kuralları (örn. maksimum süre, önceden rezervasyon limitleri) zorunlu kılar ve çifte rezervasyonu (double-booking) engeller. Kesin bir **No-Show Politikası** ve otomatik hayalet oturum (ghost-session) temizleme özelliği içerir.

### 💳 Online Ödeme ve Cüzdan Sistemi
Cüzdan yüklemeleri için güvenli bir ödeme sistemi entegre edilmiştir. Şarj ücretleri, tüketilen kWh miktarına göre otomatik olarak düşülür. İptal edilen rezervasyonlar için dinamik iade işlemi sağlar.

### 🔋 Şarj Oturumu Takibi
Aktif şarj oturumlarını gerçek zamanlı ilerleme ile (mevcut kWh, tahmini kalan süre, maliyet) kaydeder. Kullanıcılar oturum durumlarını canlı olarak izleyebilir.

### 🛠️ İstasyon Bakımı ve Sorun Bildirimi
Kullanıcılar istasyon sorunlarını bildirebilir. Operatörler şarj cihazlarını "servis dışı" olarak işaretleyebilir; bu durum etkilenen rezervasyonları otomatik olarak iptal eder ve kullanıcılara **gerçek zamanlı bildirimler** gönderir.

### 📊 Yönetici (Admin) Raporlaması
Gelir raporları, istasyon kullanım istatistikleri, yoğun saat analizi ve kullanıcı aktivite özetleri sunan kapsamlı bir Yönetici Paneli (Admin Dashboard) içerir.

---

## 🛠️ Teknoloji Yığını

**Frontend:**
- **React.js** (Vite)
- **Vanilla CSS** (Glassmorphism ve Modern Responsive UI)
- **Leaflet & Leaflet Routing Machine** (İnteraktif Harita Entegrasyonu)
- **Vercel** (Hosting)

**Backend:**
- **Java Spring Boot 3+**
- **Spring Security ve RESTful API Mimarisi**
- **Render** (Hosting)

**Veritabanı ve Kimlik Doğrulama:**
- **PostgreSQL** (İlişkisel Veri Yönetimi)
- **Supabase** (Veritabanı Hosting ve Authentication)

---

## 👥 Kullanıcı Rolleri
1. **Sürücü (Driver)**: Araç kaydedebilir, haritada istasyon arayabilir, rezervasyon yapabilir, şarj oturumlarını takip edebilir ve arıza bildiriminde bulunabilir.
2. **Operatör**: Belirli istasyonları yönetir, cihaz durumlarını izler, arıza bildirimlerini çözer ve bakım gerektiğinde rezervasyonları iptal edebilir.
3. **Yönetici (Admin)**: Tüm sisteme tam erişimi vardır; platform genelindeki gelir ve kullanım raporlarını görüntüler, genel sistem yapılandırmasını yönetir.

---

## 💻 Geliştirme Ortamı Kurulumu (Local Development)

### Gereksinimler
- Node.js (v18+) ve npm
- Java 17+
- Maven
- PostgreSQL / Supabase Hesabı

### Çevre Değişkenleri (Environment Variables)
Uygulamayı çalıştırmadan önce aşağıdaki ortam değişkenlerini ayarlamanız gerekir.

**Frontend (`ev-frontend/.env`):**
```env
VITE_API_BASE_URL=http://localhost:8080/api
```

**Backend (`ev-backend/src/main/resources/application.properties`):**
```properties
spring.datasource.url=jdbc:postgresql://<SUPABASE_DB_URL>:6543/postgres
spring.datasource.username=<YOUR_DB_USER>
spring.datasource.password=<YOUR_DB_PASSWORD>
# Diğer JWT/Auth secret bilgilerinizi buraya ekleyin
```

### ⚙️ Kurulum ve Çalıştırma

**1. Backend Kurulumu (`/ev-backend`)**
```bash
cd ev-backend
# Bağımlılıkları yükleyin ve Spring Boot uygulamasını başlatın
./mvnw spring-boot:run
```
*Backend sunucusu `http://localhost:8080` adresinde çalışacaktır.*

**2. Frontend Kurulumu (`/ev-frontend`)**
```bash
cd ev-frontend
# Bağımlılıkları yükleyin
npm install
# Geliştirme sunucusunu başlatın
npm run dev
```
*Frontend uygulaması `http://localhost:5173` adresinde çalışacaktır.*

---

## 👨‍💻 Geliştirici
**Emir Magrul**
- GitHub: [@emirmagrul](https://github.com/emirmagrul)
- LinkedIn: [Emir Magrul](https://www.linkedin.com/in/emirmagrul/)
