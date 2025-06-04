const express = require('express');
const router = express.Router();
const gyroController = require('../controllers/gyroController');

// POST /api/gyro-alert
router.post('/gyro-alert', gyroController.receiveGyroAlert);

module.exports = router;
