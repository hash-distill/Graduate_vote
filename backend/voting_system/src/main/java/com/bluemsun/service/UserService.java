package com.bluemsun.service;

import com.bluemsun.entity.User;
import org.springframework.stereotype.Service;

import jakarta.servlet.ServletContext;
import java.util.List;
import java.util.Map;

public interface UserService {
    List<User> getAllUsers();

    boolean insertOne(User user);

    boolean updateByIds(List<Integer> students);

    Map<String, Object> getRevote(List<User> list, int students, List<User> last);

    Map<String, Object> getPreRevote(List<User> list, int students, List<User> pre);

    boolean insertAll(List<User> users);

    boolean setPollZero();

    boolean updatePollToFirst(List<User> list);


    Map<String, Object> vote(ServletContext application, Map<String, Object> map, Integer prenum);
}
