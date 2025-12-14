package com.mint.caizhitong.common.utils;


import com.mint.caizhitong.model.SysUser;

public class UserHolder {
    private static final ThreadLocal<SysUser> SYS_USER_THREAD_LOCAL = new ThreadLocal<SysUser>();

    public static void saveUser(SysUser user) {
        SYS_USER_THREAD_LOCAL.set(user);
    }

    public static SysUser getUser() {
        return SYS_USER_THREAD_LOCAL.get();
    }

    public static void removeUser() {
        SYS_USER_THREAD_LOCAL.remove();
    }
}
