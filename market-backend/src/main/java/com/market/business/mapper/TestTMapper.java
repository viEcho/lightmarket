package com.market.business.mapper;

import com.market.business.entity.TestT;



/**
 * TODO 请添加类注释
 *
 * @author viEcho
 * @date 2026/01/20
 */
public interface TestTMapper {
    int insert(TestT record);

    TestT selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TestT record);

    int updateByPrimaryKey(TestT record);
}