import React, { useState } from "react"
import { Button, Modal } from 'antd'
import styles from './button.module.css'
import axios from "axios"
import { useNavigate } from "react-router-dom";
import { API_BASE_URL } from "../../../config";

// 提交按钮
const Submit = ((props) => {
    const navigate = useNavigate();

    const [visible, setVisible] = useState(false);
    const visibleTrigger = () => {
        setVisible(true);
    }
    const visibleCancel = () => {
        setVisible(false);
    }

    const sentResult = () => {
        setVisible(false);

        axios({
            method: 'post',
            url: `${API_BASE_URL}/vote`,
            params: '',
            data: props.list,
        }).then(
            res => {
                navigate("/waiting", { replace: true })
            }
        )
    }

    return (
        <div className={styles.submitContainer}>
            <Button className={styles.submit}
                type="primary"
                shape="round"
                size="large"
                block='true'
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
            >
                <p>当前已投 {props.check} 票,是否确定提交？</p>
            </Modal>
        </div>

    )
})

export default Submit