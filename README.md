## ‼️ 기획 의도
![image](https://github.com/hongju904/GiftMap/assets/74871378/b9bf53e6-0b74-4841-b571-e37f9a2b611c)

---

## 🌐 개발 환경
#### 📱 Android Studio (Kotlin)
- 사용자 UI/UX

#### 🗺 KakaoMap API
- 현재 위치 표시, 매장 검색

#### 🔥 Firebase
- 사용자 데이터 관리
- Realtime Database, Authentication, ML Kit

---

## 📝 구현 내용
#### ✔  회원가입 / 로그인
- Firebase Authentication 이용
- DB에 쌓이는 user data 경로를 분리하기 위함

#### ✔  기프티콘 item 추가 / 수정 / 삭제
- Android: RecyclerView
- Firebase: Realtime Database
- Storage 저장 대신 Bitmap binary String을 이용하여 DB에서 이미지 함께 관리

#### ✔  OCR
- Firebase ML Kit 이용
- 정규표현식 이용하여 `유효기간` 추출
- 카카오톡 기프티콘 기준으로 `상품명`과 `교환처` 추출
- 사용자의 수동 입력으로 수정 가능

#### ✔  기프티콘 item 지도에서 표현
- Kakao Map API
- HashMap 사용하여 DB의 store 정보로 search
- Retrofit HTTP로 요청

#### ✔  개발 일지
- [Velog](https://velog.io/@hongju904/Gift-Map-%EC%95%B1-%EA%B0%9C%EB%B0%9C-%EA%B8%B0%EB%A1%9D)

---

## 🎁 App 이미지
![image](https://github.com/hongju904/GiftMap/assets/74871378/fbdab12a-6d7c-46a0-a45d-5f436bb1f9f2)
