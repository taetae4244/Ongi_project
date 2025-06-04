const express = require('express');
const path = require('path');
const connectDB = require('./config/db');
const authRoutes = require('./routes/auth');
const userRoutes = require('./routes/user'); // ✅ 사용자 연동용 라우터
const linkRoutes = require('./routes/link'); // ✅ 연동용 라우터
const dotenv = require('dotenv');
const cors = require('cors');



dotenv.config();
const app = express();
connectDB();

app.use(cors());
app.use(express.json());

// ✅ 정적 HTML 파일 경로
app.use(express.static(path.join(__dirname, 'public')));

// ✅ API 라우트
app.use('/api', authRoutes);            // 로그인, 회원가입 등
app.use('/api/user', userRoutes);       // 유저 관련 기타
app.use('/api/link', linkRoutes);       // 연동용

// 자이로센서
app.post('/api/gyro-alert', (req, res) => {
  const { uid, gyro_x, gyro_y, gyro_z, timestamp } = req.body;
  console.log('✅ 자이로 알림 수신:', { uid, gyro_x, gyro_y, gyro_z, timestamp });

  // 여기서 DB에 저장하거나, 보호자에게 FCM 등 푸시 알림도 가능

  res.json({ success: true, message: "알림 수신 완료" });
});

// GET 방식 테스트 엔드포인트
// 예시: http://localhost:5000/api/gyro-alert?uid=testuser&gyro_x=3.5&gyro_y=1.2&gyro_z=0.8&timestamp=1717485600000
app.get('/api/gyro-alert', (req, res) => {
  const { uid, gyro_x, gyro_y, gyro_z, timestamp } = req.query;
  console.log('✅ [GET] 자이로 알림 수신:', { uid, gyro_x, gyro_y, gyro_z, timestamp });

  // 필요하다면 여기서 DB 저장, 알림 전송 등 추가
  res.json({ success: true, message: "GET 알림 수신 완료", data: { uid, gyro_x, gyro_y, gyro_z, timestamp } });
});

const PORT = process.env.PORT || 5000;
app.listen(PORT, () => {
  console.log(`✅ 서버 실행 중: http://localhost:${PORT}`);
});

console.log("✅ JWT_SECRET:", process.env.JWT_SECRET);
