# 🚀 APK 빌더 가이드

이 가이드는 Spade Ace 프로젝트의 APK 빌드를 위한 완전한 솔루션을 제공합니다.

## 📋 개요

이 프로젝트에는 다음과 같은 빌드 워크플로가 있습니다:

### 1. 🧪 Android CI
- **파일**: `.github/workflows/android-ci.yml`
- **용도**: 지속적 통합 - 코드 테스트 및 린트 검사
- **트리거**: Push (main, develop 브랜치), Pull Request (main 브랜치)
- **특징**: 자동 테스트 실행, 코드 품질 검사

### 2. 🏗️ Build APK
- **파일**: `.github/workflows/build-apk.yml`
- **용도**: APK 파일 빌드 (Debug/Release)
- **트리거**: 수동 실행, 태그 푸시 (v*)
- **특징**: 선택적 빌드 타입, 아티팩트 업로드

### 3. 🚀 Release
- **파일**: `.github/workflows/release.yml`
- **용도**: GitHub Release 생성 및 APK 배포
- **트리거**: 태그 푸시 (v*)
- **특징**: 자동 릴리즈 생성, APK 첨부

## 🛠️ 사용 방법

### GitHub Actions에서 실행

#### APK 빌드
1. **GitHub 저장소의 Actions 탭으로 이동**
2. **"Build APK" 워크플로 선택**
3. **"Run workflow" 버튼 클릭**
4. **빌드 타입 선택** (debug 또는 release)
5. **"Run workflow" 클릭하여 실행**

#### 릴리즈 생성
1. **태그 생성**: `git tag v1.0.0 && git push origin v1.0.0`
2. **자동으로 Release 워크플로가 실행됩니다**
3. **GitHub Releases에서 생성된 릴리즈 확인**

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
- **GitHub Releases** (태그 푸시 시)

## ⚠️ 문제 해결

### 일반적인 문제들

#### 1. 네트워크 연결 문제
```
Could not GET 'https://dl.google.com/...'
```
**해결책**:
- GitHub Actions는 자동으로 재시도됩니다
- 로컬 빌드 시 네트워크 연결 확인

#### 2. 메모리 부족 오류
```
OutOfMemoryError
```
**해결책**:
- `gradle.properties`에서 메모리 설정 확인:
  ```properties
  org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=512m
  ```

#### 3. 빌드 시간 초과
**해결책**:
- Gradle 데몬 사용: `./gradlew --daemon`
- 병렬 빌드 활성화: `org.gradle.parallel=true`

#### 4. 의존성 해결 실패
**해결책**:
- 캐시 클리어: `./gradlew clean`
- Gradle 래퍼 업데이트: `./gradlew wrapper --gradle-version latest`

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
1. **Build APK 워크플로 재시도**
2. **로그 확인**: Actions 탭에서 구체적인 오류 메시지 찾기
3. **캐시 클리어**: 새로운 빌드로 재시도
4. **로컬 빌드 시도**: `./gradlew clean assembleDebug`

### 추가 도움이 필요한 경우
- **Issues 탭**에서 새로운 이슈 생성
- **로그 전체 내용** 포함
- **사용한 워크플로 명시**
- **환경 정보** 제공

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

**이 가이드로 안정적이고 오류 없는 APK 빌드가 가능합니다!** 🎉