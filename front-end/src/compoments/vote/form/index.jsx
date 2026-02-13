import React, { Component } from 'react';
import styles from './form.module.css'
import axios from 'axios'
import Footer from '../footer';
import Confirm from '../confirm';
var checkLimit = 3;

// 投票表单
class Form extends Component {
  state = {
    student_list: [],
    checked_num: 0,
    students: [
      { "id": "1", "sex": "男", "identity": "预备党员", "name": '李益方', "school": "数学与统计学院" },
      { "id": "2", "sex": "女", "identity": "预备党员", "name": '王赫奇', "school": "文学院" },
      { "id": "3", "sex": "女", "identity": "共青团员", "name": '王曼茜', "school": "美术学院" },
      { "id": "4", "sex": "女", "identity": "预备党员", "name": '殷子惠', "school": "地理科学学院" },
      { "id": "5", "sex": "女", "identity": "共青团员", "name": '赫华', "school": "地理科学学院" },
      { "id": "6", "sex": "男", "identity": "预备党员", "name": '张传民', "school": "美术学院" },
      { "id": "7", "sex": "男", "identity": "预备党员", "name": '殷琪超', "school": "美术学院" },
      { "id": "8", "sex": "女", "identity": "预备党员", "name": '盛美玲', "school": "政法学院" },
      { "id": "9", "sex": "女", "identity": "预备党员", "name": '冯荃', "school": "心理学院" },
      { "id": "10", "sex": "女", "identity": "共青团员", "name": '杨雪娇', "school": "马克思主义学部" },
      { "id": "11", "sex": "女", "identity": "预备党员", "name": '赵梓欣', "school": "心理学院" },
      { "id": "12", "sex": "女", "identity": "共青团员", "name": '杨笑雨', "school": "外国语学院" }
    ],
    btnVisible: false,
    btnCon: ""
  }

  // 学生获取
  componentDidMount() {
    axios.get('http://localhost:8081/users').then(res => {
      // console.log(res.data.data.revote, "====")
      checkLimit = res.data.data.limit
      // console.log(checkLimit)
      this.setState((state) => {
        return {
          students: res.data.data.students
        }
      }, () => {
        // console.log(this.state.students) 
      })
    })
  }

  // 通知提示显示设置
  setVisible(data) {
    this.setState((state) => {
      return {
        btnVisible: data
      }
    })
  }

  // 投票数量修改 & 判断
  checked_num = e => {
    const q = e.target
    if (e.target.checked == false) {
      // console.log(1)
      let list = this.state.student_list
      let value = e.target.dataset.id
      list.splice(list.indexOf(value), 1)
      this.setState((state) => {
        return {
          student_list: list,
          checked_num: state.checked_num - 1
        }
      }, () => {
        // console.log(this.state.student_list)
      })
    } else if (this.state.checked_num >= checkLimit) {
      // alert("选人到达上限")
      this.setVisible(true)
      e.target.checked = false
    } else {
      this.choose(q)
    }
  }

  // 选中后人数增加
  choose = q => {
    let list = this.state.student_list
    // console.log(this.state.checked_num)
    list.push(q.dataset.id)
    this.setState((state) => {
      return {
        student_list: list,
        checked_num: state.checked_num + 1
      }
    }, () => {
      // console.log(this.state.student_list)
    })
  }

  render() {
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
              1. 我校研究生支教团推免名额为25人，请选择不超过25人，在投票系统页面“是否同意”栏中点击。</p>
            <p className={styles.foots}>
              2. 共选拔25个入围人选和2个候补人选，投票完毕后，如出现平票的情况，系统会进行提示，请对平票人选进行再次投票。</p>
          </div>

          <table>
            <tr className={styles.tablehead}>
              <th style={{ WebkitBorderTopLeftRadius: 15 }}>序号</th>
              <th>学院</th>
              <th>专业</th>
              <th>姓名</th>
              <th>性别</th>
              <th>政治面貌</th>
              <th>学院排序</th>
              <th>面试序号</th>
              <th style={{ WebkitBorderTopRightRadius: 15 }}>是否同意</th>
            </tr>

            {this.state.students.map((item, index) => {
              let sex = '男'
              if (item.voteGender == 0) {
                sex = '女'
              }
              return (
                <tr className={styles.student}>
                  <td>
                    <span className={styles.message}>{index + 1} </span>
                  </td>
                  <td>
                    <span className={styles.message}> {item.voteInsti}</span>
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
                    <input type='checkbox' name='student' key={item.voteId} onClick={this.checked_num} data-id={item.voteId} />
                  </td>
                </tr>
              )
            })}
            <div className={styles.blank}></div>
          </table>
        </div>

        <Footer limit={checkLimit} checked={this.state.checked_num} list={this.state.student_list}></Footer>
        <Confirm visible={this.state.btnVisible} fn={this.setVisible.bind(this)} title="系统提示" con="选人到达上限"></Confirm>
      </div>
    );
  }
}

export default Form