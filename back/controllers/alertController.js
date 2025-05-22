const admin = require('firebase-admin');

// 서비스 계정 키로 초기화 (앱이 여러 번 초기화되지 않도록 체크)
const serviceAccount = require('../config/carenect-app-1963dee34c31.json');
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: 'https://carenect-app-default-rtdb.firebaseio.com' // 본인 프로젝트에 맞게!
});

exports.sendAlert = async (req, res) => {
  const { targetUser } = req.body;
  if (!targetUser) {
    return res.status(400).json({ success: false, message: 'targetUser is required' });
  }

  try {
    // 1. Firebase Realtime Database에서 FCM 토큰 읽기
    const tokenRef = admin.database().ref(`tokens/${targetUser}`);
    const snapshot = await tokenRef.once('value');
    const token = snapshot.val();

    if (!token) {
      return res.status(404).json({ success: false, message: 'No FCM token found for user' });
    }

    // 2. FCM 알림 메시지 생성
    const message = {
      notification: {
        title: '긴급 호출',
        body: '보호자가 긴급 호출을 보냈습니다!'
      },
      token: token,
      android: {
        priority: 'high',
        notification: {
          sound: 'siren',
          channel_id: 'emergency_channel'
        }
      }
    };

    // 3. FCM으로 알림 전송
    const response = await admin.messaging().send(message);
    return res.json({ success: true, response });

  } catch (error) {
    console.error('FCM 전송 오류:', error);
    return res.status(500).json({ success: false, error: error.message });
  }
};