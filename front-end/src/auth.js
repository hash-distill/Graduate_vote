// 设备标识工具：按“投票设备限一票”使用（无预分发链接/口令）
// 每台设备首次访问自动生成并持久化；同设备重复提交会被后端拒绝。
const ADMIN_TOKEN_KEY = 'vote_admin_token';
const DEVICE_ID_KEY = 'vote_device_id';

export function getAdminToken() {
  return localStorage.getItem(ADMIN_TOKEN_KEY);
}

export function setAdminToken(token) {
  if (token) {
    localStorage.setItem(ADMIN_TOKEN_KEY, token);
  } else {
    localStorage.removeItem(ADMIN_TOKEN_KEY);
  }
}

export function clearAdminToken() {
  localStorage.removeItem(ADMIN_TOKEN_KEY);
}

// 获取（不存在则生成）本设备唯一标识
export function getDeviceId() {
  let id = localStorage.getItem(DEVICE_ID_KEY);
  if (!id) {
    id = (crypto.randomUUID ? crypto.randomUUID().replace(/-/g, '') : ('d' + Date.now() + Math.random().toString(36).slice(2, 12)));
    localStorage.setItem(DEVICE_ID_KEY, id);
  }
  return id;
}

export function clearDeviceId() {
  localStorage.removeItem(DEVICE_ID_KEY);
}