import { Modal } from 'antd';
import { useEffect, useState } from 'react';

// 弹出提示框
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
            title={modalTitle}
            centered
            open={visible}
            onOk={triggerConfirm}
            onCancel={triggerCancel}
            okText="确定"
            cancelText="取消"
        >
            <p>{modalContent}</p>
        </Modal>
    );
}

export default Confirm