package com.miracle.login;

import com.miracle.domain.User;
import com.miracle.vo.LoginVO;

public interface LoginService {

      /**
       * 查询系统超级管理员
       * */
      User queryAdmin(LoginVO loginVO);

}
