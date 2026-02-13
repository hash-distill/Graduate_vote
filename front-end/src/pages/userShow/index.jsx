import React from "react"
import styles from './userShow.module.css'
import axios from "axios";
import { Component } from 'react';
import Header from "../../compoments/vote/header";
import { API_BASE_URL } from "../../config";

class UserShow extends Component {
    state = {
        student: [
            { voteId: 2, voteName: 'lisi', voteGender: 0, votePoli: 'test', voteInsti: 'test', votePoll: 5, voteInstiSort: '1', voteInterSort: '2' }
        ],
        pre: [],
        revoteResult: {},
        preRevoteResult: {},
        determineNum: 0
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
                    student: res.data.data.students,
                    determineNum: res.data.data.determineNum,
                    pre: res.data.data.pre == null ? [] : res.data.data.pre,
                    revoteResult: res.data.data.revoteResult == null ? {} : res.data.data.revoteResult,
                    preRevoteResult: res.data.data.preRevoteResult == null ? {} : res.data.data.preRevoteResult,
                })
                if (res.data.data.pre != null) {
                    this.setState({
                        title: "投票结果",
                        message: ""
                    })
                }
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
                        // console.log('failed')
                    }
                    this.setState({
                        student: res.data.data.students,
                        determineNum: res.data.data.determineNum,
                        pre: res.data.data.pre == null ? [] : res.data.data.pre,
                        revoteResult: res.data.data.revoteResult == null ? {} : res.data.data.revoteResult,
                        preRevoteResult: res.data.data.preRevoteResult == null ? {} : res.data.data.preRevoteResult,
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
        var i = 0;
        return (
            <div className={styles.userShow}>
                <Header></Header>
                <h1>第 26 届研究生支教团选拔结果</h1>
                <table>
                    <tr className={styles.tablehead}>
                        <th style={{ minWidth: 80, borderTopLeftRadius: 15 }}>序号</th>
                        <th style={{ minWidth: 260 }}>学院</th>
                        <th style={{ minWidth: 100, borderTopRightRadius: 15 }}>姓名</th>
                    </tr>
                    {this.state.student.map(item => {
                        i++;
                        return (
                            <tr className={styles.student}>
                                <td>
                                    <span className={styles.message}>{i} </span>
                                </td>
                                <td>
                                    <span className={styles.message}>{item.voteInsti} </span>
                                </td>
                                <td>
                                    <span className={styles.message}>{item.voteName} </span>
                                </td>
                            </tr>
                        )
                    })}

                    {this.state.pre.map(item => {
                        i++;
                        return (
                            <tr className={styles.student}>
                                <td>
                                    <span className={styles.message}>{i} </span>
                                </td>
                                <td>
                                    <span className={styles.message}>{item.voteInsti} </span>
                                </td>
                                <td>
                                    <span className={styles.message}>{item.voteName} </span>
                                </td>
                            </tr>
                        )
                    })}
                    <div className={styles.blank}></div>
                </table>
            </div>
        )
    }
}

export default UserShow
