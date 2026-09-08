import { useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import styles from './login.module.css'
import { API_BASE_URL } from "../../config";
import { setAdminToken } from "../../auth";

// 管理员登录（鉴权设计见 docs/投票系统鉴权与身份投票设计.md §4）
const Login = (() => {
    const [password, setPassword] = useState("");
    const [msg, setMsg] = useState("");
    const navigate = useNavigate();

    const doLogin = () => {
        setMsg("");
        axios({
            method: 'post',
            url: `${API_BASE_URL}/admin/login`,
            headers: { 'Content-Type': 'application/json' },
            data: JSON.stringify({ password }),
        }).then(res => {
            if (res.data.result === true) {
                setAdminToken(res.data.data.token);
                navigate("/set", { replace: true });
            } else {
                setMsg(res.data.msg || "登录失败");
            }
        }).catch(() => {
            setMsg("无法连接服务器，请确认后端已启动");
        });
    };

    return (
        <div className={styles.main}>
            <div className={styles.card}>
                <h2 className={styles.title}>管理员登录</h2>
                <input
                    type="password"
                    className={styles.input}
                    placeholder="请输入管理口令"
                    value={password}
                    onChange={e => setPassword(e.target.value)}
                    onKeyDown={e => { if (e.key === 'Enter') doLogin(); }}
                />
                {msg && <p className={styles.msg}>{msg}</p>}
                <button className={styles.submitBtn} onClick={doLogin}>登 录</button>
            </div>
        </div>
    );
});

export default Login
