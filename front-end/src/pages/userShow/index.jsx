import React from "react"
import styles from './userShow.module.css'
import axios from "axios";
import { Component } from 'react';
import Header from "../../compoments/vote/header";

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
                // console.log(this.state.revoteResult);
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
                        // console.log('failed')
                    }
                    this.setState({
                        student: res.data.data.students,
                        determineNum: res.data.data.determineNum,
                        pre: res.data.data.pre == null ? [] : res.data.data.pre,
                        revoteResult: res.data.data.revoteResult == null ? {} : res.data.data.revoteResult,
                        preRevoteResult: res.data.data.preRevoteResult == null ? {} : res.data.data.preRevoteResult,
                    })
                    // console.log(this.state.revoteResult);
                })
        }, 5000);    
    }

    render() {
        var i = 0;
        return (
            <div className={styles.userShow}>
                <Header></Header>
                <h1>第 26 届研究生支教团选拔结果</h1>
                <table>
                    <tr className={styles.tablehead}>
                        <th style={{ minWidth: 80, WebkitBorderTopLeftRadius: 15 }}>排名</th>
                        <th style={{ minWidth: 260 }}>学院</th>
                        <th style={{ minWidth: 100, WebkitBorderTopRightRadius: 15 }}>姓名</th>
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
