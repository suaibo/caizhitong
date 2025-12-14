package com.mint.caizhitong.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mint.caizhitong.common.constant.RoleConst;
import com.mint.caizhitong.common.resp.Result;
import com.mint.caizhitong.common.utils.PasswordUtils;
import com.mint.caizhitong.domain.UserDto;
import com.mint.caizhitong.domain.UserQueryDto;
import com.mint.caizhitong.domain.UserVO;
import com.mint.caizhitong.model.SysUser;
import com.mint.caizhitong.service.ISysUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

/**
 * @author mint
 * @since 2025-11-25
 */
@SaCheckRole(RoleConst.ADMIN) //权限控制
@RestController
@RequestMapping("/api/users")
public class UsersController {

    private ISysUserService userService;

    public UsersController(ISysUserService userService) {
        this.userService = userService;
    }

    //分页查询
    @GetMapping
    public Result user(@RequestBody @Validated UserQueryDto userQueryDto) {
        Page<SysUser> sysUserPage = userService.pageQuery(userQueryDto);
        List<UserVO> list = sysUserPage.getRecords().stream()
                .map(sysUser -> {
                    UserVO userVO = new UserVO();
                    BeanUtils.copyProperties(sysUser, userVO);
                    return userVO;
                }).toList();
        long total = sysUserPage.getTotal();
        HashMap<String, Object> stringObjectHashMap = new HashMap<>();
        stringObjectHashMap.put("list", list);
        stringObjectHashMap.put("total", total);
        return Result.success(stringObjectHashMap);
    }

    @GetMapping("/{id}")
    public Result getUser(@PathVariable int id) {
        return Result.success(userService.getById(id));
    }
    //创建用户
    @PostMapping
    public Result saveUser(@RequestBody @Validated UserDto userDto) {
        SysUser one = userService.getOne(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getName, userDto.getName()));
        if (one == null) {
            SysUser sysUser = new SysUser();
            //初始密码为手机号
            sysUser.setPassword(PasswordUtils.encodePassword(userDto.getPhone()));
            //使用beanutils偷一下懒，有安全隐患的
            BeanUtils.copyProperties(userDto, sysUser);
            userService.save(sysUser);
            return Result.success();
        }
        else{
            return Result.error("用户已存在啦宝子");
        }
    }

    @PutMapping("/{id}")
    public Result updateUser(@PathVariable Long id, @RequestBody @Validated UserDto userDto) {
        SysUser sysUser = new SysUser();
        sysUser.setId(id);
        sysUser.setName(userDto.getName());
        sysUser.setRole(userDto.getRole());
        sysUser.setPhone(userDto.getPhone());
        sysUser.setDept(userDto.getDept());
        boolean b = userService.updateById(sysUser);
        if(b){
            return Result.success();
        }
        else{
            return Result.error("更新失败了T-T");
        }
    }

    @DeleteMapping("/{id}")
    public Result deleteUser(@PathVariable int id) {
        boolean b = userService.removeById(id);
        if(b){
            return Result.success();
        }
        else{
            if (userService.getById(id) == null) {
                return Result.error("id不存在");
            }
            else{
                return Result.error("操作失败T-T");
            }
        }
    }
}
