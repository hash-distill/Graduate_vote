import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App.jsx'
import './index.css'
import axios from 'axios'
import { getAdminToken, clearAdminToken, getDeviceId } from './auth'
//config.js对 i18n 进行初始化操作及插件配置
import { BrowserRouter } from "react-router-dom";

// 全局 axios：
// - /admin/** 附带管理员会话 token；401 时清理并跳转登录页
// - /users、/vote 附带本设备标识（X-Device-Id），实现“按设备限一票”
axios.interceptors.request.use(config => {
  config.headers = config.headers || {};
  const token = getAdminToken();
  if (token) {
    config.headers['X-Admin-Token'] = token;
  }
  const url = config.url || '';
  if (url.indexOf('/users') >= 0 || url.indexOf('/vote') >= 0) {
    config.headers['X-Device-Id'] = getDeviceId();
  }
  return config;
});

axios.interceptors.response.use(
  response => response,
  error => {
    if (error.response && error.response.status === 401) {
      clearAdminToken();
      if (window.location.pathname !== '/login') {
        window.location.replace('/login');
      }
    }
    return Promise.reject(error);
  }
);

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </React.StrictMode>,
)