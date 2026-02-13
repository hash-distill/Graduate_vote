import Header from "../../compoments/vote/header"
import axios from "axios";
import styles from './set.module.css'
import { useNavigate } from "react-router-dom";
import { API_BASE_URL } from "../../config";

const Set = (() => {

  var state = {
    limit: 0,
    students: 0,
    teachers: 0
  }

  const navigate = useNavigate()

  const set_message = () => {
    axios({
      method: 'post',
      url: `${API_BASE_URL}/admin/setMsg`,
      params: '',
      data: JSON.stringify({ limit: state.limit, teachers: state.teachers, students: state.students }),
      headers: { 'Content-Type': 'application/json' },
    }).then(
      res => {
        navigate("/show", { replace: true })
      }
    )
  }

  const handleForm = e => {
    const target = e.target
    const value = target.type === 'checkbox'
      ? target.checked
      : target.value

    const name = target.name
    state[name] = value
  }

  return (
    <div>
      <Header></Header>
      <div className={styles.main}>
        <h2 className={styles.title}>投票参数设置</h2>

        <div className={styles.formGroup}>
          <div className={styles.field}>
            <label className={styles.label}>正选人数</label>
            <input
              type="number"
              name="students"
              className={styles.input}
              placeholder="请输入"
              onChange={handleForm}
              min="1"
            />
            <p className={styles.hint}>本次投票需要选出的正选名额数量，最终入选人数 = 正选人数 + 2（候补）</p>
          </div>

          <div className={styles.field}>
            <label className={styles.label}>评委人数</label>
            <input
              type="number"
              name="teachers"
              className={styles.input}
              placeholder="请输入"
              onChange={handleForm}
              min="1"
            />
            <p className={styles.hint}>参与投票的评委（老师）总人数，所有评委投完后系统自动处理本轮结果</p>
          </div>

          <div className={styles.field}>
            <label className={styles.label}>每人限投票数</label>
            <input
              type="number"
              name="limit"
              className={styles.input}
              placeholder="请输入"
              onChange={handleForm}
              min="1"
            />
            <p className={styles.hint}>每位评委每轮投票最多可投的票数（重投时系统会自动调整）</p>
          </div>
        </div>

        <button className={styles.submitBtn} onClick={set_message}>确认提交</button>
        <p className={styles.note}>⚠ 提交后将初始化所有投票数据，请确认参数无误后再提交</p>
      </div>
    </div>
  );
})

export default Set
