-- ============================================================
-- Step4 落库：场次 / 投票流水（记录型审计 + DB 幂等兜底）
-- v2：改为“按投票设备限一票” —— 无评委编号/口令/预分发链接；
--     每台浏览器首次投票自动取得 deviceId（localStorage 保存），
--     vote_log 唯一键 (session_id, round_no, voter_device, candidate_id) 防同设备重复票。
-- 说明：能防“同设备重复提交/双击/多标签”；清缓存或无痕开新窗口可获得新设备号，
--       现场需管理员监督兜底（无身份体系的固有边界）。
-- 执行：mysql -u root -p vote < schema_vote_step4.sql
-- ============================================================

CREATE TABLE IF NOT EXISTS vote_session (
    session_id     VARCHAR(64)  NOT NULL COMMENT '场次ID（setMsg 时生成）',
    status         VARCHAR(20)  NOT NULL DEFAULT 'INIT' COMMENT 'INIT/ONGOING/REVOTE/FINISHED',
    round_no       INT          NOT NULL DEFAULT 1 COMMENT '当前投票轮次',
    limit_votes    INT          NULL COMMENT '每人限投数（冗余，便于审计）',
    students_quota INT          NULL COMMENT '正选名额（冗余）',
    teachers_all   INT          NULL COMMENT '参与投票设备/评委总数',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投票场次';

CREATE TABLE IF NOT EXISTS vote_log (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    session_id   VARCHAR(64)  NOT NULL COMMENT '场次ID',
    round_no     INT          NOT NULL COMMENT '投票轮次',
    voter_device VARCHAR(64)  NOT NULL COMMENT '设备标识（防同设备重复票）',
    candidate_id INT          NOT NULL COMMENT '候选人 vote_id',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_vote (session_id, round_no, voter_device, candidate_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投票流水（唯一键=同设备同轮防重）';
