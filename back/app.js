const express = require('express');
const path = require('path');
const connectDB = require('./config/db');
const authRoutes = require('./routes/auth');
const userRoutes = require('./routes/user'); // ✅ 사용자 연동용 라우터
const linkRoutes = require('./routes/link'); // ✅ 연동용 라우터
const dotenv = require('dotenv');
const cors = require('cors');
const alertRoutes = require('./routes/alert');
app.use('/api', alertRoutes);


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
app.use('/api/link', linkRoutes);       // 연동용 라우트

const PORT = process.env.PORT || 5000;
app.listen(PORT, () => {
  console.log(`✅ 서버 실행 중: http://localhost:${PORT}`);
});

console.log("✅ JWT_SECRET:", process.env.JWT_SECRET);
