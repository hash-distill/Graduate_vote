import Header from "../../compoments/vote/header"
import axios from "axios";
import { useEffect, useState } from "react";
import styles from './set.module.css'
import { useNavigate } from "react-router-dom";
import { API_BASE_URL } from "../../config";
import { getAdminToken, clearAdminToken } from "../../auth";
import { message } from 'antd';

const Set = (() => {

  const [limit, setLimit] = useState("45");
  const [students, setStudents] = useState("45");
  const [teachers, setTeachers] = useState("");
  const [preNum, setPreNum] = useState("4");          // 候补人数，默认 4
  const [msg, setMsg] = useState("");
  const [done, setDone] = useState(false);          // 是否已初始化本轮
  const [submitting, setSubmitting] = useState(false);

  const navigate = useNavigate()

  // 管理员鉴权守卫：未登录跳转登录页
  useEffect(() => {
    if (!getAdminToken()) {
      navigate("/login", { replace: true });
    }
  }, [navigate]);

  const set_message = () => {
    setMsg("");
    const l = Number(limit);
    const s = Number(students);
    const t = Number(teachers);
    const p = Number(preNum);
    if (!(l > 0 && Number.isInteger(l)) || !(s > 0 && Number.isInteger(s)) || !(t > 0 && Number.isInteger(t)) || !(p > 0 && Number.isInteger(p))) {
      setMsg("请填写有效的正整数参数");
      return;
    }
    if (l !== s) {
      setMsg("每人限投票数必须与正选人数一致");
      return;
    }
    if (p > 10) {
      setMsg("候补人数过大（≤10），请检查输入");
      return;
    }
    setSubmitting(true);
    axios({
      method: 'post',
      url: `${API_BASE_URL}/admin/setMsg`,
      params: '',
      data: JSON.stringify({ limit: l, teachers: t, students: s, preNum: p }),
      headers: { 'Content-Type': 'application/json' },
    }).then(
      res => {
        if (res.data.result === true) {
          setDone(true);
          message.success(`初始化完成：正选 ${s} 人 + 候补 ${p} 人，评委请在各自设备打开投票页投票`);
        } else {
          setMsg(res.data.msg || "设置失败");
        }
      }
    ).catch(() => setMsg("设置失败：请求未完成，请确认后端已启动")).finally(() => setSubmitting(false));
  }

  const goShow = () => navigate("/show", { replace: true });

  const logout = () => {
    axios({ method: 'post', url: `${API_BASE_URL}/admin/logout` }).finally(() => {
      clearAdminToken();
      navigate("/login", { replace: true });
    });
  }

  return (
    <div>
      <Header></Header>
      <div className={styles.main}>
        <h2 className={styles.title}>投票参数设置</h2>

        {!done && (
          <div className={styles.formGroup}>
            <div className={styles.field}>
              <label className={styles.label}>正选人数（如本次 45）</label>
              <input type="number" name="students" className={styles.input} placeholder="请输入，如 45"
                value={students} onChange={e => setStudents(e.target.value)} min="1" />
              <p className={styles.hint}>本次投票选出的正选名额数量（正式入选）</p>
            </div>

            <div className={styles.field}>
              <label className={styles.label}>候补人数（默认 4）</label>
              <input type="number" name="preNum" className={styles.input} placeholder="请输入，如 4"
                value={preNum} onChange={e => setPreNum(e.target.value)} min="1" max="10" />
              <p className={styles.hint}>本轮候补名额数量（平票重投按此人数确定）</p>
            </div>

            <div className={styles.field}>
              <label className={styles.label}>参与投票设备/评委数</label>
              <input type="number" name="teachers" className={styles.input} placeholder="请输入"
                value={teachers} onChange={e => setTeachers(e.target.value)} min="1" />
              <p className={styles.hint}>现场参与投票的评委（每台设备一票）总数，全部投完后系统自动处理本轮结果</p>
            </div>

            <div className={styles.field}>
              <label className={styles.label}>每人限投票数</label>
              <input type="number" name="limit" className={styles.input} placeholder="请输入"
                value={limit} onChange={e => setLimit(e.target.value)} min="1" />
              <p className={styles.hint}>每位评委每轮最多可投的票数，须与正选人数一致</p>
            </div>
          </div>
        )}

        {done && (
          <div className={styles.formGroup}>
            <div className={styles.ticketBox}>
              <p className={styles.hint}>✅ 本轮已初始化。评委无需口令/链接/编号，直接在各自设备的投票页（/vote）投票；
                每台设备每轮只能投一次。本场：正选 {students || "—"} 人 + 候补 {preNum || "—"} 人。</p>
              <div className={styles.btnRow}>
                <button className={styles.submitBtn} onClick={() => setDone(false)}>重新设置</button>
                <button className={styles.submitBtn} onClick={goShow}>去监控页</button>
              </div>
            </div>
          </div>
        )}

        {msg && <p className={styles.error}>{msg}</p>}

        {!done && (
          <button className={styles.submitBtn} disabled={submitting} onClick={set_message}>确认提交</button>
        )}
        <button className={styles.logoutBtn} onClick={logout}>退出登录</button>
        <p className={styles.note}>⚠ 提交后将初始化所有投票数据，请确认参数无误后再提交</p>
      </div>
    </div>
  );
})

export default Set