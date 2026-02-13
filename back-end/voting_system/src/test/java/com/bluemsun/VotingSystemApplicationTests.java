package com.bluemsun;

import com.bluemsun.dao.UserDao;
import com.bluemsun.entity.User;
import com.bluemsun.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class VotingSystemApplicationTests {

    @Autowired
    UserService userService;
    @Autowired
    UserDao userDao;
    @Test
    void testGetAll(){
        List<User> u = userService.getAllUsers();
        System.out.println(u);

    }
    @Test
    void testInsertOne(){
        User user = new User(2, "lisi", 0, "test", "test", 0);
        boolean success = userService.insertOne(user);
        System.out.println(success);
    }

    @Test
    void testUpdate(){
        userDao.setPollZero(1);
    }

}
