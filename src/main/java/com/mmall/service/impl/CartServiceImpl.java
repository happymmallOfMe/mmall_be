package com.mmall.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CartMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Cart;
import com.mmall.pojo.Product;
import com.mmall.service.ICartService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.CartProductVo;
import com.mmall.vo.CartVo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Huanyu
 * @date 2018/4/26
 */
@Service("iCartService")
public class CartServiceImpl implements ICartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    /**
     * 添加商品到购物车
     * @param userId 用户id
     * @param count 所添加商品的数量
     * @param productId 所添加商品的id
     * @return 包含更新状态下的购物车信息的响应数据对象
     */
    @Override
    public ServerResponse<CartVo> add(Integer userId, Integer count, Integer productId) {
        if (count == null || productId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectCartByUserIdProductId(userId, productId);
        if (cart == null) {
            // 购物车中不存在该商品，需要增加一条记录
            Cart cartItem = new Cart();
            cartItem.setQuantity(count);
            cartItem.setChecked(Const.Cart.CHECKED);
            cartItem.setProductId(productId);
            cartItem.setUserId(userId);
            cartMapper.insert(cartItem);
        } else {
            // 此产品已经在购物车中，添加该商品，仅需增加其数量
            count = cart.getQuantity() + count;
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(cart);
        }
        CartVo cartVo = this.getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

    /**
     * 添加商品到购物车
     * @param userId 用户id
     * @param count 所更新商品的数量
     * @param productId 所更新商品的id
     * @return 包含更新状态下的购物车信息的响应数据对象
     */
    @Override
    public ServerResponse<CartVo> update(Integer userId, Integer count, Integer productId) {
        if (count == null || productId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectCartByUserIdProductId(userId, productId);
        // 购物车中已有该商品，则更新其数量
        if (cart != null) {
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(cart);
        }
        CartVo cartVo = this.getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

    /**
     * 通过指定商品id删除购物车中的商品
     * @param userId 用户id
     * @param productIds 用户所属购物车中要删除商品的id序列
     * @return 包含删除商品后的用户购物车信息的响应数据对象
     */
    @Override
    public ServerResponse<CartVo> deleteProduct(Integer userId, String productIds) {
        List<String> productIdList = Splitter.on(",").splitToList(productIds);
        if (CollectionUtils.isEmpty(productIdList)) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        cartMapper.deleteByUserIdProductIds(userId, productIdList);
        CartVo cartVo = this.getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

    /**
     * 查询购物车商品信息：购物车商品列表未做分页
     * @param userId 用户id
     * @return 包含用户购物车现有信息的响应数据对象
     */
    @Override
    public ServerResponse<CartVo> list(Integer userId) {
        CartVo cartVo = this.getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

    /**
     * 用户购物车商品选择或者反选
     * @param userId 用户id
     * @param productId 商品id
     * @param checked 商品是否被选上
     * @return 选中或未选中状态的用户购物车信息的相应数据对象
     */
    @Override
    public ServerResponse<CartVo> selectOrUnselect(Integer userId, Integer productId, Integer checked) {
        cartMapper.checkedOrUncheckedProduct(userId, productId, checked);
        return this.list(userId);
    }

    /**
     * 返回用户购物车中商品的总数，包括选中的和未选中的
     * @param userId 用户id
     * @return 包含用户购物车商品总数的响应数据对象
     */
    @Override
    public ServerResponse<Integer> getCartProductCount(Integer userId) {
        if (userId == null) {
            return ServerResponse.createBySuccess(0);
        }
        return ServerResponse.createBySuccess(cartMapper.selectCartProductCount(userId));
    }

    /**
     * 获取用户的购物车vo对象，该对象下包含多个商品对象
     * @param userId 购物车所属用户id
     * @return 包含购物车各属性信息的vo对象
     */
    private CartVo getCartVoLimit(Integer userId) {
        // 创建购物车vo对象
        CartVo cartVo = new CartVo();
        // 从数据库中查询出该用户的购物车商品记录集合
        List<Cart> cartList = cartMapper.selectCartByUserId(userId);
        // 创建购物车商品vo集合
        List<CartProductVo> cartProductVos = Lists.newArrayList();
        // 初始化购物车商品总价：需要考虑精度丢失问题，如0.4+0.2=0.600000000005的计算精度异常
        BigDecimal cartTotalPrice = new BigDecimal("0");
        if (CollectionUtils.isNotEmpty(cartList)) {
            for (Cart cartItem : cartList) {
                CartProductVo cartProductVo = new CartProductVo();
                // cartProductVo参数赋值1，包括相关id
                cartProductVo.setId(cartItem.getId());
                cartProductVo.setUserId(userId);
                cartProductVo.setProductId(cartItem.getProductId());

                // 获取购物车中该商品实例
                Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
                // 判断库存
                if (product != null) {

                    // cartProductVo参数赋值2，该商品相关信息
                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductSubtitle(product.getSubtitle());
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductStatus(product.getStatus());
                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductStock(product.getStock());

                    // cartProductVo参数赋值3，该商品购买数量，需要考虑库存对商品购买数量的限制
                    int buyLimitCount = 0;
                    if (product.getStock() >= cartItem.getQuantity()) {
                        // 购物车中该商品库存充足时
                        buyLimitCount = cartItem.getQuantity();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                    } else {
                        // 购物车中该商品库存不充足时
                        buyLimitCount = product.getStock();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);

                        // 将购物车中该商品的购买数量更新为有效库存
                        Cart cartForQuantity = new Cart();
                        cartForQuantity.setId(cartItem.getId());
                        cartForQuantity.setQuantity(buyLimitCount);
                        cartMapper.updateByPrimaryKeySelective(cartForQuantity);
                    }
                    cartProductVo.setQuantity(buyLimitCount);

                    // cartProductVo参数赋值4，计算该商品的总价
                    cartProductVo.setProductTotalPrice(
                            BigDecimalUtil.mul(product.getPrice().doubleValue(), cartProductVo.getQuantity())
                    );
                    cartProductVo.setProductChecked(cartItem.getChecked());
                }
                // 如果购物车中该商品已经被勾选，则将该商品总价添加到购物车商品总价里
                if (cartItem.getChecked() == Const.Cart.CHECKED) {
                    cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(), cartProductVo.getProductTotalPrice().doubleValue());
                }
                // 将购物车单个商品的CartProductVo对象添加到购物车商品集合（cartProductVos）中，
                cartProductVos.add(cartProductVo);
            }
        }
        // 设置购物车的相关属性
        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setCartProductVos(cartProductVos);
        cartVo.setAllChecked(this.getAllCheckedStatus(userId));
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        return cartVo;
    }

    private boolean getAllCheckedStatus(Integer userId) {
        if (userId == null) {
            return false;
        }
        return cartMapper.selectCartProductCheckedStatusByUserId(userId) == 0;
    }

}
