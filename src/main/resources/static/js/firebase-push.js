/**
 * Firebase Push Notification Setup
 * Tích hợp FCM để nhận push notifications realtime.
 *
 * Cách dùng:
 * 1. Điền firebaseConfig bên dưới (lấy từ Firebase Console > Project Settings)
 * 2. Tạo file public/firebase-messaging-sw.js (service worker)
 * 3. Include script này vào layout chính
 */

// ==================== CONFIG ====================
// TODO: Điền config từ Firebase Console
const firebaseConfig = {
    apiKey: window.FIREBASE_API_KEY || "",
    authDomain: window.FIREBASE_AUTH_DOMAIN || "",
    projectId: window.FIREBASE_PROJECT_ID || "",
    storageBucket: window.FIREBASE_STORAGE_BUCKET || "",
    messagingSenderId: window.FIREBASE_MESSAGING_SENDER_ID || "",
    appId: window.FIREBASE_APP_ID || ""
};

// Chỉ khởi tạo nếu có config
if (firebaseConfig.apiKey && firebaseConfig.apiKey !== "") {
    initFirebasePush();
}

async function initFirebasePush() {
    try {
        // Dynamic import Firebase modules
        const { initializeApp } = await import('https://www.gstatic.com/firebasejs/10.7.0/firebase-app.js');
        const { getMessaging, getToken, onMessage } = await import('https://www.gstatic.com/firebasejs/10.7.0/firebase-messaging.js');

        const app = initializeApp(firebaseConfig);
        const messaging = getMessaging(app);

        // Request permission
        const permission = await Notification.requestPermission();
        if (permission !== 'granted') {
            console.log('Push notification permission denied');
            return;
        }

        // Get FCM token
        const token = await getToken(messaging, {
            vapidKey: window.FIREBASE_VAPID_KEY || ""
        });

        if (token) {
            // Gửi token lên server
            await registerFcmToken(token);
            console.log('FCM token registered');
        }

        // Xử lý notification khi app đang mở (foreground)
        onMessage(messaging, (payload) => {
            console.log('FCM message received:', payload);
            showInAppNotification(payload.notification?.title, payload.notification?.body, payload.data);
        });

    } catch (error) {
        console.warn('Firebase push init failed:', error.message);
    }
}

async function registerFcmToken(token) {
    try {
        const csrfToken = document.cookie.match(/XSRF-TOKEN=([^;]+)/)?.[1] || '';
        await fetch('/api/notifications/register-token', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-XSRF-TOKEN': decodeURIComponent(csrfToken)
            },
            body: JSON.stringify({ fcmToken: token })
        });
    } catch (e) {
        console.warn('Failed to register FCM token:', e);
    }
}

function showInAppNotification(title, body, data) {
    // Tạo toast notification trong app
    const toast = document.createElement('div');
    toast.style.cssText = `
        position: fixed; top: 20px; right: 20px; z-index: 9999;
        background: white; border-radius: 12px; padding: 16px 20px;
        box-shadow: 0 8px 24px rgba(0,0,0,0.15); max-width: 320px;
        border-left: 4px solid #6366f1; animation: slideIn 0.3s ease;
        font-family: 'Inter', sans-serif;
    `;
    toast.innerHTML = `
        <div style="font-weight:700;color:#0f172a;margin-bottom:4px;">${title || 'Thông báo'}</div>
        <div style="font-size:0.85rem;color:#64748b;">${body || ''}</div>
        <button onclick="this.parentElement.remove()" style="
            position:absolute;top:8px;right:10px;background:none;border:none;
            cursor:pointer;color:#94a3b8;font-size:1rem;">×</button>
    `;

    // Click để navigate
    if (data?.url) {
        toast.style.cursor = 'pointer';
        toast.addEventListener('click', () => window.location.href = data.url);
    }

    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 5000);
}

// Unregister token khi logout
document.addEventListener('DOMContentLoaded', () => {
    const logoutForms = document.querySelectorAll('form[action="/logout"]');
    logoutForms.forEach(form => {
        form.addEventListener('submit', async () => {
            try {
                const csrfToken = document.cookie.match(/XSRF-TOKEN=([^;]+)/)?.[1] || '';
                await fetch('/api/notifications/unregister-token', {
                    method: 'DELETE',
                    headers: { 'X-XSRF-TOKEN': decodeURIComponent(csrfToken) }
                });
            } catch (e) { /* ignore */ }
        });
    });
});
