# 🚀 간소화된 APK 빌더 가이드

이 가이드는 Spade Ace 프로젝트의 새롭게 간소화된 APK 빌드 시스템을 설명합니다.

## 📋 개요

이 프로젝트는 **단일 통합 APK 빌더**를 제공합니다:

### 🚀 APK Builder (통합 빌더)
- **파일**: `.github/workflows/apk-builder.yml`
- **용도**: 모든 빌드 요구사항을 충족하는 간단하고 효율적인 빌더
- **특징**: 
  - 깔끔한 설정과 빠른 빌드
  - 디버그 및 릴리즈 APK 지원
  - 안정적이고 유지보수가 쉬운 구조
  - 불필요한 복잡성 제거

## 🛠️ 사용 방법

### GitHub Actions에서 실행

1. **GitHub 저장소의 Actions 탭으로 이동**
2. **"APK Builder" 워크플로 선택**
3. **"Run workflow" 버튼 클릭**
4. **빌드 타입 선택** (debug, release, both)
5. **"Run workflow" 클릭하여 실행**

### 로컬에서 실행

1. **빌드 스크립트 사용**:
   ```bash
   chmod +x build-apk.sh
   ./build-apk.sh both  # debug와 release 모두 빌드
   ./build-apk.sh debug # debug만 빌드
   ./build-apk.sh release # release만 빌드
   ```

2. **직접 Gradle 사용**:
   ```bash
   ./gradlew clean
   ./gradlew assembleDebug    # Debug APK
   ./gradlew assembleRelease  # Release APK
   ```

## 📁 빌드 결과물

### APK 위치
- **Debug APK**: `app/build/outputs/apk/debug/`
- **Release APK**: `app/build/outputs/apk/release/`

### 아티팩트 다운로드
GitHub Actions 실행 후 다음을 다운로드할 수 있습니다:
- **APK 파일** (debug/release)
- **체크섬 파일** (.sha256, .md5)
- **빌드 정보 파일**

## ⚠️ 문제 해결

### 일반적인 문제들

#### 1. 네트워크 연결 문제
```
Could not GET 'https://dl.google.com/...'
```
**해결책**:
- 워크플로우 재실행 (자동 재시도 메커니즘 포함)
- 몇 분 후 다시 시도

#### 2. 메모리 부족 오류
```
OutOfMemoryError
```
**해결책**:
- `gradle.properties`에서 메모리 설정 확인
- 로컬 빌드 시 메모리 할당 증가

#### 3. 빌드 시간 초과
**해결책**:
- 워크플로우 재실행
- 캐시된 의존성 활용으로 시간 단축

#### 4. 의존성 해결 실패
**해결책**:
- Actions > Caches에서 캐시 클리어 후 재실행
- 네트워크 상태 확인 후 재시도

### 로그 확인 방법

1. **GitHub Actions 페이지에서**:
   - 실패한 작업 클릭
   - 각 단계의 로그 확인
   - "Show all checks" → "Re-run all jobs"

2. **로컬 빌드에서**:
   ```bash
   ./gradlew assembleDebug --stacktrace --debug
   ```

## 🔧 고급 설정

### 사용자 정의 빌드 구성

#### gradle.properties 수정
```properties
# 메모리 설정 증가
org.gradle.jvmargs=-Xmx6g -XX:+UseG1GC

# 빌드 최적화
org.gradle.parallel=true
org.gradle.workers.max=4
```

#### 서명 키 설정 (Release APK)
GitHub Repository Settings → Secrets에 추가:
- `KEYSTORE_BASE64`: 키스토어 파일 (base64 인코딩)
- `KEYSTORE_PASSWORD`: 키스토어 비밀번호
- `KEY_ALIAS`: 키 별칭
- `KEY_PASSWORD`: 키 비밀번호

### 새로운 워크플로 추가

새로운 요구사항에 맞는 워크플로를 만들려면:

1. **기존 워크플로 복사** (가장 유사한 것)
2. **이름과 트리거 수정**
3. **필요한 단계 추가/수정**
4. **테스트 실행**

## 📊 성능 최적화

### 빌드 시간 단축
- **캐시 활용**: GitHub Actions 캐시 사용
- **병렬 빌드**: `org.gradle.parallel=true`
- **증분 빌드**: 소스 변경 시에만 재빌드

### 안정성 향상
- **재시도 메커니즘**: 네트워크 실패 대응
- **검증 단계**: APK 무결성 확인
- **다단계 빌드**: 단계별 오류 감지

### 리소스 최적화
- **메모리 관리**: JVM 힙 크기 조정
- **디스크 공간**: 불필요한 파일 정리
- **네트워크**: 의존성 캐싱

## 🆘 지원

### 빌드 실패 시
1. **Actions 탭에서 로그 확인**: 구체적인 오류 메시지 찾기
2. **캐시 클리어**: Actions > Caches에서 Gradle 캐시 삭제
3. **재시도**: 워크플로우 재실행
4. **로컬 빌드**: 빌드 스크립트 `./build-apk.sh` 사용

### 추가 도움이 필요한 경우
- **Issues 탭**에서 새로운 이슈 생성
- **로그 전체 내용** 포함
- **환경 정보** 제공

## 장점

이 간소화된 시스템의 장점:
- ✅ **단순함**: 하나의 워크플로우로 모든 요구사항 충족
- ✅ **빠름**: 불필요한 복잡성 제거로 빌드 시간 단축
- ✅ **안정성**: 검증된 패턴과 최소한의 설정
- ✅ **유지보수성**: 관리해야 할 파일 수 감소

## ✅ 체크리스트

빌드 전 확인사항:
- [ ] 필요한 모든 소스 파일이 커밋되었는가?
- [ ] `gradle.properties` 설정이 올바른가?
- [ ] 네트워크 연결이 안정적인가?
- [ ] 충분한 저장 공간이 있는가?

빌드 후 확인사항:
- [ ] APK 파일이 생성되었는가?
- [ ] 파일 크기가 합리적인가? (>1MB)
- [ ] 체크섬이 생성되었는가?
- [ ] APK가 정상적으로 설치되는가?

---

**이 간소화된 가이드로 빠르고 안정적인 APK 빌드가 가능합니다!** 🎉