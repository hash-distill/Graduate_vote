import React from "react"
import styles from './result.module.css'
import axios from "axios";
import { Component } from 'react';

var all = []

function Setall() {
    if (all == null || all.length == 0) {
        return;
    }
    else {
        let i = 0;
        return (
            <div>
                <h1 style={{ marginTop: 80 }}>第 1 次投票结果</h1>
                <h2 className={styles.tip}>本次投票限投 25 票</h2>
                <table>
                    <tr className={styles.tablehead}>
                        <th style={{ WebkitBorderTopLeftRadius: 15 }}>排名</th>
                        <th style={{ minWidth: 260 }}>学院</th>
                        <th style={{ minWidth: 220 }}>专业</th>
                        <th style={{ minWidth: 80 }}>姓名</th>
                        <th>性别</th>
                        <th style={{ minWidth: 140 }}>政治面貌</th>
                        <th>学院排序</th>
                        <th>面试序号</th>
                        <th style={{ WebkitBorderTopRightRadius: 15 }}>票数</th>
                    </tr>
                    {all.map(item => {
                        // console.log(item)
                        i++;
                        // if (item.votePoll == 0 && i > 10) return;
                        let sex = '男';
                        if (item.voteGender == 0) {
                            sex = '女';
                        }
                        return (
                            <tr className={styles.student}>
                                <td>
                                    <span className={styles.message}>{i} </span>
                                </td>
                                <td>
                                    <span className={styles.message}>{item.voteInsti} </span>
                                </td>
                                <td>
                                    <span className={styles.message}>{item.voteMajor} </span>
                                </td>
                                <td>
                                    <span className={styles.message}>{item.voteName} </span>
                                </td>
                                <td>
                                    <span className={styles.message}>{sex} </span>
                                </td>
                                <td>
                                    <span className={styles.message}>{item.votePoli} </span>
                                </td>
                                <td>
                                    <span className={styles.message}>{item.voteInstiSort} </span>
                                </td>
                                <td>
                                    <span className={styles.message}>({item.voteInterSort}) </span>
                                </td>
                                <td>
                                    <span className={styles.message}>{item.votePoll} </span>
                                </td>
                            </tr>
                        )
                    })}
                </table>
            </div>
        )
    }
}

