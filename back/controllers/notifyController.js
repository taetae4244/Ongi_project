// controllers/notifyController.js
const {google} = require('google-auth-library');
const axios = require('axios');
const path = require('path');
const serviceAccount = require(path.join(__dirname, '../config/carenect-app-96c266170dfc.json'));

async function getAccessToken() {
  const client = new google.auth.JWT(
    serviceAccount.client_email,
    null,
    serviceAccount.private_key,
    ['https://www.googleapis.com/auth/firebase.messaging']
  );
  const tokens = await client.authorize();
  return tokens.access_token;
}

exports.notifyFall = async (req, res) => {
  const {toToken, title, body} = req.body;
  try {
    const accessToken = await getAccessToken();
    const response = await axios.post(
      'https://fcm.googleapis.com/v1/projects/carenect-app/messages:send',
      {
        message: {
          token: toToken,
          notification: { title, body },
          android: { priority: "HIGH" }
        }
      },
      {
        headers: {
          Authorization: `Bearer ${accessToken}`,
          'Content-Type': 'application/json'
        }
      }
    );
    res.json({success: true, fcm: response.data});
  } catch (err) {
    res.status(500).json({success: false, error: err.toString()});
  }
};
