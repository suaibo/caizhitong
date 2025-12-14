package com.mint.caizhitong.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mint.caizhitong.domain.UserQueryDto;
import com.mint.caizhitong.domain.UserVO;
import com.mint.caizhitong.model.SysUser;
import com.mint.caizhitong.mapper.SysUserMapper;
import com.mint.caizhitong.service.ISysUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * <p>
 * 鐢ㄦ埛琛 服务实现类
 * </p>
 *
 * @author mint
 * @since 2025-11-25
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;
    //分页查询(管理员操作)
    @Override
    public Page<SysUser> pageQuery(UserQueryDto userQueryDto) {
        Page<SysUser> page = new Page<>(userQueryDto.getPageNum(),userQueryDto.getPageSize());
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if(StringUtils.hasText(userQueryDto.getName())){
            wrapper.like(SysUser::getName,userQueryDto.getName());
        }
        if(StringUtils.hasText(userQueryDto.getDept())){
            wrapper.like(SysUser::getDept,userQueryDto.getDept());
        }
        if(StringUtils.hasText(userQueryDto.getRole())){
            wrapper.like(SysUser::getRole,userQueryDto.getRole());
        }
        wrapper.orderByAsc(SysUser::getId);
        return sysUserMapper.selectPage(page,wrapper);
    }


}
