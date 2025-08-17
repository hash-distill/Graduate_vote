import React from "react";
import styles from './header.module.css'

// 标题
const Header = (() => {
    return (
        <div className={styles.top}>
            <img src="/photo/name.png" className={styles.name} srcset="" />
            <h3 className={styles.title}>研究生支教团招募投票系统</h3>
        </div>
    )
})

export default Header