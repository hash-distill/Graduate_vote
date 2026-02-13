import React from "react"
import styles from './totalcount.module.css'
import { Component } from 'react';
import axios from "axios";

// 接收当前投票人数
class Totalcount extends Component {

    state = {
        teachersNum: '2',
        teachersAll: '10'
    }

    componentDidMount() {
        axios({
            method: 'post',//请求方式
            url: 'http://localhost:8081/admin/getVoteResult',//请求地址
            params: '',//和url一起发送的数据（如get请求）
            data: '',//必要参数，
            // 自定义请求头
        }).then(
            res => {
                if (res.data.msg == 'success') {
                }
                else {
                    // console.log('failed')
                }
                this.setState({
                    teachersNum: res.data.data.teachers_all - res.data.data.teachersNum,
                    teachersAll: res.data.data.teachers_all
                })
            })

        setInterval(() => {
            axios({
                method: 'post',//请求方式
                url: 'http://localhost:8081/admin/getVoteResult',//请求地址
                params: '',//和url一起发送的数据（如get请求）
                data: '',//必要参数，
                // 自定义请求头
            }).then(
                res => {
                    if (res.data.msg == 'success') {
                    }
                    else {
                        console.log('failed')
                    }
                    this.setState({
                        teachersNum: res.data.data.teachers_all - res.data.data.teachersNum,
                        teachersAll: res.data.data.teachers_all
                    })
                })
        }, 5000);
    }

    render() {
        return (
            <div>
                <div className={styles.total}>当 前 投 票 人 数 :{'\u00A0\u00A0\u00A0'}<span className={styles.count}> {this.state.teachersNum} / {this.state.teachersAll}</span></div>
            </div>

        )
    }
}

export default Totalcount
