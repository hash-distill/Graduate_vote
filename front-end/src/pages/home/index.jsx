import { useEffect, useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import Header from "../../compoments/vote/header";
import styles from './home.module.css'
import { API_BASE_URL } from "../../config";
import { getAdminToken } from "../../auth";

// 系统首页：进入后先探测投票状态，避免直接暴露“请先设置参数”的报错
const Home = (() => {
    const navigate = useNavigate();
    const [state, setState] = useState('loading'); // loading | notStarted | ongoing | finished
    const [isAdmin, setIsAdmin] = useState(!!getAdminToken());

    useEffect(() => {
        axios({
            method: 'post',
            url: `${API_BASE_URL}/vote/status`,
            params: '',
            data: '',
        }).then(res => {
            const d = res.data && res.data.data;
            if (res.data && res.data.result === true && d) {
                // 已初始化：结束判定与投票页一致（pre 非空即结束）
                setState(d.pre ? 'finished' : 'ongoing');
            } else {
                setState('notStarted');
            }
        }).catch(() => setState('notStarted'));
    }, []);

    return (
        <div>
            <Header></Header>
            <div className={styles.main}>
                <div className={styles.card}>
                    <h1 className={styles.title}>研究生支教团招募投票系统</h1>
                    <p className={styles.subtitle}>正选与候补评选 · 实时投票</p>

                    {state === 'loading' && <p className={styles.status}>正在获取系统状态…</p>}

                    {state === 'notStarted' && (
                        <div>
                            <p className={styles.status}>
                                投票尚未开始，请等待管理员完成参数设置后再进行投票。
                            </p>
                            <p className={styles.status}>
                                如果您是管理员，请先登录后进入“参数设置”。
                            </p>
                            <div className={styles.btnRow}>
                                <button className={styles.primaryBtn} onClick={() => navigate('/login')}>管理员登录</button>
                            </div>
                        </div>
                    )}

                    {state === 'ongoing' && (
                        <div>
                            <p className={styles.statusOk}>投票进行中，评委请进入投票页开始投票。</p>
                            <div className={styles.btnRow}>
                                <button className={styles.primaryBtn} onClick={() => navigate('/vote')}>进入投票</button>
                                {isAdmin && <button className={styles.ghostBtn} onClick={() => navigate('/show')}>实时监控</button>}
                            </div>
                        </div>
                    )}

                    {state === 'finished' && (
                        <div>
                            <p className={styles.statusOk}>本轮投票已结束，可查看最终结果。</p>
                            <div className={styles.btnRow}>
                                <button className={styles.primaryBtn} onClick={() => navigate('/usershow')}>查看结果</button>
                                {isAdmin && <button className={styles.ghostBtn} onClick={() => navigate('/show')}>实时监控</button>}
                            </div>
                        </div>
                    )}

                    {isAdmin && (
                        <div className={styles.adminRow}>
                            <button className={styles.linkBtn} onClick={() => navigate('/set')}>管理设置</button>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
})

export default Home
