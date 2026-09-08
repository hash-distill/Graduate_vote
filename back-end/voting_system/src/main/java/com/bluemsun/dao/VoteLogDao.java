package com.bluemsun.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * Step4 记录型落库 DAO（vote_session / vote_log）。
 *
 * v2：按“投票设备限一票” —— 设备号由前端生成并携带（X-Device-Id），
 * vote_log 唯一键 (session_id, round_no, voter_device, candidate_id) 防同设备同轮重复票。
 * 设计约束：仅“记录”，不改变内存状态机的计票判定；写库失败由调用方捕获降级。
 */
@Mapper
public interface VoteLogDao {

    /** 新建场次记录。 */
    @Insert("insert into vote_session(session_id, status, round_no, limit_votes, students_quota, teachers_all) "
            + "values(#{sessionId}, 'INIT', 1, #{limitVotes}, #{studentsQuota}, #{teachersAll})")
    int insertSession(@Param("sessionId") String sessionId,
                      @Param("limitVotes") int limitVotes,
                      @Param("studentsQuota") int studentsQuota,
                      @Param("teachersAll") int teachersAll);

    /** 记录一条投票流水（唯一键冲突 = 同设备同轮对同一候选人重复票，返回 0）。 */
    @Insert("insert ignore into vote_log(session_id, round_no, voter_device, candidate_id) "
            + "values(#{sessionId}, #{roundNo}, #{deviceId}, #{candidateId})")
    int insertVote(@Param("sessionId") String sessionId,
                   @Param("roundNo") int roundNo,
                   @Param("deviceId") String deviceId,
                   @Param("candidateId") int candidateId);

    /** 查询某场次某轮某设备是否已有流水。 */
    @Select("select count(*) from vote_log where session_id = #{sessionId} "
            + "and round_no = #{roundNo} and voter_device = #{deviceId}")
    int countVoted(@Param("sessionId") String sessionId,
                   @Param("roundNo") int roundNo,
                   @Param("deviceId") String deviceId);

    /** 场次状态更新（进入重投 / 结束）。 */
    @Update("update vote_session set status = #{status}, round_no = #{roundNo} "
            + "where session_id = #{sessionId}")
    int updateSession(@Param("sessionId") String sessionId,
                      @Param("status") String status,
                      @Param("roundNo") int roundNo);
}
