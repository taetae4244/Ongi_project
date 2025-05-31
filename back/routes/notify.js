// routes/notify.js
const express = require('express');
const router = express.Router();
const notifyController = require('../controllers/notifyController');

router.post('/fall', notifyController.notifyFall);

module.exports = router;
