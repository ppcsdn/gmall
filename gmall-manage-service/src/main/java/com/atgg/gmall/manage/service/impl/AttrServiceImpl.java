package com.atgg.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atgg.gmall.bean.PmsBaseAttrInfo;
import com.atgg.gmall.bean.PmsBaseAttrValue;
import com.atgg.gmall.manage.mapper.PmsBaseAttrInfoMapper;
import com.atgg.gmall.manage.mapper.PmsBaseAttrValueMapper;
import com.atgg.gmall.service.AttrService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.HashSet;
import java.util.List;

/**
 * @author Pstart
 * @create 2019-10-11 22:27
 */
@Service
public class AttrServiceImpl implements AttrService {
    @Autowired
    PmsBaseAttrValueMapper pmsBaseAttrValueMapper;
    @Autowired
    PmsBaseAttrInfoMapper pmsBaseAttrInfoMapper;

    public List<PmsBaseAttrInfo> getAttrInfoList(String catalog3Id) {
        PmsBaseAttrInfo pmsBaseAttrInfo = new PmsBaseAttrInfo();
        pmsBaseAttrInfo.setCatalog3Id(catalog3Id);
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = pmsBaseAttrInfoMapper.select(pmsBaseAttrInfo);

        for (PmsBaseAttrInfo baseAttrInfo :
                pmsBaseAttrInfos) {
            String id = baseAttrInfo.getId();
            PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
            pmsBaseAttrValue.setAttrId(id);

            baseAttrInfo.setAttrValueList
                    (pmsBaseAttrValueMapper.select(pmsBaseAttrValue));
        }

        return pmsBaseAttrInfos;
    }

    //修改和新增,用户的id不为空时为修改,为空时为保存
    public void saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo) {//拿到的新增表单和修改表单的不同的数据
        String attrId = "";
        if (StringUtils.isNotBlank(pmsBaseAttrInfo.getId())) {
            attrId = pmsBaseAttrInfo.getId();
            Example example = new Example(PmsBaseAttrInfo.class);//表示where子句==where
            example.createCriteria().andEqualTo("id", attrId);//匹配规则==条件id=attrId
            pmsBaseAttrInfoMapper.updateByExampleSelective(pmsBaseAttrInfo, example);//对指定数据进行匹配example条件,修改info表内容
            //将PmsBaseAttrValue表中数据删除,具体的修改步骤先删除后保存
            PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
            pmsBaseAttrValue.setAttrId(attrId);
            pmsBaseAttrValueMapper.delete(pmsBaseAttrValue);
        } else {
            pmsBaseAttrInfoMapper.insertSelective(pmsBaseAttrInfo);
            attrId = pmsBaseAttrInfo.getId();
        }
        //抽取出保存和修改更新时的对数据持久层操作
        if (StringUtils.isNotBlank(attrId)) {
            List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
            for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                pmsBaseAttrValue.setAttrId(attrId);//更新时这一步不用做

                pmsBaseAttrValueMapper.insertSelective(pmsBaseAttrValue);
            }
        }
    }

    //通过用户id封装单个用户具体信息
    public PmsBaseAttrInfo getAttrValueList(String attrId) {
        PmsBaseAttrInfo pmsBaseAttrInfo = new PmsBaseAttrInfo();
        pmsBaseAttrInfo.setId(attrId);
        PmsBaseAttrInfo pmsBaseAttrInfo1 = pmsBaseAttrInfoMapper.selectOne(pmsBaseAttrInfo);

        PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
        pmsBaseAttrValue.setAttrId(attrId);
        List<PmsBaseAttrValue> pmsBaseAttrValues = pmsBaseAttrValueMapper.select(pmsBaseAttrValue);

        pmsBaseAttrInfo1.setAttrValueList(pmsBaseAttrValues);
        return pmsBaseAttrInfo1;
    }

    public List<PmsBaseAttrInfo> getAttrValueListByValueIds(HashSet<String> valueIds) {

        String values = StringUtils.join(valueIds, ",");
        return pmsBaseAttrValueMapper.selectAttrValueListByValueIds(values);

    }

}
