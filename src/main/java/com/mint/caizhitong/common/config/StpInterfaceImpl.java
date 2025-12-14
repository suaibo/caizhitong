package com.mint.caizhitong.common.config;

import cn.dev33.satoken.stp.StpInterface;

import com.mint.caizhitong.mapper.SysUserMapper;
import com.mint.caizhitong.model.SysUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 自定义权限验证接口扩展
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final SysUserMapper sysUserMapper;

    /**
     * 返回一个账号所拥有的权限码集合 (暂时用不到，留空)
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return Collections.emptyList();
    }

    /**
     * 返回一个账号所拥有的角色标识集合 (权限的核心)
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // 1. loginId 即为 User ID (Long 类型)
        Long userId = Long.valueOf(loginId.toString());

        // 2. 查数据库
        SysUser user = sysUserMapper.selectById(userId);

        // 3. 返回角色 (假设 sys_user 表 role 字段存的是 "admin" 或 "student")
        if (user != null && user.getRole() != null) {
            return List.of(user.getRole());
        }
        return Collections.emptyList();
    }
}
