import Header from "../../compoments/vote/header"
import React from "react"
import { useNavigate } from "react-router-dom";
import styles from './end.module.css'

const End = (() => {
    const navigate = useNavigate()
    const toShow = () => {
        navigate('/usershow', { replace: true })
    }
    return (
        <div>
            <Header></Header>
            <h2 className={styles.end}>投票已结束，感谢您的参与，点击下方按钮查看结果</h2>
            <button onClick={toShow} className={styles.button}>查看结果</button>
        </div>
    );
})

export default End