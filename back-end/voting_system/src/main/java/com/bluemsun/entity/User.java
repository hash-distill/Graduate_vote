package com.bluemsun.entity;

import lombok.Data;

@Data
public class User {
    private Integer voteId;
    private String voteName;
    private String voteMajor;
    private Integer voteGender;
    private String votePoli;
    private String voteInsti;
    private Integer votePoll=0;
    private String voteInstiSort;
    private String voteInterSort;

    public static User getUser(User u){
        return new User(u.getVoteId(), u.getVoteName(),u.getVoteMajor(), u.getVoteGender(),u.getVotePoli(),u.getVoteInsti(),u.getVotePoll(), u.getVoteInstiSort(),u.getVoteInterSort());
    }
    public User(Integer voteId, String voteName, String voteMajor, Integer voteGender, String votePoli, String voteInsti, Integer votePoll, String voteInstiSort, String voteInterSort) {
        this.voteId = voteId;
        this.voteName = voteName;
        this.voteMajor = voteMajor;
        this.voteGender = voteGender;
        this.votePoli = votePoli;
        this.voteInsti = voteInsti;
        this.votePoll = votePoll;
        this.voteInstiSort = voteInstiSort;
        this.voteInterSort = voteInterSort;
    }

    public User(Integer voteId, String voteName, Integer voteGender, String votePoli, String voteInsti, Integer votePoll, String voteInstiSort, String voteInterSort) {
        this.voteId = voteId;
        this.voteName = voteName;
        this.voteGender = voteGender;
        this.votePoli = votePoli;
        this.voteInsti = voteInsti;
        this.votePoll = votePoll;
        this.voteInstiSort = voteInstiSort;
        this.voteInterSort = voteInterSort;
    }

    public User(Integer voteId, String voteName, Integer voteGender, String votePoli, String voteInsti, Integer votePoll) {
        this.voteId = voteId;
        this.voteName = voteName;
        this.voteGender = voteGender;
        this.votePoli = votePoli;
        this.voteInsti = voteInsti;
        this.votePoll = votePoll;
    }

    public User(String voteName, Integer voteGender, String votePoli, String voteInsti, Integer votePoll) {
        this.voteName = voteName;
        this.voteGender = voteGender;
        this.votePoli = votePoli;
        this.voteInsti = voteInsti;
        this.votePoll = votePoll;
    }


    public User() {
    }
}
