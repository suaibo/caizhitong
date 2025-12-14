package com.mint.caizhitong.common.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Component
public class RedisUtil {

    private final StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();

    public <R, ID> R execute(String keyPrefix, ID id, Class<R> type,
                             Function<ID,R> getById,
                             Long expireTime, TimeUnit timeUnit
                             ) {
        String key = keyPrefix + id;
        String value = stringRedisTemplate.opsForValue().get(key);
        //判断是否是空值
        if (StrUtil.isNotBlank(value)) {
            return JSONUtil.toBean(value, type);
        }
        //如果是空字符串则返回，实现缓存穿透策略
        if(value != null){
            return null;
        }
        //查询数据库
        R r = getById.apply(id);
        //将数据缓存到redis，不存在的数据缓存“”
        if (r == null) {
            stringRedisTemplate.opsForValue().set(key, "", timeUnit.toHours(expireTime), timeUnit);
            return null;
        }
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(r), expireTime, timeUnit);
        //返回数据
        return r;
    }
}
