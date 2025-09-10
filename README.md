# Spade Ace - 고성능 복호화 공격 도구

[![APK Builder](https://github.com/Binah-Arbitor/Spade-Ace/actions/workflows/apk-builder.yml/badge.svg)](https://github.com/Binah-Arbitor/Spade-Ace/actions/workflows/apk-builder.yml)

**Spade Ace**는 고성능 파일 암호화/복호화를 위한 다중 플랫폼 도구입니다. 원래 코틀린 기반의 안드로이드 복호화 공격 도구에서 확장되어, 이제 PCB/사이버테크 미학을 적용한 Flutter UI와 Crypto++ 라이브러리 기반의 고성능 백엔드를 포함합니다.

## 🆕 Flutter UI (신규 추가)
`flutter_ui/` 디렉토리에서 새로운 고성능 암호화 유틸리티 UI를 확인하세요:
- **PCB/사이버펑크 테마**: 진한 배경에 청록색과 라임그린 악센트
- **실시간 로그 콘솔**: 백엔드 처리 과정의 실시간 모니터링
- **고급 암호화 설정**: AES, Serpent, Twofish 등 다양한 알고리즘 지원
- **멀티스레딩 제어**: CPU 코어 감지 및 최적화된 성능 설정
- **하드웨어 가속**: GPU 기반 병렬 처리 지원

## 주요 기능

### 🔐 암호 해독 알고리즘
- **무차별 대입 공격 (Brute Force)**: 모든 가능한 조합을 체계적으로 시도
- **스마트 무차별 대입 (Smart Brute Force)**: 일반적인 패턴과 최적화를 사용한 지능형 무차별 대입
- **사전 공격 (Dictionary Attack)**: 사전 파일을 기반으로 한 효율적인 공격
- **하이브리드 공격 (Hybrid Attack)**: 사전 공격과 무차별 대입의 조합
- **마스크 공격 (Mask Attack)**: 패턴 기반 공격 (?l?l?d?d 형식)
- **규칙 기반 공격 (Rule-based Attack)**: 변환 규칙을 적용한 고급 공격
- **레인보우 테이블 공격 (Rainbow Table)**: 사전 계산된 해시 테이블 활용
- **다중 암호화 지원**: AES, DES, 3DES, Blowfish, Twofish, Serpent, CAST, RC 시리즈, Camellia, IDEA, Skipjack 등

### ⚡ 성능 최적화
- **멀티스레딩**: 최대 CPU 코어 수의 2배까지 스레드 활용
- **하드웨어 가속**: GPU 기반 연산 가속 지원
- **4단계 최적화 레벨**: Low, Medium, High, Extreme
- **메모리 관리**: 효율적인 청크 기반 파일 처리
- **실시간 진행률 추적**: 예상 완료 시간과 현재 시도 표시

### 🚀 하드웨어 가속 (신규)
- **CPU 전용 모드**: 전통적인 CPU 기반 처리
- **GPU 보조 모드**: GPU 가속을 통한 병렬 처리 향상
- **하이브리드 모드**: CPU와 GPU를 균형있게 활용

### 🔑 고급 키 유도 방법
- **SHA-256 Simple**: 기본 해시 기반 키 생성
- **PBKDF2**: 패스워드 기반 키 유도 함수
- **Scrypt**: 메모리 하드 키 유도 함수  
- **Argon2**: 최신 패스워드 해싱 표준
- **Bcrypt**: 적응형 해시 함수
=======
- **GPU 감지**: 자동 GPU 칩셋 및 성능 감지
- **3가지 가속 모드**: CPU 전용, GPU 보조, 하이브리드 모드
- **지원 칩셋**: Adreno, Mali, PowerVR, NVIDIA Tegra
- **Vulkan 지원**: 최신 GPU API 활용
- **실시간 GPU 정보**: 현재 GPU 상태 및 성능 정보 표시

### 🎨 사용자 인터페이스
- **Material Design 3**: 현대적이고 직관적인 UI/UX
- **3개 탭 구조**: 복호화 공격, 파일 관리, 설정
- **실시간 파일 브라우저**: 타겟 파일 쉬운 선택
- **다크/라이트 테마 지원**: 자동 테마 전환

### 📁 파일 관리
- **통합 파일 브라우저**: 암호화된 파일 탐색 및 관리
- **파일 타입 아이콘**: 직관적인 파일 식별
- **숨김 파일 표시 옵션**: 모든 파일 접근 가능
- **파일 크기 표시**: 효율적인 파일 관리

## 시스템 요구사항

- **Android 버전**: Android 5.0 (API 21) 이상
- **메모리**: 최소 1GB RAM 권장 (GPU 가속 시 2GB 권장)
- **저장공간**: 50MB 이상
- **권한**: 외부 저장소 읽기/쓰기, 인터넷 (선택사항)
- **GPU 지원** (선택사항): 
  - Qualcomm Adreno 630+
  - ARM Mali-G76+
  - PowerVR Series 6+
  - NVIDIA Tegra
  - OpenGL ES 3.1+ 또는 Vulkan API

## 설치 및 사용

### 빌드 방법

#### 🚀 간단한 APK 빌드 (권장)
이 프로젝트는 **단일 통합 APK 빌더**를 제공합니다:

**APK Builder** - 깔끔하고 효율적인 통합 빌더
- 간단한 설정과 빠른 빌드
- 디버그/릴리즈 APK 빌드 지원
- 안정적이고 유지보수가 쉬운 구조

**사용 방법**:
- [GitHub Actions](https://github.com/Binah-Arbitor/Spade-Ace/actions) 페이지로 이동
- "APK Builder" 워크플로 선택 후 "Run workflow" 클릭
- 빌드 타입 선택 (debug/release/both)

**다운로드**:
- **디버그 APK**: [Actions](https://github.com/Binah-Arbitor/Spade-Ace/actions)에서 다운로드
- **릴리즈 APK**: [Releases](https://github.com/Binah-Arbitor/Spade-Ace/releases) 페이지에서 다운로드

#### 로컬 빌드

**빌드 상태 확인** (권장):
```bash
# 프로젝트 클론
git clone https://github.com/Binah-Arbitor/Spade-Ace.git
cd Spade-Ace

# 빌드 환경 상태 확인 및 권장사항 확인
./check-builder-status.sh
```

**안정적인 로컬 빌드**:
```bash
# 자동화된 안전한 빌드 (권장)
./build-apk.sh both      # debug와 release 모두
./build-apk.sh debug     # debug만
./build-apk.sh release   # release만

# 직접 Gradle 사용
./gradlew clean
./gradlew assembleDebug    # 디버그 빌드
./gradlew assembleRelease  # 릴리즈 빌드
```

**문제 해결**:
- 빌드 실패 시 Actions 탭에서 로그 확인
- 캐시 문제 시 Gradle 캐시 삭제 후 재실행
- 자세한 가이드: [APK_BUILDER_GUIDE.md](APK_BUILDER_GUIDE.md)

### 사용법
1. **파일 선택**: 복호화할 대상 파일 선택
2. **공격 방식 선택**: 7가지 공격 유형 중 선택
   - **무차별 대입**: 모든 조합 시도 (기본)
   - **스마트 무차별 대입**: 일반적인 패턴 우선 시도
   - **사전 공격**: 단어 리스트 파일 활용
   - **하이브리드**: 사전 + 무차별 대입 조합
   - **마스크 공격**: ?l?l?d?d 형식의 패턴 사용
   - **규칙 기반**: 변환 규칙 파일 적용
   - **레인보우 테이블**: 사전 계산된 해시 테이블 활용
3. **설정 조정**: 공격 유형에 따른 세부 설정
   - 최대 비밀번호 길이, 문자 집합 (무차별 대입)
   - 사전/규칙/레인보우 테이블 파일 (해당 공격 유형)
   - 마스크 패턴 (?l=소문자, ?u=대문자, ?d=숫자, ?s=특수문자)
4. **성능 최적화**: 스레드 수, 최적화 레벨, 하드웨어 가속 설정
5. **키 유도 방법**: SHA-256, PBKDF2, Scrypt, Argon2, Bcrypt 중 선택
6. **공격 시작**: 실시간 진행률과 함께 공격 실행

## 기술 스택

### 핵심 기술
- **언어**: Kotlin 100%
- **UI 프레임워크**: Jetpack Compose
- **암호화 라이브러리**: BouncyCastle
- **동시성**: Kotlin Coroutines
- **아키텍처**: MVVM with StateFlow

### 주요 의존성
```gradle
// UI 및 컴포즈
androidx.compose.ui:ui
androidx.compose.material3:material3
androidx.activity:activity-compose

// 생명주기 및 뷰모델
androidx.lifecycle:lifecycle-viewmodel-compose
androidx.lifecycle:lifecycle-runtime-ktx

// 암호화 및 보안
org.bouncycastle:bcprov-jdk15on
commons-codec:commons-codec

// 동시성
org.jetbrains.kotlinx:kotlinx-coroutines-android
```

## 프로젝트 구조

```
app/src/main/java/com/binah/spadeace/
├── core/
│   └── DecryptionEngine.kt      # 핵심 복호화 엔진
├── data/
│   └── Models.kt                # 데이터 모델 및 설정
├── ui/
│   ├── screens/
│   │   ├── DecryptionScreen.kt  # 복호화 공격 화면
│   │   ├── FileOperationsScreen.kt # 파일 관리 화면
│   │   └── SettingsScreen.kt    # 설정 화면
│   ├── theme/
│   │   ├── Theme.kt            # 앱 테마 정의
│   │   └── Type.kt             # 타이포그래피 설정
│   ├── MainViewModel.kt        # 메인 뷰모델
│   └── SpadeAceApp.kt         # 메인 앱 컴포저블
└── MainActivity.kt             # 메인 액티비티
```

## 보안 주의사항

⚠️ **중요**: 이 도구는 교육 목적과 승인된 침투 테스트 용도로만 설계되었습니다.

- 파일 복호화 시도 전 적절한 권한을 확보하세요
- 자신이 소유하지 않은 파일에 대한 무단 접근은 불법입니다
- 사용자는 해당 도구의 사용에 대한 모든 법적 책임을 집니다

## 성능 특성

### 최적화 레벨별 성능
| 레벨 | 업데이트 간격 | 전력 사용 | 예상 성능 |
|------|---------------|-----------|-----------|
| Low | 100회마다 | 낮음 | 기본 성능 |
| Medium | 500회마다 | 보통 | 1.5x 성능 |
| High | 1000회마다 | 높음 | 2x 성능 |
| Extreme | 5000회마다 | 최대 | 3x+ 성능 |

### 지원 암호화 알고리즘
- **대칭 키**: AES, DES, 3DES, Blowfish, Twofish, Serpent, CAST5/6, RC2/4/5/6, Camellia, IDEA, Skipjack
- **모드**: ECB, CBC, CFB, OFB, CTR, GCM, CCM
- **패딩**: PKCS5Padding, NoPadding, PKCS1Padding, OAEPPadding, ISO10126Padding, X9.23Padding

### 지원 공격 유형
- **Brute Force**: 전체 조합 시도 (기본 성능)
- **Smart Brute Force**: 패턴 최적화 (1.2x 성능)  
- **Dictionary**: 사전 기반 (10-100x 성능)
- **Hybrid**: 사전 + 무차별 대입 (2-50x 성능)
- **Mask**: 패턴 기반 (?l?l?d?d) (5-20x 성능)
- **Rule-based**: 변환 규칙 적용 (3-15x 성능)
- **Rainbow Table**: 사전 계산된 해시 (100-1000x 성능)

## 기여 방법

1. 이 저장소를 포크하세요
2. 기능 브랜치를 생성하세요 (`git checkout -b feature/AmazingFeature`)
3. 변경사항을 커밋하세요 (`git commit -m 'Add some AmazingFeature'`)
4. 브랜치에 푸시하세요 (`git push origin feature/AmazingFeature`)
5. Pull Request를 생성하세요

## 라이센스

이 프로젝트는 MIT 라이센스 하에 있습니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

## 작성자

- **Binah-Arbitor** - *초기 작업* - [Binah-Arbitor](https://github.com/Binah-Arbitor)

## 감사의 글

- [BouncyCastle](https://www.bouncycastle.org/) - 암호화 라이브러리 제공
- [Android Jetpack Compose](https://developer.android.com/jetpack/compose) - 현대적인 UI 프레임워크
- [Kotlin](https://kotlinlang.org/) - 우아하고 간결한 프로그래밍 언어

---

**주의**: 이 도구를 사용하기 전에 현지 법률과 규정을 확인하시기 바랍니다. 불법적인 활동에 사용될 경우 개발자는 책임지지 않습니다.