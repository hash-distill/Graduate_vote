import Header from "../../compoments/vote/header"
import axios from "axios";
import { useNavigate } from "react-router-dom";
import { useEffect } from "react";
import styles from './waiting.module.css'
import { API_BASE_URL } from "../../config";

const Waiting = (() => {
    const navigate = useNavigate()

    useEffect(() => {
        const timer = setInterval(() => {
            axios({
                method: 'post',
                url: `${API_BASE_URL}/vote/status`,
                params: '',
                data: '',
            }).then(
                res => {
                    const d = res.data && res.data.data;
                    if (!d) return;   // 后端未初始化/失败：保持等待，下一轮再试
                    if (d.pre) {
                        clearInterval(timer);
                        navigate("/end", { replace: true });
                    }
                    else if (d.isRevote != 0 && (d.teachersNum == 0 || d.teachersNum == d.teachers_all)) {
                        clearInterval(timer);
                        navigate("/vote", { replace: true });
                    }
                }).catch(() => { /* 网络异常：保持等待 */ })
        }, 3000);

        return () => clearInterval(timer);
    }, [navigate]);

    return (
        <div>
            <Header></Header>
            <h2 className={styles.waiting}>投 票 完 成 ， 请 耐 心 等 待 投 票 结 果{'\u00A0\u00A0\u00A0'}.{'\u00A0\u00A0\u00A0'}.{'\u00A0\u00A0\u00A0'}. </h2>
        </div>
    );
})

export default Waiting