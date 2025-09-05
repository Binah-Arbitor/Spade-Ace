# GitHub Actions Workflows for Spade Ace

이 디렉토리에는 Spade Ace 안드로이드 앱을 위한 GitHub Actions 워크플로우가 포함되어 있습니다.

## 워크플로우 설명

### 1. `build-apk.yml` - 메인 빌드 워크플로우
**트리거**: Push to main/develop, PR, 수동 실행
**목적**: 디버그/릴리즈 APK 빌드, 테스트 실행, 린트 검사

**기능**:
- ✅ 자동 JDK 17 설정
- ✅ Gradle 캐싱으로 빌드 속도 향상
- ✅ 디버그/릴리즈 APK 빌드 (선택 가능)
- ✅ 버전 정보 자동 추출 및 APK 파일명 변경
- ✅ 빌드된 APK를 아티팩트로 업로드
- ✅ 단위 테스트 실행
- ✅ 린트 검사 실행
- ✅ 실패 시 빌드 리포트 업로드

**수동 실행 옵션**:
- `debug`: 디버그 APK만 빌드
- `release`: 릴리즈 APK만 빌드  
- `both`: 디버그/릴리즈 APK 모두 빌드 (기본값)

### 2. `release-build.yml` - 릴리즈 빌드 워크플로우
**트리거**: GitHub 릴리즈 생성, 수동 실행
**목적**: 프로덕션용 서명된 APK 생성

**기능**:
- ✅ 서명된 릴리즈 APK 빌드 (키스토어 있는 경우)
- ✅ 서명되지 않은 APK 빌드 (폴백)
- ✅ SHA256/MD5 체크섬 생성
- ✅ GitHub 릴리즈에 APK 자동 업로드
- ✅ 보안 취약점 검사 (선택사항)

**필요한 시크릿** (서명된 APK용):
- `KEYSTORE_BASE64`: Base64로 인코딩된 키스토어 파일
- `KEYSTORE_PASSWORD`: 키스토어 비밀번호
- `KEY_ALIAS`: 키 별칭
- `KEY_PASSWORD`: 키 비밀번호

### 3. `pr-validation.yml` - PR 검증 워크플로우  
**트리거**: Pull Request 생성/업데이트
**목적**: 빠른 코드 검증 및 빌드 확인

**기능**:
- ✅ 빠른 컴파일 검사
- ✅ 단위 테스트 실행
- ✅ 린트 검사
- ✅ 코드 포맷팅 검사 (ktlint)
- ✅ 정적 분석 (detekt)
- ✅ PR에 빌드 상태 코멘트 자동 추가

## 사용 방법

### 자동 빌드
1. `main` 또는 `develop` 브랜치에 코드 푸시
2. GitHub Actions가 자동으로 APK 빌드
3. Actions 탭에서 빌드 상태 확인
4. 완료된 빌드에서 APK 다운로드

### 수동 빌드
1. GitHub 저장소의 "Actions" 탭 이동
2. "Build APK" 워크플로우 선택
3. "Run workflow" 클릭
4. 빌드 타입 선택 (debug/release/both)
5. "Run workflow" 버튼 클릭

### 릴리즈 빌드
1. GitHub에서 새 릴리즈 생성
2. 태그와 릴리즈 노트 작성
3. "Publish release" 클릭
4. 자동으로 서명된 APK가 릴리즈에 업로드됨

## 빌드 결과물

### Debug APK
- **위치**: Actions > Artifacts > "SpadeAce-Debug-APK"
- **파일명**: `SpadeAce-{version}-debug.apk`
- **용도**: 개발 및 테스트

### Release APK  
- **위치**: Actions > Artifacts > "SpadeAce-Release-APK"
- **파일명**: `SpadeAce-{version}-release.apk`
- **용도**: 프로덕션 배포

## 설정 방법

### 키스토어 설정 (릴리즈 서명용)
1. 안드로이드 키스토어 생성:
   ```bash
   keytool -genkey -v -keystore spadeace-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias spadeace
   ```

2. 키스토어를 Base64로 인코딩:
   ```bash
   base64 spadeace-key.jks > keystore-base64.txt
   ```

3. GitHub 저장소 Settings > Secrets and variables > Actions에서 시크릿 추가:
   - `KEYSTORE_BASE64`: 인코딩된 키스토어 내용
   - `KEYSTORE_PASSWORD`: 키스토어 비밀번호
   - `KEY_ALIAS`: 키 별칭 (예: spadeace)
   - `KEY_PASSWORD`: 키 비밀번호

### 추가 도구 설정 (선택사항)
프로젝트에 다음 도구들을 추가하면 더 강력한 코드 품질 검사가 가능합니다:

1. **ktlint** (코드 포맷팅):
   ```gradle
   // app/build.gradle에 추가
   apply plugin: 'org.jlleitschuh.gradle.ktlint'
   ```

2. **detekt** (정적 분석):
   ```gradle
   // app/build.gradle에 추가
   apply plugin: 'io.gitlab.arturbosch.detekt'
   ```

## 문제 해결

### 빌드 실패 시
1. Actions 탭에서 실패한 빌드 클릭
2. 빌드 로그 확인
3. "build-reports" 아티팩트 다운로드하여 상세 오류 확인

### 캐시 문제 시
1. Actions 탭 > Caches에서 Gradle 캐시 삭제
2. 워크플로우 재실행

### 권한 문제 시
- 저장소 Settings > Actions > General에서 워크플로우 권한 확인
- "Read and write permissions" 활성화 필요

## 성능 최적화

현재 워크플로우는 다음과 같은 최적화를 포함합니다:
- ✅ Gradle 의존성 캐싱
- ✅ JDK 캐싱
- ✅ 병렬 작업 실행
- ✅ 필요한 경우에만 APK 빌드

## 보안

- ✅ 키스토어는 GitHub Secrets로 안전하게 저장
- ✅ 의존성 취약점 자동 검사
- ✅ Gradle wrapper 검증
- ✅ 서명된 APK의 체크섬 제공