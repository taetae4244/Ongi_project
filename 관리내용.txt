안드스튜디오 rebuild 컨트롤 F9


[개발 설계도]

1.로그인 시, 사용자의 역할(예: "senior" / "caregiver")을 서버나 로컬에서 판단

2.구현 방식
User 객체에 role 필드 추가 (ex: "caregiver", "senior")

로그인 응답(JSON)에서 역할 포함시켜서 저장

SplashActivity나 LoginActivity에서 분기 처리


[몽고디비]
추가한 항목이 있으나 이전 계정들이 존재한다면 항목 추가 전 계정들을 삭제해야
정상작동
 MongoDB는 NoSQL이기 때문에
스키마 없이도 필드가 빠진 채 저장되는 게 가능합니다.

즉, 예전에 회원가입할 때 role 없이 저장된 사용자들이 있었다면,
로그인 시 user.role이 undefined가 되고,
앱에서 다음 코드에 걸려 오류 메시지를 띄웠던 거

userSchema의 role 필드에 required: true와 enum: [...]이 설정되어 있어,
지금은 role이 반드시 포함된 상태로 저장되기 때문에 더 이상 오류가 나지 않습니다.

✅ 정리
문제 원인	예전 DB 사용자 문서에 role 필드가 없어서 앱이 인식 못함
지금 정상 이유	role이 포함된 새 사용자로 로그인했기 때문
추가 권장 사항	MongoDB Compass에서 role 없는 사용자 문서 삭제 또는 수정 권장