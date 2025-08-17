import React from "react";
import styles from './footer.module.css'
import Totalcount from "../totalcount/index"

// show 页面底部投票人数显示
const Footresult = (() => {
    return (
        <div className={styles.bottom}>
            <Totalcount></Totalcount>
        </div>
    )
})

export default Footresult