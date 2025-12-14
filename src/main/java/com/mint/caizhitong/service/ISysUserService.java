package com.mint.caizhitong.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mint.caizhitong.domain.UserQueryDto;
import com.mint.caizhitong.model.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;


/**
 * @author Mint
 */
public interface ISysUserService extends IService<SysUser> {


    Page<SysUser> pageQuery(UserQueryDto userQueryDto);
}
