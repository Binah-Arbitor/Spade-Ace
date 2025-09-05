# GitHub Actions Workflows for Spade Ace

이 디렉토리에는 Spade Ace 안드로이드 앱을 위한 간단하고 효율적인 GitHub Actions 워크플로우가 포함되어 있습니다.

## 워크플로우 설명

### `apk-builder.yml` - 통합 APK 빌더
**트리거**: Push to main/develop, Pull Request, 수동 실행
**목적**: 깔끔하고 안정적인 APK 빌드

**기능**:
- ✅ 자동 JDK 17 설정
- ✅ Android SDK 설정 (API 34)
- ✅ Gradle 캐싱으로 빌드 속도 향상
- ✅ 디버그/릴리즈 APK 빌드 (선택 가능)
- ✅ 빌드된 APK를 아티팩트로 업로드
- ✅ 깔끔한 설정과 빠른 빌드

**수동 실행 옵션**:
- `debug`: 디버그 APK만 빌드
- `release`: 릴리즈 APK만 빌드  
- `both`: 디버그/릴리즈 APK 모두 빌드 (기본값)

## 사용 방법

### 자동 빌드
1. `main` 또는 `develop` 브랜치에 코드 푸시
2. GitHub Actions가 자동으로 APK 빌드
3. Actions 탭에서 빌드 상태 확인
4. 완료된 빌드에서 APK 다운로드

### 수동 빌드
1. GitHub 저장소의 "Actions" 탭 이동
2. "APK Builder" 워크플로우 선택
3. "Run workflow" 클릭
4. 빌드 타입 선택 (debug/release/both)
5. "Run workflow" 버튼 클릭

## 빌드 결과물

### Debug APK
- **위치**: Actions > Artifacts > "SpadeAce-Debug-APK"
- **용도**: 개발 및 테스트

### Release APK  
- **위치**: Actions > Artifacts > "SpadeAce-Release-APK"
- **용도**: 프로덕션 배포

## 장점

이 간소화된 워크플로우는 다음과 같은 장점을 제공합니다:
- ✅ **단순함**: 하나의 워크플로우로 모든 빌드 요구사항 충족
- ✅ **빠름**: 불필요한 복잡성 제거로 빌드 시간 단축
- ✅ **안정성**: 검증된 패턴과 최소한의 설정
- ✅ **유지보수성**: 관리해야 할 파일 수 감소