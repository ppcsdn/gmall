package com.atgg.gmall.user.mapper;

import com.atgg.gmall.bean.UmsMember;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

public interface UmsMemberMapper extends Mapper<UmsMember> {

    UmsMember selectUserById(@Param("memberId") String memberId);
}
