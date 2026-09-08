import { Modal } from 'antd';
import { useEffect, useState } from 'react';

// 弹出提示框（警示风格：图标 + 红字，与正文/普通弹窗区分）
function Confirm(props) {
    const [visible, setVisible] = useState(false);
    const triggerConfirm = () => {
        props.fn(false);
    }
    const triggerCancel = () => {
        props.fn(false);
    }
    const modalTitle = props.title;
    const modalContent = props.con;
    useEffect(() => {
        setVisible(props.visible);
    }, [props.visible]);

    return (
        <Modal
            title={<span style={{ color: '#c00020' }}>⚠ {modalTitle}</span>}
            centered
            open={visible}
            onOk={triggerConfirm}
            onCancel={triggerCancel}
            okText="确定"
            cancelText="取消"
            okButtonProps={{ style: { background: '#c00020', borderColor: '#c00020' } }}
        >
            <p style={{
                margin: 0,
                padding: '14px 18px',
                border: '2px solid #e0002a',
                borderRadius: '10px',
                background: '#fff2f2',
                color: '#c00020',
                fontWeight: 'bold',
                fontSize: '16px',
                textAlign: 'center',
                lineHeight: 1.7,
                fontFamily: '"Microsoft YaHei", 微软雅黑, sans-serif'
            }}>
                {modalContent}
            </p>
        </Modal>
    );
}

export default Confirm