class Result extends Component {
    state = {
        student: [
            { voteId: 2, voteName: 'lisi', voteGender: 0, votePoli: 'test', voteInsti: 'test', votePoll: 5, voteInstiSort: '1', voteInterSort: '2' }
        ],
        pre: [],
        revoteResult: {},
        preRevoteResult: {},
        determineNum: 0,
        message: "",
        title: "当前投票",
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
                    console.log('failed')
                }
                this.setState({
                    student: res.data.data.students,
                    determineNum: res.data.data.determineNum,
                    pre: res.data.data.pre == null ? [] : res.data.data.pre,
                    revoteResult: res.data.data.revoteResult == null ? {} : res.data.data.revoteResult,
                    preRevoteResult: res.data.data.preRevoteResult == null ? {} : res.data.data.preRevoteResult,
                    message: `当前第 ${(this.state.determineNum + 1)} 名至第 ${(this.state.determineNum + this.state.student.length)} 名平票，还需选出 ${res.data.data.limit} 名`
                })
                all = res.data.data.all == null ? [] : res.data.data.all
                if (res.data.data.pre != null) {
                    this.setState({
                        title: "投票结果",
                        message: ""
                    })
                }
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
                        student: res.data.data.students,
                        determineNum: res.data.data.determineNum,
                        pre: res.data.data.pre == null ? [] : res.data.data.pre,
                        revoteResult: res.data.data.revoteResult == null ? {} : res.data.data.revoteResult,
                        preRevoteResult: res.data.data.preRevoteResult == null ? {} : res.data.data.preRevoteResult,
                        message: `当前第 ${(this.state.determineNum + 1)} 名至第 ${(this.state.determineNum + this.state.student.length)} 名平票，还需选出 ${res.data.data.limit} 名`
                    })
                    all = res.data.data.all == null ? [] : res.data.data.all
                    if (res.data.data.pre != null) {
                        this.setState({
                            title: "投票结果",
                            message: ""
                        })
                    }
                })
        }, 5000);
    }
    render() {
        var i = 0;
        var j = 0;
        return (
            <div className={styles.result}>
                <Setall></Setall>

                {Object.keys(this.state.revoteResult).map((v, i) => {
                    i = 0;
                    j++;
                    return (
                        <div>
                            <h1 style={{ marginTop: 80 }}>第 {j} 次重投【入围人选】</h1>
                            <h2 className={styles.tip}>本次投票限投 {this.state.revoteResult[v].limit} 票</h2>
                            <table>
                                <tr className={styles.tablehead}>
                                    <th style={{ WebkitBorderTopLeftRadius: 15 }}>排名</th>
                                    <th style={{ minWidth: 260 }}>学院</th>
                                    <th style={{ minWidth: 220 }}>专业</th>
                                    <th style={{ minWidth: 80 }}>姓名</th>
                                    <th>性别</th>
                                    <th style={{ minWidth: 140 }}>政治面貌</th>
                                    <th>学院排序</th>
                                    <th>面试序号</th>
                                    <th style={{ WebkitBorderTopRightRadius: 15 }}>票数</th>
                                </tr>
                                {this.state.revoteResult[v].revoteList.map(item => {
                                    i++;
                                    let sex = '男';
                                    if (item.voteGender == 0) {
                                        sex = '女';
                                    }
                                    return (
                                        <tr className={styles.student}>
                                            <td>
                                                <span className={styles.message}>{i} </span>
                                            </td>
                                            <td>
                                                <span className={styles.message}>{item.voteInsti} </span>
                                            </td>
                                            <td>
                                                <span className={styles.message}>{item.voteMajor} </span>
                                            </td>
                                            <td>
                                                <span className={styles.message}>{item.voteName} </span>
                                            </td>
                                            <td>
                                                <span className={styles.message}>{sex} </span>
                                            </td>
                                            <td>
                                                <span className={styles.message}>{item.votePoli} </span>
                                            </td>
                                            <td>
                                                <span className={styles.message}>{item.voteInstiSort} </span>
                                            </td>
                                            <td>
                                                <span className={styles.message}>({item.voteInterSort}) </span>
                                            </td>
                                            <td>
                                                <span className={styles.message}>{item.votePoll} </span>
                                            </td>
                                        </tr>
                                    )
                                })}
                            </table>
                        </div>
                    )
                })}

                {Object.keys(this.state.preRevoteResult).map((v, i) => {
                    i = 0;
                    j++;

                    return (
                        <div>
                            <h1 style={{ marginTop: 80 }}>第 {j} 次重投【候补人选】</h1>
                            <h2 className={styles.tip}>本次投票限投 {this.state.preRevoteResult[v].limit} 票</h2>
                            <table>
                                <tr className={styles.tablehead}>
                                    <th style={{ WebkitBorderTopLeftRadius: 15 }}>排名</th>
                                    <th style={{ minWidth: 260 }}>学院</th>
                                    <th style={{ minWidth: 220 }}>专业</th>
                                    <th style={{ minWidth: 80 }}>姓名</th>
                                    <th>性别</th>
                                    <th style={{ minWidth: 140 }}>政治面貌</th>
                                    <th>学院排序</th>
                                    <th>面试序号</th>
                                    <th style={{ WebkitBorderTopRightRadius: 15 }}>票数</th>
                                </tr>
                                {this.state.preRevoteResult[v].revoteList.map(item => {
                                    i++;
                                    let sex = '男';
                                    if (item.voteGender == 0) {
                                        sex = '女';
                                    }
                                    return (
                                        <tr className={styles.student}>
                                            <td>
                                                <span className={styles.message}>{i} </span>
                                            </td>
                                            <td>
                                                <span className={styles.message}>{item.voteInsti} </span>
                                            </td>
                                            <td>
                                                <span className={styles.message}>{item.voteMajor} </span>
                                            </td>
                                            <td>
                                                <span className={styles.message}>{item.voteName} </span>
                                            </td>
                                            <td>
                                                <span className={styles.message}>{sex} </span>
                                            </td>
                                            <td>
                                                <span className={styles.message}>{item.votePoli} </span>
                                            </td>
                                            <td>
                                                <span className={styles.message}>{item.voteInstiSort} </span>
                                            </td>
                                            <td>
                                                <span className={styles.message}>({item.voteInterSort}) </span>
                                            </td>
                                            <td>
                                                <span className={styles.message}>{item.votePoll} </span>
                                            </td>
                                        </tr>
                                    )
                                })}
                            </table>
                        </div>
                    )
                })}

                <h1 style={{ marginTop: 80 }}>{this.state.title}</h1>
                <h2 className={styles.tip}>{this.state.message}</h2>
                <table>
                    <tr className={styles.tablehead}>
                        <th style={{ WebkitBorderTopLeftRadius: 15 }}>排名</th>
                        <th style={{ minWidth: 260 }}>学院</th>
                        <th style={{ minWidth: 220 }}>专业</th>
                        <th style={{ minWidth: 80 }}>姓名</th>
                        <th>性别</th>
                        <th style={{ minWidth: 140 }}>政治面貌</th>
                        <th>学院排序</th>
                        <th>面试序号</th>
                        <th style={{ WebkitBorderTopRightRadius: 15 }}>票数</th>
                    </tr>
                    {this.state.student.map(item => {
                        i++
                        let sex = '男'
                        if (item.voteGender == 0) {
                            sex = '女'
                        }
                        return (
                            <tr className={styles.student}>
                                <td>
                                    <span className={styles.message}>{i} </span>
                                </td>
                                <td>
                                    <span className={styles.message}>{item.voteInsti} </span>
                                </td>
                                <td>
                                    <span className={styles.message}>{item.voteMajor} </span>
                                </td>
                                <td>
                                    <span className={styles.message}>{item.voteName} </span>
                                </td>
                                <td>
                                    <span className={styles.message}>{sex} </span>
                                </td>
                                <td>
                                    <span className={styles.message}>{item.votePoli} </span>
                                </td>
                                <td>
                                    <span className={styles.message}>{item.voteInstiSort} </span>
                                </td>
                                <td>
                                    <span className={styles.message}>({item.voteInterSort}) </span>
                                </td>
                                <td>
                                    <span className={styles.message}>{item.votePoll} </span>
                                </td>
                            </tr>
                        )
                    })}
                    {this.state.pre.map(item => {
                        i++
                        let sex = '男'
                        if (item.voteGender == 0) {
                            sex = '女'
                        }
                        return (
                            <tr className={styles.student}>
                                <td>
                                    <span className={styles.message}>{i} </span>
                                </td>
                                <td>
                                    <span className={styles.message}>{item.voteInsti} </span>
                                </td>
                                <td>
                                    <span className={styles.message}>{item.voteMajor} </span>
                                </td>
                                <td>
                                    <span className={styles.message}>{item.voteName} </span>
                                </td>
                                <td>
                                    <span className={styles.message}>{sex} </span>
                                </td>
                                <td>
                                    <span className={styles.message}>{item.votePoli} </span>
                                </td>
                                <td>
                                    <span className={styles.message}>{item.voteInstiSort} </span>
                                </td>
                                <td>
                                    <span className={styles.message}>({item.voteInterSort}) </span>
                                </td>
                                <td>
                                    <span className={styles.message}>{item.votePoll} </span>
                                </td>
                            </tr>
                        )
                    })}
                </table>
            </div>
        )
    }
}

export default Result
