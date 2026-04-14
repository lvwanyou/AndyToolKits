package com.lwy.andytoolkits.unitTestDemo;

// UserService 业务类，依赖 UserRepository 来获取 User
public class UserService {
    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findUserById(String id) {
        return userRepository.findById(id);
    }
}


// User 数据模型类，包含了一个用户的 id 和 name
class User {
    private String id;
    private String name;

    public User(String id) {
        this.id = id;
    }

    public User(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}

// UserRepository 数据访问类，提供了通过 id 获取 User 的方法
interface UserRepository {
    User findById(String id);
}