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
        <h3>每人限投：<input type="text" name="limit" onChange={handleForm} />
          学生人数: <input type="text" name="students" onChange={handleForm} />
          教师人数: <input type="text" name="teachers" onChange={handleForm} />
          <button onClick={set_message}>提交</button></h3>
      </div>

    </div>
  );
})

export default Set
