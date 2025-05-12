// 회원가입, 로그인 등 인증관련 라우트 전담
// auth.js >> post /signup, post /login

const express = require('express');
const router = express.Router();
const authController = require('../controllers/authController');

router.post('/signup', authController.register);
router.post('/login', authController.login);

module.exports = router;
