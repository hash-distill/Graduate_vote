import Header from "../../compoments/vote/header"
import axios from "axios";
import styles from './set.module.css'
import { useNavigate } from "react-router-dom";

const Set = (() => {

  var state = {
    limit: 0,
    students: 0,
    teachers: 0
  }

  const navigate = useNavigate()

  const set_message = () => {
    // console.log(state)
    axios({
      method: 'post',//请求方式
      url: 'http://localhost:8081/admin/setMsg',//请求地址
      params: '',//和url一起发送的数据（如get请求）
      data: JSON.stringify({ limit: state.limit, teachers: state.teachers, students: state.students }),//必要参数，
      // 自定义请求头
      headers: { 'Content-Type': 'application/json' },
    }).then(
      res => {
        // console.log(res)
        navigate("/show", { replace: true })
        // this.setState({
        //   data:res.data.data
        // })
      }
    )
  }

  const handleForm = e => {
    const target = e.target
    const value = target.type === 'checkbox'
      ? target.checked
      : target.value

    //获取name
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
