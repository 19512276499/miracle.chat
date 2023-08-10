package com.miracle.login;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.miracle.dao.UserMapper;
import com.miracle.domain.User;
import com.miracle.vo.LoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Wrapper;
import java.util.List;

@Service
public class LoginServiceImpl  implements LoginService {

    @Autowired
     private UserMapper userMapper;

    @Override
    public User queryAdmin(LoginVO loginVO) {
        LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(User::getAccount,loginVO.getUsername());
        wrapper.eq(User::getPassword,loginVO.getPassword());
        return userMapper.selectOne(wrapper);
    }
}
