// routes/link.js
const express = require('express');
const router = express.Router();
const userController = require('../controllers/userController'); // ✅ 올바른 경로인지 확인

// ✅ 연동 요청 (POST /api/link)
router.post('/', userController.linkUsers);

// ✅ 연동 여부 확인 (GET /api/link/:username)
router.get('/:username', userController.getUserByUsername);

module.exports = router;
