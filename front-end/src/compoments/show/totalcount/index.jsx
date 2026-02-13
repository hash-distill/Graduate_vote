import React from "react"
import styles from './totalcount.module.css'
import { Component } from 'react';
import axios from "axios";
import { API_BASE_URL } from "../../../config";

// 接收当前投票人数
class Totalcount extends Component {

    state = {
        teachersNum: '2',
        teachersAll: '10'
    }

    componentDidMount() {
        axios({
            method: 'post',
            url: `${API_BASE_URL}/admin/getVoteResult`,
            params: '',
            data: '',
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

        this.timer = setInterval(() => {
            axios({
                method: 'post',
                url: `${API_BASE_URL}/admin/getVoteResult`,
                params: '',
                data: '',
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

    componentWillUnmount() {
        if (this.timer) {
            clearInterval(this.timer);
        }
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
