import React, { Component } from "react"
import styles from './result.module.css'
import axios from "axios";
import { API_BASE_URL } from "../../../config";

/**
 * 可复用的投票结果表格组件
 * @param {string} title - 表格标题
 * @param {string} subtitle - 副标题
 * @param {Array} data - 学生数据列表
 * @param {number} startRank - 起始排名序号（默认 1）
 */
const VoteTable = ({ title, subtitle, data, startRank = 1 }) => {
    if (!data || data.length === 0) return null;

    return (
        <div>
            {title && <h1 style={{ marginTop: 80 }}>{title}</h1>}
            {subtitle && <h2 className={styles.tip}>{subtitle}</h2>}
            <table>
                <thead>
                    <tr className={styles.tablehead}>
                        <th style={{ borderTopLeftRadius: 15 }}>排名</th>
                        <th style={{ minWidth: 260 }}>学院</th>
                        <th style={{ minWidth: 220 }}>专业</th>
                        <th style={{ minWidth: 80 }}>姓名</th>
                        <th>性别</th>
                        <th style={{ minWidth: 140 }}>政治面貌</th>
                        <th>学院排序</th>
                        <th>面试序号</th>
                        <th style={{ borderTopRightRadius: 15 }}>票数</th>
                    </tr>
                </thead>
                <tbody>
                    {data.map((item, index) => (
                        <tr key={item.voteId || index} className={styles.student}>
                            <td><span className={styles.message}>{startRank + index}</span></td>
                            <td><span className={styles.message}>{item.voteInsti}</span></td>
                            <td><span className={styles.message}>{item.voteMajor}</span></td>
                            <td><span className={styles.message}>{item.voteName}</span></td>
                            <td><span className={styles.message}>{item.voteGender === 0 ? '女' : '男'}</span></td>
                            <td><span className={styles.message}>{item.votePoli}</span></td>
                            <td><span className={styles.message}>{item.voteInstiSort}</span></td>
                            <td><span className={styles.message}>({item.voteInterSort})</span></td>
                            <td><span className={styles.message}>{item.votePoll}</span></td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
};

/**
 * 投票结果实时展示组件
 *
 * 后端数据流（saveRevote 在 getRevote 之前调用）：
 *
 *   首轮投票结束 → vote() 被调用：
 *     1. saveRevote(getAllUsers()) → revoteTimes=1, revoteResult[1] = 首轮全部学生票数快照
 *     2. getRevote() → 识别平票学生，resetVotePoll=0，设为 revote
 *     3. all = getAllUsers() → 从 DB 重新读取（也是首轮票数）
 *     ★ revoteResult[1] ≈ all → 两者实质相同，都是首轮结果
 *
 *   第一次重投结束 → vote() 再次被调用：
 *     1. saveRevote(revote候选人) → revoteTimes=2, revoteResult[2] = 第一次重投的票数快照
 *     2. getRevote() → 检查是否还有平票
 *     ★ revoteResult[2] 才是真正的"第一次重投结果"
 *
 * 因此前端显示时：
 *   - "首轮投票结果" → 使用 all（跳过 revoteResult[1]，避免重复）
 *   - "第 N 轮重投结果" → 使用 revoteResult[N+1]（key 从 2 开始，label 从 1 开始）
 *   - "当前投票/重投" → 使用 students（当前轮次的实时数据）
 */
class Result extends Component {
    state = {
        students: [],           // 当前展示的学生（实时数据）
        pre: [],                // 候补名单（投票结束后才有）
        all: [],                // 首轮投票完整结果快照
        revoteResult: {},       // 正选重投各轮结果快照
        preRevoteResult: {},    // 候补重投各轮结果快照
        determineNum: 0,        // 已确定人数
        limit: 0,               // 当前投票限投数
        isRevote: 0,            // 是否在重投中
        isPreRevote: false,     // 是否在候补重投中
        isFinished: false,      // 投票是否已全部结束
        hasData: false,         // 是否已获取到数据
    }

    /** 从后端获取投票结果并更新 state */
    fetchResult = () => {
        axios({
            method: 'post',
            url: `${API_BASE_URL}/admin/getVoteResult`,
        }).then(res => {
            if (res.data.msg !== 'success' || !res.data.data) return;

            const data = res.data.data;
            this.setState({
                students: data.students || [],
                pre: data.pre || [],
                all: data.all || [],
                revoteResult: data.revoteResult || {},
                preRevoteResult: data.preRevoteResult || {},
                determineNum: data.determineNum || 0,
                limit: data.limit || 0,
                isRevote: data.isRevote || 0,
                isPreRevote: data.isPreRevote || false,
                isFinished: data.pre != null,
                hasData: true,
            });
        }).catch(err => {
            console.error('获取投票结果失败:', err);
        });
    }

    componentDidMount() {
        this.fetchResult();
        this.timer = setInterval(this.fetchResult, 5000);
    }

    componentWillUnmount() {
        if (this.timer) {
            clearInterval(this.timer);
        }
    }

    /** 计算当前投票区域的标题和提示信息 */
    getCurrentVoteInfo() {
        const { students, determineNum, limit, isFinished, all, isRevote } = this.state;

        if (isFinished) {
            return { title: '最终入选名单', subtitle: '' };
        }
        if (all.length === 0) {
            return { title: '当前投票', subtitle: '' };
        }
        if (isRevote !== 0 && students.length > 0) {
            return {
                title: '当前重投',
                subtitle: `当前第 ${determineNum + 1} 名至第 ${determineNum + students.length} 名平票，还需选出 ${limit} 名`
            };
        }
        return { title: '当前投票', subtitle: '' };
    }

    render() {
        const { all, revoteResult, preRevoteResult, students, pre, hasData, isFinished } = this.state;

        if (!hasData) {
            return <div className={styles.result}><h2 className={styles.tip}>正在加载投票数据...</h2></div>;
        }

        const currentInfo = this.getCurrentVoteInfo();

        // 正选重投：key 从 2 开始才是真正的重投结果（key=1 与 all 重复）
        const revoteKeys = Object.keys(revoteResult)
            .map(Number)
            .filter(k => k >= 2)
            .sort((a, b) => a - b);

        // 候补重投：所有 key 都是有效的重投结果
        const preRevoteKeys = Object.keys(preRevoteResult)
            .map(Number)
            .sort((a, b) => a - b);

        let roundLabel = 0; // 重投轮次显示编号

        return (
            <div className={styles.result}>
                {/* 首轮投票完整结果（重投开始后才有数据） */}
                {all.length > 0 && (
                    <VoteTable
                        title="首轮投票结果"
                        subtitle={`共 ${all.length} 名候选人参与投票`}
                        data={all}
                    />
                )}

                {/* 已完成的正选重投轮次（从 key=2 开始，标签从"第1轮"开始） */}
                {revoteKeys.map((key) => {
                    roundLabel++;
                    const round = revoteResult[key];
                    return (
                        <VoteTable
                            key={`revote-${key}`}
                            title={`第 ${roundLabel} 轮重投结果【入围人选】`}
                            subtitle={`本轮限投 ${round.limit} 票`}
                            data={round.revoteList}
                        />
                    );
                })}

                {/* 已完成的候补重投轮次 */}
                {preRevoteKeys.map((key) => {
                    roundLabel++;
                    const round = preRevoteResult[key];
                    return (
                        <VoteTable
                            key={`preRevote-${key}`}
                            title={`第 ${roundLabel} 轮重投结果【候补人选】`}
                            subtitle={`本轮限投 ${round.limit} 票`}
                            data={round.revoteList}
                        />
                    );
                })}

                {/* 当前进行中的投票 / 最终结果 */}
                <VoteTable
                    title={currentInfo.title}
                    subtitle={currentInfo.subtitle}
                    data={isFinished ? [...students, ...pre] : students}
                />
            </div>
        );
    }
}

export default Result
