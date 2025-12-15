package com.mint.caizhitong.controller;


import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mint.caizhitong.common.resp.Result;
import com.mint.caizhitong.common.utils.PasswordUtils;
import com.mint.caizhitong.domain.RegisterDto;
import com.mint.caizhitong.domain.UserDto;
import com.mint.caizhitong.domain.UserVO;
import com.mint.caizhitong.model.SysUser;
import com.mint.caizhitong.service.ISysUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private ISysUserService userService;

    public AuthController(ISysUserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public Result register(@RequestBody RegisterDto user) {
        SysUser sysUser = new SysUser();
        //加密密码
        if ("teacher".equals(user.getRole())) {
            user.setRole("lab");
        }
        user.setPassword(PasswordUtils.encodePassword(user.getPassword()));
        BeanUtils.copyProperties(user, sysUser);
        userService.save(sysUser);

        return Result.success();
    }

    @PostMapping("/login")
    public Result login(String username, String password) {
        SysUser one = userService.getOne(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getName, username));
        if (one != null && PasswordUtils.matchesPassword(password, one.getPassword())) {
            StpUtil.login(one.getId());
            UserVO userVO = new UserVO();
            Map<String, Object> stringObjectMap = new HashMap<>();
            stringObjectMap.put("token", StpUtil.getTokenValue());
            BeanUtils.copyProperties(one, userVO);
            stringObjectMap.put("user", userVO);
            return Result.success(stringObjectMap);
        } else if (one != null) {
            return Result.error("用户名不存在呢亲亲");
        } else {
            return Result.error("密码错误呢亲亲");
        }
    }

    @GetMapping("/me")
    public Result me() {
        Long loginId = StpUtil.getLoginIdAsLong();
        SysUser byId = userService.getById(loginId);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(byId, userVO);
        return Result.success(userVO);
    }

}
