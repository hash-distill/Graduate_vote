import React, { Component } from 'react';
import styles from './form.module.css'
import axios from 'axios'
import Footer from '../footer';
import Confirm from '../confirm';
import { API_BASE_URL } from '../../../config';
var checkLimit = 0;

// 投票表单（按设备限一票：无需口令/链接，设备标识由 axios 拦截器自动携带）
class Form extends Component {
  state = {
    student_list: [],
    checked_num: 0,
    students: [],                 // 真实数据，由后端下发
    btnVisible: false,
    btnCon: "",
    deviceId: null,
    alreadyVoted: false,          // 本设备本轮是否已投（防重复）
    loadError: "",
    loaded: false
  }

  // 拉取名单
  componentDidMount() {
    axios.get(`${API_BASE_URL}/users`).then(res => {
      if (res.data && res.data.result === true) {
        const data = res.data.data;
        checkLimit = data.limit;
        this.setState({
          students: data.students || [],
          deviceId: data.deviceId,
          alreadyVoted: !!data.alreadyVoted,
          loadError: "",
          loaded: true
        });
        if (data.alreadyVoted) {
          this.setState({ btnVisible: true, btnCon: "本设备已投过，请勿重复提交" });
        }
      } else {
        const msg = (res.data && res.data.msg) || "加载失败";
        this.setState({ loadError: msg, loaded: true, btnVisible: true, btnCon: msg });
      }
    }).catch(() => {
      this.setState({ loadError: "无法连接服务器", loaded: true });
      this.setVisible(true, "无法连接服务器，请确认后端已启动");
    });
  }

  // 通知提示显示设置
  setVisible(data, con) {
    this.setState((state) => {
      return {
        btnVisible: data,
        btnCon: con || state.btnCon
      }
    })
  }

  // 投票数量修改 & 判断
  checked_num = e => {
    const q = e.target
    if (this.state.alreadyVoted) { e.target.checked = false; return; }
    if (e.target.checked == false) {
      let list = this.state.student_list
      let value = e.target.dataset.id
      list.splice(list.indexOf(value), 1)
      this.setState((state) => {
        return {
          student_list: list,
          checked_num: state.checked_num - 1
        }
      })
    } else if (this.state.checked_num >= checkLimit) {
      this.setVisible(true, "选人到达上限")
      e.target.checked = false
    } else {
      this.choose(q)
    }
  }

  // 选中后人数增加
  choose = q => {
    let list = this.state.student_list
    list.push(q.dataset.id)
    this.setState((state) => {
      return {
        student_list: list,
        checked_num: state.checked_num + 1
      }
    })
  }

  render() {
    const { students, loadError, alreadyVoted, loaded } = this.state;
    return (
      <div>
        <div className={styles.form}>
          <div className={styles.need}>
            <span className={styles.needtitle}>一、招募条件</span>
            <p className={styles.foots}>
              1．具有我校学籍的全日制应届本科毕业生，并且不属于公费师范生以及定向、委托培养等招生时明确规定不得报考研究生的情形。</p>
            <p className={styles.foots}>2．申请研究生支教团的不可兼报校内外研究生推免和其他专项计划类推免。</p>
            <p className={styles.foots}>3．具有高尚的爱国主义情操和集体主义精神，理想信念坚定，社会责任感强，遵纪守法，积极参加志愿服务。</p>
            <p className={styles.foots}>4．完成截止第三学年（五年制为第四学年）应修的全部必修课程，不及格（重修及格的视为及格）或未修的不具备排名资格。</p>
            <p className={styles.foots}>5．勤奋学习，刻苦钻研，成绩优秀。研究生支教团推免生综合测评成绩底线应与所在学科普通类推免生综合测评成绩底线保持一致；学校认定的基地班专业学生不受排名限制。</p>
            <p className={styles.foots}>大学英语四级成绩在425分（含）以上，其它语种学生大学外语四级成绩需合格（仅限东北师范大学考点，其它考点无效）；或东北师范大学英语水平测试成绩达到70分(含)以上。</p>
            <p className={styles.foots}>体育类、艺术类学生申报研究生支教团的，其专业排名、外语水平等要求与申请所在学科普通类推免保持一致。</p>
            <p className={styles.foots}>6．诚实守信，学风端正，品行优良，无任何考试作弊和剽窃他人学术成果记录，无任何违法违纪受处分记录。凡有不良记录者，一票否决，不得入选。</p>
            <p className={styles.foots}>7．身心健康，能胜任西部地区基础教育志愿服务工作。</p>
            <p className={styles.foots}>8．中共党员（含中共预备党员）、获得中小学教师资格证者，同等条件下应优先考虑。</p>
            <p className={styles.foots}>9．其他相关事宜按照《东北师范大学推荐优秀应届本科毕业生免试攻读硕士学位研究生工作实施办法（修订）》执行。</p>
            <span className={styles.needtitle}>二、投票说明</span>
            <p className={styles.foots}>
              1. 请按管理员公布的应选名额投票，在投票系统页面“是否同意”栏中点击。</p>
            <p className={styles.foots}>
              2. 每台设备每轮只能投一次；投票完毕后，如出现平票，系统会提示对平票人选再次投票。</p>
            {alreadyVoted && <span className={styles.warn}>⚠ 本设备已投过本轮，请勿重复投票。</span>}
            {loadError && <span className={styles.warn}>⚠ {loadError}</span>}
          </div>

          {loaded && !loadError && (
            <table>
              <tbody>
                <tr className={styles.tablehead}>
                  <th style={{ borderTopLeftRadius: 15 }}>序号</th>
                  <th>学院</th>
                  <th>专业</th>
                  <th>姓名</th>
                  <th>性别</th>
                  <th>政治面貌</th>
                  <th>学院排序</th>
                  <th>面试序号</th>
                  <th style={{ borderTopRightRadius: 15 }}>是否同意</th>
                </tr>

                {students.map((item, index) => {
                  let sex = '男'
                  if (item.voteGender == 0) {
                    sex = '女'
                  }
                  return (
                    <tr className={styles.student} key={item.voteId}>
                      <td><span className={styles.message}>{index + 1} </span></td>
                      <td><span className={styles.message}> {item.voteInsti}</span></td>
                      <td><span className={styles.message}>{item.voteMajor} </span></td>
                      <td><span className={styles.message}>{item.voteName} </span></td>
                      <td><span className={styles.message}>{sex} </span></td>
                      <td><span className={styles.message}>{item.votePoli} </span></td>
                      <td><span className={styles.message}>{item.voteInstiSort} </span></td>
                      <td><span className={styles.message}>({item.voteInterSort}) </span></td>
                      <td>
                        <input type='checkbox' name='student' key={item.voteId}
                          onClick={this.checked_num} data-id={item.voteId}
                          disabled={alreadyVoted || !loaded} />
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          )}
        </div>

        {loaded && !loadError && (
          <Footer limit={checkLimit} checked={this.state.checked_num} list={this.state.student_list}
            disabled={alreadyVoted || this.state.student_list.length === 0}></Footer>
        )}
        <Confirm visible={this.state.btnVisible} fn={this.setVisible.bind(this)} title="系统提示" con={this.state.btnCon}></Confirm>
      </div>
    );
  }
}

export default Form