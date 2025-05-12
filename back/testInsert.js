const { MongoClient } = require('mongodb');
require('dotenv').config(); // ✅ 환경변수 불러오기

console.log("⚙️ 실행 시작");

const uri = process.env.MONGO_URI;
if (!uri) {
  console.error("❌ .env에서 MONGO_URI를 찾을 수 없습니다.");
  process.exit(1);
}

const client = new MongoClient(uri);

async function run() {
  try {
    console.log("🔗 DB 연결 시도 중...");
    await client.connect();
    console.log("✅ MongoDB 연결 성공");

    const db = client.db("Dahee");
    const collection = db.collection("test");

    const result = await collection.insertOne({
      message: "Hello from Dahee 👋",
      createdAt: new Date()
    });

    console.log("✅ 문서 추가 완료:", result.insertedId);
  } catch (err) {
    console.error("❌ 오류 발생:", err.message);
  } finally {
    await client.close();
  }
}

run();
