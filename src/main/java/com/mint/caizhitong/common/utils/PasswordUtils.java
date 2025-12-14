package com.mint.caizhitong.common.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码加密工具类
 * 使用 BCrypt 强哈希算法加密密码
 */
public class PasswordUtils {

    // 使用 Spring Security 的 BCrypt 加密器
    private static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    /**
     * 加密原始密码
     * @param rawPassword 原始密码
     * @return 加密后的哈希密码
     */
    public static String encodePassword(String rawPassword) {
        return PASSWORD_ENCODER.encode(rawPassword);
    }

    /**
     * 验证密码是否匹配
     * @param rawPassword     原始密码
     * @param encodedPassword 已加密的密码
     * @return 匹配返回 true，否则 false
     */
    public static boolean matchesPassword(String rawPassword, String encodedPassword) {
        return PASSWORD_ENCODER.matches(rawPassword, encodedPassword);
    }

    // 测试示例
    public static void main(String[] args) {
        String rawPassword = "mySecret123";

        // 加密密码
        String encodedPassword = encodePassword(rawPassword);
        System.out.println("原始密码: " + rawPassword);
        System.out.println("加密结果: " + encodedPassword);

        // 验证密码
        boolean isMatch = matchesPassword(rawPassword, encodedPassword);
        System.out.println("密码验证结果: " + isMatch);

        // 错误密码测试
        boolean wrongPassword = matchesPassword("wrongPassword", encodedPassword);
        System.out.println("错误密码测试: " + wrongPassword);
    }
}
