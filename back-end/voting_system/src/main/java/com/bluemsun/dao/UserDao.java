package com.bluemsun.dao;

import com.bluemsun.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserDao {

    @Select("select vote_id as voteId, vote_name as voteName,vote_gender as voteGender," +
            "vote_poli as votePoli,vote_insti as voteInsti,vote_poll as votePoll, " +
            "vote_insti_sort as voteInstiSort, vote_inter_sort as voteInterSort, vote_major as voteMajor " +
            "from vote01")
    List<User> selectAll();

    int insertOne(User user);

    int updateById(int id);

    @Update("update vote01 set vote_poll = 0 where vote_id = #{id}")
    int setPollZero(int id);

    @Update("update vote01 set vote_poll = 0")
    int setAllPollZero();

    @Select("select vote_id as voteId, vote_name as voteName,vote_gender as voteGender," +
            "vote_poli as votePoli,vote_insti as voteInsti,vote_poll as votePoll, " +
            "vote_insti_sort as voteInstiSort, vote_inter_sort as voteInterSort, vote_major as voteMajor " +
            "from vote01 " +
            "where vote_id = #{id}")
    User updatePollToFirst(int id);
}
