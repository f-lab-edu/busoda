# 🚌 Busoda 🚏

> **Modern Android Bus Stop Information App**  
> Clean Architecture · Jetpack Compose · Multi-Module

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-blue.svg)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20Architecture-orange.svg)](#architecture)

**버스오다**는 최신 Android 개발 기술과 모범 사례를 적용한 **버스 정류소 정보 조회 애플리케이션**입니다.  
사용자는 버스 정류소를 검색하고, 실시간 도착 정보를 확인하며, 자주 이용하는 정류소를 즐겨찾기에 추가할 수 있습니다.

## ✨ Key Features

- 🔍 **실시간 정류소 검색** - 디바운스 최적화로 효율적인 검색
- 📍 **상세 도착 정보** - 15초마다 자동 갱신되는 실시간 정보
- ⭐ **즐겨찾기 관리** - 자주 이용하는 정류소 저장 및 관리
- 🎨 **Material 3 Design** - 라이트/다크 모드 지원
- 🔗 **딥링크 지원** - 외부에서 특정 정류소로 직접 접근

## 📱 Screenshots

<div align="center">

| 즐겨찾기 화면 | 정류소 검색 | 상세 정보 |
|:---:|:---:|:---:|
| <img src="https://play-lh.googleusercontent.com/tTGu2UM9bN0DDTUJMguaqcwp_TfIUQnOaFikR-ASqeZZFqFblfFyEhBLlXOBJTCSOjw1hQongxz2zZig2AIQ=w1052-h592-rw" width="250"> | <img src="https://play-lh.googleusercontent.com/5M0ddr9NnzzReSRWZnS4YzzIDAVMetIhgUX0zyBOraRQklz_HbNfkKaiPQDRaG3aARzBp11T3DcxO9KfKO8L9Q=w1052-h592-rw" width="250"> | <img src="https://play-lh.googleusercontent.com/zX1cNpjF-bRe-S3q5KeK1sVlb08Cy7Isg544UJim90tjxQORjkEx9iz4-ah4xDbhGHWTATIXNyBY42pnG3Ba=w1052-h592-rw" width="250"> |

</div>

## 🏗️ Architecture

이 프로젝트는 **Multi-Module Clean Architecture**를 채택하여 관심사를 명확히 분리하고 확장성과 테스트 용이성을 확보했습니다.

```
app/
├── feature/
│   ├── stoplist/      # 정류소 검색 기능
│   ├── stopdetail/    # 정류소 상세 정보
│   └── favorites/     # 즐겨찾기 관리
└── core/
    ├── data/          # Repository 구현
    ├── database/      # Room 데이터베이스
    ├── model/         # 데이터 모델
    ├── ui/           # 디자인 시스템
    └── testing/      # 테스트 유틸리티
```

### Architecture Principles

- **🔄 Unidirectional Data Flow (UDF)** - 예측 가능한 상태 관리
- **🎯 Single Responsibility** - 각 모듈과 클래스의 명확한 역할 분담
- **📦 Dependency Inversion** - 인터페이스 기반의 느슨한 결합
- **⚡ Reactive Streams** - Flow를 통한 반응형 데이터 처리

## 🛠️ Tech Stack

### Core Technologies
- **Language**: Kotlin 100%
- **UI Framework**: Jetpack Compose
- **Architecture**: Multi-Module Clean Architecture
- **Dependency Injection**: Hilt
- **Asynchronous**: Coroutines + Flow

### Libraries & Dependencies

| Category | Library | Purpose |
|----------|---------|---------|
| **UI** | Jetpack Compose | 선언형 UI 프레임워크 |
| **Navigation** | Navigation Compose | 화면 간 이동 관리 |
| **DI** | Hilt | 의존성 주입 |
| **Network** | Retrofit + OkHttp | HTTP 통신 |
| **Parsing** | TikXml | XML 응답 파싱 |
| **Database** | Room | 로컬 데이터 저장 |
| **Testing** | JUnit + Mockk | 단위 테스트 |

## 🎯 Key Technical Highlights

### 1. **Reactive Data Flow**
```kotlin
// Repository에서 UI까지 끊김없는 반응형 스트림
@Dao
interface FavoriteStopDao {
    @Query("SELECT * FROM favorite_stops")
    fun getFavorites(): Flow<List<FavoriteStop>>  // Room에서 Flow 반환
}

// ViewModel에서 StateFlow로 변환
val favorites = favoriteRepository.getFavorites()
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
```

### 2. **Performance Optimized Search**
```kotlin
// 검색어 입력 최적화 - 1초 디바운스로 불필요한 API 호출 방지
searchQuery
    .debounce(1000) // 1초 후에 검색 실행
    .distinctUntilChanged()
    .collectLatest { query ->
        if (query.isNotBlank()) {
            searchBusStops(query)
        }
    }
```

### 3. **Clean Dependency Injection**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    abstract fun bindBusStopRepository(
        apiBusStopRepository: ApiBusStopRepository
    ): BusStopRepository  // 인터페이스와 구현체 분리
}
```

## 🧪 Testing Strategy

체계적인 테스트를 통해 코드 품질과 안정성을 보장합니다.

- **📊 Unit Tests**: ViewModel 및 Repository 로직 검증
- **🔀 Mocking**: 외부 의존성 분리로 순수한 테스트 환경 구축
- **⏱️ Async Testing**: 복잡한 비동기 로직의 정확성 검증

```kotlin
@Test
fun `when keyword is updated then api is called only once after debounce time`() = runTest {
    // Given: 검색어를 빠르게 연속 입력
    viewModel.updateSearchQuery("서울")
    viewModel.updateSearchQuery("서울역")
    
    // When: 1초 경과
    advanceTimeBy(1000)
    
    // Then: 마지막 검색어로 단 한번만 API 호출
    verify(exactly = 1) { repository.searchBusStops("서울역") }
}
```

## 📂 Module Structure

<details>
<summary><strong>📱 :app</strong> - Application Module</summary>

- `MainActivity.kt` - Single Activity entry point
- `MainApplication.kt` - Hilt application class
- `di/` - App-level dependency injection

</details>

<details>
<summary><strong>🎨 :feature</strong> - Feature Modules</summary>

**:feature:stoplist**
- 정류소 검색 및 목록 표시
- 디바운스 최적화된 실시간 검색

**:feature:stopdetail**
- 정류소 상세 정보 및 버스 도착 현황
- 15초 자동 갱신 타이머

**:feature:favorites**
- 즐겨찾기 정류소 관리
- 반응형 데이터베이스 연동

</details>

<details>
<summary><strong>🔧 :core</strong> - Core Modules</summary>

**:core:data**
- Repository 패턴 구현
- API 및 로컬 데이터 소스 통합

**:core:database**
- Room 데이터베이스 설정
- DAO 및 Entity 정의

**:core:model**
- 앱 전체 데이터 모델
- DTO 및 Entity 클래스

**:core:ui**
- Material 3 디자인 시스템
- 공용 Composable 컴포넌트

</details>

## 🔗 Deep Links

앱은 다음과 같은 딥링크를 지원합니다:

```
busoda://stop_detail/{stopId}
```

외부 앱에서 특정 정류소 정보로 직접 이동할 수 있습니다.


## 📲 Download

<p>
  <a href="https://play.google.com/store/apps/details?id=com.chaeny.busoda" target="_blank">
    <img src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png"
         alt="Get it on Google Play"
         height="80"/>
  </a>
</p>

---

<div align="center">

</div>
