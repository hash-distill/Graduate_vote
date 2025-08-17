import Header from "../../compoments/vote/header"
import axios from "axios";
import { useNavigate } from "react-router-dom";
import styles from './waiting.module.css'

const Waiting = (() => {
    const navigate = useNavigate()
    // axios({
    //     method: 'post',//请求方式
    //     url: 'http://localhost:8081/admin/getVoteResult',//请求地址
    //     params: '',//和url一起发送的数据（如get请求）
    //     data: '',//必要参数，
    //     // 自定义请求头
    // }).then(
    //     res => {
    //         if (res.data.data.pre) {
    //             clearInterval(timer);
    //             navigate("/end", { replace: true });
    //         }
    //         else if (res.data.data.isRevote != 0 && (res.data.data.teachersNum == 0 || res.data.data.teachersNum == res.data.data.teachers_all)) {
    //             // console.log("qwq")
    //             navigate("/vote", { replace: true });
    //         }
    //     })

    var timer = setInterval(() => {
        axios({
            method: 'post',//请求方式
            url: 'http://localhost:8081/admin/getVoteResult',//请求地址
            params: '',//和url一起发送的数据（如get请求）
            data: '',//必要参数，
            // 自定义请求头
        }).then(
            res => {
                if (res.data.data.pre) {
                    clearInterval(timer);
                    navigate("/end", { replace: true });
                }
                else if (res.data.data.isRevote != 0 && (res.data.data.teachersNum == 0 || res.data.data.teachersNum == res.data.data.teachers_all)) {
                    // console.log("qwq")
                    navigate("/vote", { replace: true });
                }
            })
    }, 3000);

    return (
        <div>
            <Header></Header>
            <h2 className={styles.waiting}>投 票 完 成 ， 请 耐 心 等 待 投 票 结 果{'\u00A0\u00A0\u00A0'}.{'\u00A0\u00A0\u00A0'}.{'\u00A0\u00A0\u00A0'}. </h2>
        </div>
    );
})

export default Waiting