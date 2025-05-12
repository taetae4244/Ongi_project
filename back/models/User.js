const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
  username: {
    type: String,
    required: true,
    unique: true,
  },
  password: {
    type: String,
    required: true,
  },
  email: {
    type: String,
    required: true,
    unique: true,
  },
  phone: {
    type: String,
    required: true,
  },
  role: {                     // ← ✅ 추가
    type: String,
    enum: ['guardian', 'senior'], // 잘못된 값 저장 방지
    required: true
  },
  linkedUser: {              // ✅ 연동된 상대방 ID(username 등)
    type: String,
    default: null
  }

}, { timestamps: true });


module.exports = mongoose.model('User', userSchema);
