const User = require('../models/User'); // 사용자 모델 임포트

// 🔹 사용자 연동 API
exports.linkUsers = async (req, res) => {
  console.log('✅ linkUsers 진입');
  console.log('📦 받은 요청 바디:', req.body);

  const { currentUsername, targetUsername } = req.body;

  try {
    const currentUser = await User.findOne({ username: currentUsername });
    const targetUser = await User.findOne({ username: targetUsername });

    if (!currentUser || !targetUser) {
      return res.status(404).json({ message: '사용자 정보를 찾을 수 없습니다.' });
    }

    // 이미 연동된 경우 방지
    if (currentUser.linkedUser || targetUser.linkedUser) {
      return res.status(400).json({ message: '이미 연동된 사용자입니다.' });
    }

    // 서로 연동
    currentUser.linkedUser = targetUser.username;
    targetUser.linkedUser = currentUser.username;

    await currentUser.save();
    await targetUser.save();

    res.status(200).json({ message: '연동 완료' });
  } catch (err) {
    console.error('❌ 연동 중 오류 발생:', err);
    res.status(500).json({ message: '서버 에러', error: err.message });
  }
};

// 🔹 ✅ 사용자 정보 조회 API (/api/user/:username)
exports.getUserByUsername = async (req, res) => {
  try {
    const user = await User.findOne({ username: req.params.username }).select('-password'); // 비밀번호 제외
    if (!user) {
      return res.status(404).json({ message: '사용자를 찾을 수 없습니다.' });
    }
    res.status(200).json(user);
  } catch (err) {
    console.error('❌ 사용자 조회 중 오류:', err);
    res.status(500).json({ message: '서버 에러', error: err.message });
  }
};
