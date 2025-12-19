# 🌍 Triply - Smart Travel Planning App

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Android](https://img.shields.io/badge/Android-SDK%2036-green.svg)](https://developer.android.com/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

Ứng dụng lập kế hoạch du lịch thông minh sử dụng AI (Perplexity) để tự động gợi ý chuyến bay, khách sạn và địa điểm tham quan dựa trên ngân sách và thời gian của người dùng.

## 📋 Mục lục

- [Tổng quan](#-tổng-quan)
- [Tính năng chính](#-tính-năng-chính)
- [Kiến trúc hệ thống](#-kiến-trúc-hệ-thống)
- [Công nghệ sử dụng](#-công-nghệ-sử-dụng)
- [Yêu cầu hệ thống](#-yêu-cầu-hệ-thống)
- [Cài đặt và chạy](#-cài-đặt-và-chạy)
- [Cấu hình](#-cấu-hình)
- [API Documentation](#-api-documentation)
- [Cấu trúc dự án](#-cấu-trúc-dự-án)
- [Đóng góp](#-đóng-góp)
- [License](#-license)

## 🎯 Tổng quan

Triply là một hệ thống ứng dụng di động giúp người dùng lập kế hoạch du lịch một cách thông minh và tiện lợi. Hệ thống bao gồm:

- **Backend Server**: RESTful API với Spring Boot
- **Mobile App**: Ứng dụng Android Native
- **AI Integration**: Tích hợp Perplexity AI cho trip planning và chatbot
- **RAG Chatbot**: Chatbot tư vấn với Retrieval-Augmented Generation

### 🌟 Điểm nổi bật

- ✅ Lập kế hoạch tự động với AI
- ✅ Chatbot tư vấn 24/7 với RAG
- ✅ Tích hợp bản đồ và dẫn đường
- ✅ Quản lý ngân sách thông minh
- ✅ OAuth2 Google Sign-In
- ✅ Admin panel cho quản lý dữ liệu

## 🚀 Tính năng chính

### 🔐 Authentication & User Management
- Đăng ký/Đăng nhập với email và password
- Đăng nhập qua Google OAuth2
- JWT-based authentication
- Quản lý thông tin cá nhân

### 🗺️ Destination Management
- Xem danh sách thành phố và địa điểm du lịch
- Chi tiết địa điểm với rating, reviews
- Tích hợp Google Maps/Place ID
- Thông tin thời tiết theo điểm đến

### ✈️ AI-Powered Trip Planning
- **Scenario 1**: Có điểm đến → Tìm chuyến bay, khách sạn, lịch trình
- **Scenario 2**: Chưa có điểm đến → AI gợi ý điểm đến phù hợp
- Phân bổ ngân sách tự động (45% flight, 30% hotel, 10% attractions)
- Tích hợp SerpAPI cho flights và hotels thực tế
- Lưu và quản lý kế hoạch

### 💬 AI Chatbot
- Chat với AI sử dụng Perplexity
- RAG với ChromaDB vector store
- Lưu lịch sử hội thoại theo thread
- Context-aware responses

### 🗺️ Map & Navigation
- Hiển thị bản đồ với Mapbox SDK
- Directions với Goong Maps API
- Dẫn đường với Google Maps
- GPS tracking

### 👨‍💼 Admin Features
- CRUD operations cho Region/City/Destination
- Crawl dữ liệu từ SerpAPI Google Maps
- Embedding destinations vào vector store
- Thống kê và báo cáo

## 🏗️ Kiến trúc hệ thống

### Backend Architecture

```
┌─────────────┐
│   Client    │ (Android App)
└──────┬──────┘
       │ HTTP/REST
       │ JWT Token
┌──────▼──────────────────────────────┐
│      Spring Boot Server             │
│  ┌────────────────────────────────┐ │
│  │   Security Layer (JWT Filter)  │ │
│  └────────────┬───────────────────┘ │
│  ┌────────────▼───────────────────┐ │
│  │      Controllers Layer          │ │
│  │  /auth  /destinations  /trip    │ │
│  │  /chat  /admin                  │ │
│  └────────────┬───────────────────┘ │
│  ┌────────────▼───────────────────┐ │
│  │       Services Layer            │ │
│  │  Business Logic & External APIs │ │
│  └────────────┬───────────────────┘ │
│  ┌────────────▼───────────────────┐ │
│  │     Repository Layer (JPA)      │ │
│  └────────────┬───────────────────┘ │
└───────────────┼─────────────────────┘
                │
       ┌────────┴────────┐
       ▼                 ▼
  ┌─────────┐      ┌──────────────┐
  │  MySQL  │      │ External APIs│
  │Database │      │ - Perplexity │
  └─────────┘      │ - SerpAPI    │
                   │ - Weather    │
                   │ - ChromaDB   │
                   └──────────────┘
```

### Mobile Architecture

```
┌────────────────────────────────────┐
│         Android Activities         │
│  Login → Home → Plan → Map → Chat │
└──────────────┬─────────────────────┘
               │
┌──────────────▼─────────────────────┐
│       API Integration Layer        │
│  Retrofit + OkHttp + Interceptors  │
└──────────────┬─────────────────────┘
               │
┌──────────────▼─────────────────────┐
│        Local Storage               │
│  SharedPreferences (TokenManager)  │
└────────────────────────────────────┘
```

## 🛠️ Công nghệ sử dụng

### Backend Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Spring Boot | 3.5.5 | REST API Framework |
| Java | 21 | Programming Language |
| Spring Security | 3.5.5 | Authentication & Authorization |
| Spring Data JPA | 3.5.5 | Database ORM |
| MySQL | 8.0+ | Relational Database |
| JJWT | 0.12.5 | JWT Token Generation |
| Lombok | Latest | Code Generation |
| Spring AI | 1.1.0 | AI Integration (OpenAI, ChromaDB) |
| Maven | 3.9+ | Build Tool |

### Mobile Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Android SDK | Min 24, Target 36 | Mobile Platform |
| Java | 11 | Programming Language |
| Retrofit | 2.11.0 | HTTP Client |
| OkHttp | 4.12.0 | Network Library |
| Gson | 2.11.0 | JSON Parsing |
| Mapbox SDK | 9.7.1 | Map Display |
| Google Play Services | 21.2.0 | OAuth2 & Location |
| Glide | 5.0.5 | Image Loading |
| Gradle | 8.13.0 | Build Tool |

### External Services

- **Perplexity AI**: Trip planning & chatbot (model: sonar-pro)
- **SerpAPI**: Google Flights, Hotels, Maps data
- **WeatherAPI**: Weather forecast
- **ChromaDB**: Vector database for RAG
- **Goong Maps API**: Directions & routing (Vietnam)
- **Mapbox**: Map tiles & SDK

## 📦 Yêu cầu hệ thống

### Backend

- **Java**: JDK 21+
- **Maven**: 3.9+ (hoặc sử dụng Maven Wrapper đi kèm)
- **MySQL**: 8.0+
- **ChromaDB**: Docker hoặc local instance (port 8000)
- **RAM**: Tối thiểu 2GB
- **Disk**: 500MB cho dependencies

### Android App

- **Android Studio**: Arctic Fox trở lên
- **Android SDK**: Minimum API 24 (Android 7.0)
- **Target SDK**: API 36
- **RAM**: Tối thiểu 4GB (8GB khuyến nghị)
- **Emulator/Device**: Android 7.0+

## 🚀 Cài đặt và chạy

### 1️⃣ Clone Repository

**Lưu ý**: Dự án sử dụng Git Submodule cho TriplyFE (Android App)

**Cách 1: Clone với submodule (Khuyến nghị)**
```bash
# Clone cả backend và frontend
git clone --recurse-submodules https://github.com/Zappu1204/triply.git
cd triply
```

**Cách 2: Clone riêng rồi init submodule**
```bash
git clone https://github.com/Zappu1204/triply.git
cd triply

# Init và update submodule
git submodule update --init --recursive
```

**Kết quả**: Bạn sẽ có cấu trúc:
```
triply/
├── server/          # Backend Spring Boot
└── TriplyFE/        # Android App (submodule)
```

### 2️⃣ Backend Setup

#### Bước 1: Cấu hình Database

```sql
-- Tạo database
CREATE DATABASE travelapp;

-- Tạo user (tùy chọn)
CREATE USER 'triply_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON travelapp.* TO 'triply_user'@'localhost';
FLUSH PRIVILEGES;
```

#### Bước 2: Cấu hình Application

```bash
cd server
cp src/main/resources/application.example.yml src/main/resources/application.yml
```

Chỉnh sửa `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/travelapp
    username: your_username
    password: your_password

# External APIs (bắt buộc)
external-apis:
  perplexity:
    api-key: your_perplexity_api_key
  serpapi:
    api-key: your_serpapi_key
  weatherapi:
    api-key: your_weather_api_key

# JWT Secret
app:
  jwt:
    secret: your_secret_key_min_256_bits
```

#### Bước 3: Chạy ChromaDB (cho RAG)

```bash
# Sử dụng Docker
docker run -p 8000:8000 chromadb/chroma

# Hoặc cài đặt local
pip install chromadb
chroma run --path ./chroma_data
```

#### Bước 4: Build và chạy Backend

**Windows:**
```bash
mvnw.cmd clean install
mvnw.cmd spring-boot:run
```

**Linux/Mac:**
```bash
./mvnw clean install
./mvnw spring-boot:run
```

Server sẽ chạy tại: `http://localhost:8080/api/v1`

### 3️⃣ Android App Setup

> **Lưu ý**: TriplyFE là Git Submodule trỏ đến repo [dihnhuonq130104/TriplyFE](https://github.com/dihnhuonq130104/TriplyFE)

#### Bước 1: Đảm bảo submodule đã được init

```bash
# Kiểm tra submodule status
git submodule status

# Nếu chưa có TriplyFE, chạy:
git submodule update --init --recursive
```

#### Bước 2: Mở project trong Android Studio

```bash
cd TriplyFE
# Mở thư mục này trong Android Studio
```

#### Bước 3: Cấu hình API Base URL

Chỉnh sửa `app/src/main/java/com/example/triply/data/remote/RetrofitClient.java`:

```java
private static final String BASE_URL = "http://YOUR_IP:8080";
// Thay YOUR_IP bằng:
// - 10.0.2.2 nếu dùng emulator
// - IP máy thật nếu test trên thiết bị thật
```

#### Bước 4: Cấu hình Google OAuth2 (tùy chọn)

1. Tạo project tại [Google Cloud Console](https://console.cloud.google.com/)
2. Enable Google Sign-In API
3. Tạo OAuth 2.0 Client ID
4. Thêm SHA-1 fingerprint của app
5. Cấu hình client ID trong backend `application.yml`

#### Bước 5: Build và chạy

**Debug Build:**
```bash
./gradlew :app:assembleDebug
# APK tại: app/build/outputs/apk/debug/app-debug.apk
```

**Release Build:**
```bash
./gradlew :app:assembleRelease
```

Hoặc chạy trực tiếp trong Android Studio: **Run > Run 'app'**

---

### 4️⃣ Làm việc với Git Submodule (TriplyFE)

#### 🔄 Update Frontend từ Remote

Khi frontend team push code mới:

```bash
cd TriplyFE
git pull origin main

# Quay về thư mục gốc và commit submodule reference
cd ..
git add TriplyFE
git commit -m "Update TriplyFE to latest version"
git push origin main
```

#### 💻 Phát triển code trong TriplyFE

```bash
cd TriplyFE

# Tạo branch mới cho feature
git checkout -b feature/new-feature

# Code và commit như bình thường
git add .
git commit -m "Add new feature"

# Push lên repo TriplyFE
git push origin feature/new-feature

# Sau khi merge PR vào main của TriplyFE
git checkout main
git pull origin main

# Quay về repo chính và update reference
cd ..
git add TriplyFE
git commit -m "Update TriplyFE: Add new feature"
git push origin main
```

#### 🔍 Kiểm tra trạng thái Submodule

```bash
# Xem commit hiện tại của submodule
git submodule status

# Update tất cả submodules về commit mới nhất
git submodule update --remote --merge

# Pull repo chính kèm submodules
git pull --recurse-submodules
```

#### ⚠️ Lưu ý khi làm việc với Submodule

1. **Luôn commit trong TriplyFE trước**, sau đó mới commit trong repo chính
2. **Không edit code trực tiếp** mà chưa checkout branch trong TriplyFE
3. **Khi pull code**, nhớ update submodule: `git submodule update --remote`
4. **2 repo độc lập**: TriplyFE có lịch sử commit riêng, repo chính chỉ lưu reference

## ⚙️ Cấu hình

### Backend Environment Variables

Khuyến nghị sử dụng biến môi trường thay vì hard-code trong `application.yml`:

```bash
# Database
export MYSQL_USERNAME=your_username
export MYSQL_PASSWORD=your_password

# API Keys
export PERPLEXITY_API_KEY=your_key
export SERPAPI_API_KEY=your_key
export WEATHER_API_KEY=your_key

# JWT
export APP_JWT_SECRET=your_secret_key

# Google OAuth2
export GOOGLE_CLIENT_ID=your_client_id
export GOOGLE_CLIENT_SECRET=your_client_secret
```

### Admin Account Setup

Mặc định hệ thống không có admin. Để tạo admin:

```sql
-- Sau khi đăng ký tài khoản thường, chạy SQL:
UPDATE tbl_Account 
SET role = 'ADMIN' 
WHERE userName = 'your_email@example.com';
```

## 📚 API Documentation

### Base URL
```
http://localhost:8080/api/v1
```

### Authentication APIs

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/auth/register` | Đăng ký tài khoản | ❌ |
| POST | `/auth/login` | Đăng nhập | ❌ |
| POST | `/auth/social-login` | Đăng nhập Google | ❌ |
| GET | `/auth/me` | Lấy thông tin user | ✅ |

### Destination APIs

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/destinations/cities` | Danh sách thành phố | ✅ |
| GET | `/destinations/cities/{id}` | Chi tiết thành phố | ✅ |
| GET | `/destinations/cities/{id}/destinations` | Địa điểm theo thành phố | ✅ |
| GET | `/destinations` | Tất cả địa điểm | ✅ |
| GET | `/destinations/{id}` | Chi tiết địa điểm | ✅ |

### Trip Planning APIs

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/trip/plan` | Tạo kế hoạch với AI | ✅ |
| POST | `/trip/save` | Lưu kế hoạch đơn giản | ✅ |
| POST | `/trip/save-full` | Lưu kế hoạch đầy đủ | ✅ |
| POST | `/trip/{id}/flight` | Thêm/cập nhật chuyến bay | ✅ |
| DELETE | `/trip/{id}/flight` | Xóa chuyến bay | ✅ |
| POST | `/trip/{id}/hotel` | Thêm/cập nhật khách sạn | ✅ |
| DELETE | `/trip/{id}/hotel` | Xóa khách sạn | ✅ |

### Chat APIs

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/chat/threads` | Tạo thread mới | ✅ |
| GET | `/chat/threads` | Danh sách threads | ✅ |
| GET | `/chat/threads/{id}/messages` | Lịch sử chat | ✅ |
| POST | `/chat/send` | Gửi message | ✅ |

### Admin APIs (ROLE_ADMIN required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET/POST/PUT/DELETE | `/admin/regions/*` | CRUD Region |
| GET/POST/PUT/DELETE | `/admin/cities/*` | CRUD City |
| GET/POST/PUT/DELETE | `/admin/destinations/*` | CRUD Destination |
| POST | `/admin/destinations/crawl/all` | Crawl tất cả cities |
| POST | `/admin/destinations/crawl/city/{id}` | Crawl một city |
| POST | `/admin/destinations/embed/all` | Embed tất cả destinations |
| POST | `/admin/destinations/embed/city/{id}` | Embed theo city |

### Request/Response Examples

**POST /auth/register**
```json
{
  "fullName": "Nguyễn Văn A",
  "email": "user@example.com",
  "phone": "0123456789",
  "userName": "user@example.com",
  "password": "SecurePass123"
}
```

**POST /trip/plan**
```json
{
  "budget": 10000000,
  "startDate": "2025-12-20",
  "endDate": "2025-12-25",
  "people": 2,
  "interests": ["beach", "food", "culture"],
  "destination": "Da Nang",
  "origin": "Ho Chi Minh City"
}
```

## 📁 Cấu trúc dự án

### Backend Structure
```
server/
├── src/
│   ├── main/
│   │   ├── java/com/triply/tripapp/
│   │   │   ├── config/          # Security, JWT, CORS config
│   │   │   ├── controller/      # REST Controllers
│   │   │   ├── service/         # Business Logic
│   │   │   ├── repository/      # JPA Repositories
│   │   │   ├── entity/          # Database Entities
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── integration/     # External API Clients
│   │   │   └── exception/       # Custom Exceptions
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application.example.yml
│   └── test/
├── pom.xml
├── mvnw
└── mvnw.cmd
```

### Android Structure
```
TriplyFE/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/triply/
│   │   │   │   ├── activities/      # UI Activities
│   │   │   │   ├── data/
│   │   │   │   │   ├── remote/      # API Service, Retrofit
│   │   │   │   │   └── model/       # Data Models
│   │   │   │   ├── util/            # TokenManager, Helpers
│   │   │   │   └── adapter/         # RecyclerView Adapters
│   │   │   └── res/
│   │   │       ├── layout/          # XML Layouts
│   │   │       ├── drawable/        # Images, Icons
│   │   │       └── values/          # Strings, Colors
│   │   └── test/
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── gradle/
│   └── libs.versions.toml
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew
└── gradlew.bat
```

## 🧪 Testing

### Backend Tests

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=AuthServiceTest

# Generate coverage report
./mvnw jacoco:report
```

### Android Tests

```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest
```

## 🐛 Troubleshooting

### Backend Issues

**Problem**: Cannot connect to MySQL
```bash
# Check MySQL is running
mysql -u root -p
# Check port 3306 is available
netstat -an | grep 3306
```

**Problem**: External API errors
- Kiểm tra API keys trong `application.yml`
- Verify network connection
- Check API rate limits

### Android Issues

**Problem**: Cannot connect to backend
- Sử dụng `10.0.2.2` cho Android Emulator
- Sử dụng IP thật của máy cho thiết bị thật
- Kiểm tra firewall settings

**Problem**: Google Sign-In không hoạt động
- Kiểm tra SHA-1 fingerprint
- Verify Google OAuth2 client ID
- Check package name matches

## 🤝 Đóng góp

Mọi đóng góp đều được chào đón! Vui lòng:

1. Fork repository
2. Tạo feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Mở Pull Request

## 📝 License

Dự án này được phát hành dưới [MIT License](LICENSE).

## 👥 Contributors

- **Backend Team**: Spring Boot, API Development, AI Integration
- **Mobile Team**: Android Development, UI/UX
- **AI/Integration Team**: RAG, ChromaDB, External APIs
- **Map Team**: Mapbox, Goong, Navigation

## 📧 Contact

- **Project Link**: [https://github.com/Zappu1204/triply](https://github.com/Zappu1204/triply)
- **Frontend Link**: [https://github.com/dihnhuonq130104/TriplyFE](https://github.com/dihnhuonq130104/TriplyFE)

---

Made with ❤️ by Triply Team
