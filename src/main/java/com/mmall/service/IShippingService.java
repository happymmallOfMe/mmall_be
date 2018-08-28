package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Shipping; /**
 * 购物地址业务层接口
 *
 * @author Huanyu
 * @date 2018/4/27
 */
public interface IShippingService {
    ServerResponse add(Integer userId, Shipping shipping);

    ServerResponse update(Integer userId, Shipping shipping);

    ServerResponse delete(Integer userId, Integer shippingId);

    ServerResponse<Shipping> select(Integer userId, Integer shippingId);

    ServerResponse<PageInfo> list(Integer userId, int pageNum, int pageSize);
}
