package com.atgg.gmall.manage.mapper;

import com.atgg.gmall.bean.PmsBaseAttrInfo;
import com.atgg.gmall.bean.PmsBaseAttrValue;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @author Pstart
 * @create 2019-10-11 22:33
 */
public interface PmsBaseAttrValueMapper extends Mapper<PmsBaseAttrValue> {
    List<PmsBaseAttrInfo> selectAttrValueListByValueIds(@Param("join") String values);
}
