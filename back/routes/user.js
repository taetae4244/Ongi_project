//  유저 정보 조작, 조회 등 (연동, 프로필변경, 탈퇴 등)
// user.js >> post /link, get /me, put /profile

const express = require('express');
const router = express.Router();
const userController = require('../controllers/userController');

// 연동 요청 API // post는 새로운 데이터 생성 시 사용
router.post('/link', userController.linkUsers);

// ✅ 사용자 정보 조회 API 추가 // get은 서버에 정보를 요청,조회 할때 사용
router.get('/:username', userController.getUserByUsername);

module.exports = router;
