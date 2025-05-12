const User = require('../models/User');
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');

exports.register = async (req, res) => {
  try {
    const { username, password, password_confirm, email, phone, role } = req.body;

    console.log('📌 회원가입 요청 받은 역할 role:', role);

    // 비밀번호 확인 검사
    if (password !== password_confirm) {
      return res.status(400).json({ message: '비밀번호가 일치하지 않습니다.' });
    }

    // 중복 아이디 검사
    const existingUser = await User.findOne({ username });
    if (existingUser) {
      return res.status(409).json({ message: '이미 존재하는 아이디입니다.' });
    }

    // 중복 이메일 검사
    const existingEmail = await User.findOne({ email });
    if (existingEmail) {
      return res.status(409).json({ message: '이미 사용 중인 이메일입니다.' });
    }

    // 비밀번호 암호화
    const hashedPassword = await bcrypt.hash(password, 10);

    // 유저 저장
    const newUser = new User({
      username,
      password: hashedPassword,
      email,
      phone,
      role
    });
    await newUser.save();

    res.status(201).json({ message: '회원가입 성공' });
  } catch (err) {
    res.status(500).json({ message: '서버 에러', error: err.message });
  }
};

exports.login = async (req, res) => {
  try {
    const { username, password } = req.body;

    // ✅ 로그인 요청 확인 로그
    console.log('📥 로그인 요청 - username:', username);
    console.log('📥 로그인 요청 - password:', password);

    // 사용자 조회
    const user = await User.findOne({ username });
    if (!user) {
      console.log('❌ 사용자 없음');
      return res.status(401).json({ message: '존재하지 않는 사용자입니다.' });
    }

    // 비밀번호 확인
    const isMatch = await bcrypt.compare(password, user.password);
    if (!isMatch) {
      console.log('❌ 비밀번호 불일치');
      return res.status(401).json({ message: '비밀번호가 틀렸습니다.' });
    }

    // JWT 발급
    const token = jwt.sign(
      { id: user._id, username: user.username },
      process.env.JWT_SECRET,
      { expiresIn: '1h' }
    );

    // 로그인 성공 응답 (역할 포함)
    res.status(200).json({
      message: '로그인 성공',
      token,
      role: user.role
    });

  } catch (err) {
    console.error('❌ 로그인 처리 중 오류:', err.message);
    res.status(500).json({ message: '서버 에러', error: err.message });
  }
};
