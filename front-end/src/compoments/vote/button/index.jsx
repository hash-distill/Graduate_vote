import React, { useState } from "react"
import { Button, Modal, message } from 'antd'
import styles from './button.module.css'
import axios from "axios"
import { useNavigate } from "react-router-dom";
import { API_BASE_URL } from "../../../config";

// 提交按钮（设备标识由 axios 拦截器自动附带 X-Device-Id）
const Submit = ((props) => {
    const navigate = useNavigate();

    const [visible, setVisible] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const visibleTrigger = () => {
        setVisible(true);
    }
    const visibleCancel = () => {
        setVisible(false);
    }

    const sentResult = () => {
        setVisible(false);
        if (submitting) return;   // 防重复提交
        if (props.disabled) return;
        if (!props.list || props.list.length === 0) {
            message.warning("请至少选择一位候选人后再提交");
            return;
        }
        setSubmitting(true);
        axios({
            method: 'post',
            url: `${API_BASE_URL}/vote`,
            params: '',
            data: props.list,
        }).then(
            res => {
                if (res.data && res.data.result === true) {
                    navigate("/waiting", { replace: true })
                } else {
                    message.error((res.data && res.data.msg) || "投票失败，请重试");
                }
            }
        ).catch(() => message.error("投票请求失败，请确认后端已启动")).finally(() => setSubmitting(false));
    }

    return (
        <div className={styles.submitContainer}>
            <Button className={styles.submit}
                type="primary"
                shape="round"
                size="large"
                block
                disabled={submitting || props.disabled}
                loading={submitting}
                onClick={visibleTrigger}
            >提 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 交</Button>
            <Modal
                title="系统提示"
                centered
                open={visible}
                onOk={sentResult}
                onCancel={visibleCancel}
                okText="确定"
                cancelText="取消"
                confirmLoading={submitting}
            >
                <p>当前已投 {props.check} 票,是否确定提交？</p>
            </Modal>
        </div>

    )
})

export default Submit