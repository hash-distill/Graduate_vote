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
                url: `${API_BASE_URL}/admin/getVoteResult`,
                params: '',
                data: '',
            }).then(
                res => {
                    if (res.data.data.pre) {
                        clearInterval(timer);
                        navigate("/end", { replace: true });
                    }
                    else if (res.data.data.isRevote != 0 && (res.data.data.teachersNum == 0 || res.data.data.teachersNum == res.data.data.teachers_all)) {
                        clearInterval(timer);
                        navigate("/vote", { replace: true });
                    }
                })
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