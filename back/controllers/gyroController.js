const User = require('../models/User');
const admin = require('firebase-admin');

// FCM Admin 초기화는 앱 실행 시 한 번만 해야 합니다.
// app.js 등에서 이미 했다면 여기선 할 필요 없음

exports.receiveGyroAlert = async (req, res) => {
  try {
    const { uid, gyro_x, gyro_y, gyro_z, timestamp } = req.body;
    console.log('📡 자이로 알림 수신:', { uid, gyro_x, gyro_y, gyro_z, timestamp });

    // 1. 피보호자 → 연동 보호자 username 찾기
    const senior = await User.findOne({ username: uid });
    if (!senior || !senior.linkedUser) {
      return res.status(404).json({ message: '연동된 보호자 정보 없음' });
    }

    // 2. 보호자 FCM 토큰 조회
    const caregiver = await User.findOne({ username: senior.linkedUser });
    if (!caregiver || !caregiver.fcmToken) {
      return res.status(404).json({ message: '보호자 FCM 토큰 없음' });
    }

    // 3. FCM 메시지 작성
    const message = {
      token: caregiver.fcmToken,
      notification: {
        title: '긴급 움직임 감지',
        body: `피보호자 ${uid}에서 급격한 움직임이 감지되었습니다.`
      },
      data: {
        uid: uid,
        gyro_x: gyro_x?.toString() ?? "",
        gyro_y: gyro_y?.toString() ?? "",
        gyro_z: gyro_z?.toString() ?? "",
        timestamp: timestamp?.toString() ?? ""
      }
    };

    // 4. FCM 전송
    const response = await admin.messaging().send(message);
    console.log('✅ FCM 전송 성공:', response);

    res.json({ success: true, message: "알림 수신 및 보호자에게 FCM 전송 완료" });
  } catch (err) {
    console.error('❌ FCM 전송 실패:', err.message);
    res.status(500).json({ message: '서버 오류', error: err.message });
  }
};
