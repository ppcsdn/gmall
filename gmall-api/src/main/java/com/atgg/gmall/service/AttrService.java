package com.atgg.gmall.service;

import com.atgg.gmall.bean.PmsBaseAttrInfo;

import java.util.HashSet;
import java.util.List;

/**
 * @author Pstart
 * @create 2019-10-11 22:04
 */
public interface AttrService {
    List<PmsBaseAttrInfo> getAttrInfoList(String catalog3Id);

    void saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo);

    PmsBaseAttrInfo getAttrValueList(String attrId);

    List<PmsBaseAttrInfo> getAttrValueListByValueIds(HashSet<String> valueIds);
}
