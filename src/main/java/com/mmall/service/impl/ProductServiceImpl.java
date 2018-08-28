package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Category;
import com.mmall.pojo.Product;
import com.mmall.service.ICategoryService;
import com.mmall.service.IProductService;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVo;
import com.mmall.vo.ProductListVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Huanyu
 * @date 2018/4/22
 */
@Service("iProductService")
public class ProductServiceImpl implements IProductService {
    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ICategoryService iCategoryService;

    @Override
    public ServerResponse saveOrUpdateProduct(Product product) {
        if (product != null) {
            if (StringUtils.isNotBlank(product.getSubImages())) {
                String[] subImageArray = product.getSubImages().split(",");
                if (subImageArray.length > 0) {
                    product.setMainImage(subImageArray[0]);
                }
            }
            if (product.getId() != null) {
                int rowCount = productMapper.updateByPrimaryKey(product);
                if (rowCount > 0) {
                    return ServerResponse.createBySuccess("更新产品成功");
                }
                return ServerResponse.createByErrorMessage("更新产品失败");
            } else {
                int rowCount = productMapper.insert(product);
                if (rowCount > 0) {
                    return ServerResponse.createBySuccess("新增产品成功");
                }
                return ServerResponse.createByErrorMessage("新增产品失败");
            }
        }
        return ServerResponse.createByErrorMessage("新增或更新产品参数不正确");
    }

    @Override
    public ServerResponse<String> setSaleStatus(Integer productId, Integer status) {
        if (productId == null || status == null) {
            return ServerResponse.createByErrorCodeMessage(
                    ResponseCode.ILLEGAL_ARGUMENT.getCode(),
                    ResponseCode.ILLEGAL_ARGUMENT.getDesc()
            );
        }
        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);
        int rowCount = productMapper.updateByPrimaryKeySelective(product);
        if (rowCount > 0) {
            return ServerResponse.createBySuccess("修改产品销售状态成功");
        }
        return ServerResponse.createBySuccessMessage("修改产品销售状态失败");
    }

    @Override
    public ServerResponse<ProductDetailVo> manageProductDetail(Integer productId) {
        if (productId == null) {
            return ServerResponse.createByErrorCodeMessage(
                    ResponseCode.ILLEGAL_ARGUMENT.getCode(),
                    ResponseCode.ILLEGAL_ARGUMENT.getDesc()
            );
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return ServerResponse.createByErrorMessage("产品已下架或者删除");
        }
        // 简单：vo对象--value object 承载对象各个值（本例采用）
        // 复杂：pojo-->bo(business object)-->vo(view object)

        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        return ServerResponse.createBySuccess(productDetailVo);
    }

    private ProductDetailVo assembleProductDetailVo(Product product) {
        ProductDetailVo productDetailVo = new ProductDetailVo();
        productDetailVo.setId(product.getId());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setMainImages(product.getMainImage());
        productDetailVo.setSubImages(product.getSubImages());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setName(product.getName());
        productDetailVo.setStock(product.getStock());
        productDetailVo.setStatus(product.getStatus());

        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if (category == null) {
            // 即该product为根节点
            productDetailVo.setParentCategoryId(0);
        } else {
            productDetailVo.setParentCategoryId(category.getParentId());

        }

        productDetailVo.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        productDetailVo.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));

        return productDetailVo;
    }

    /**
     * 后台商品列表动态分页功能实现
     * @param pageNum 商品列表分页总数
     * @param pageSize 分页条目数
     * @return 包含商品列表分页信息的响应数据对象
     */
    @Override
    public ServerResponse<PageInfo> getProductList(int pageNum, int pageSize) {
        // 1.startPage 记录一个开始
        PageHelper.startPage(pageNum, pageSize);

        // 2.填充自己的SQL查询逻辑
        List<Product> products = productMapper.selectList();
        List<ProductListVo> productListVos = Lists.newArrayList();
        for (Product productItem : products) {
            ProductListVo productListVo = assembleProductListVo(productItem);
            productListVos.add(productListVo);
        }

        // 3.pageHelper-收尾
        PageInfo pageResult = new PageInfo(products);
        pageResult.setList(productListVos);
        return ServerResponse.createBySuccess(pageResult);
    }

    private ProductListVo assembleProductListVo(Product product) {
        ProductListVo productListVo = new ProductListVo();
        productListVo.setId(product.getId());
        productListVo.setName(product.getName());
        productListVo.setCategoryId(product.getCategoryId());
        productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        productListVo.setMainImage(product.getMainImage());
        productListVo.setPrice(product.getPrice());
        productListVo.setSubtitle(product.getSubtitle());
        productListVo.setStatus(product.getStatus());
        return productListVo;
    }

    /**
     * 商品后台搜索功能实现
     * @param productName 商品名称
     * @param productId 商品id
     * @param pageNum 商品列表分页总数
     * @param pageSize 分页条目数
     * @return 包含商品列表分页信息的响应数据对象
     */
    @Override
    public ServerResponse<PageInfo> searchProduct(String productName, Integer productId, int pageNum, int pageSize) {
        // 1.startPage 记录一个开始
        PageHelper.startPage(pageNum, pageSize);
        if (StringUtils.isNotBlank(productName)) {
            // 添加"%"的目的是实现模糊查询，与ProductMapper.xml文件中的like结合使用
            // % 表示任意0个或多个字符
            // 可匹配任意类型和长度的字符，有些情况下若是中文，需要使用两个百分号（%%）表示
            // SELECT * from [user] WHERE u_name LIKE '%三%'
            // 将会把u_name为“张三”，“张猫三”、“三脚猫”，“唐三藏”等等有“三”的记录全找出来
            productName = new StringBuilder().append("%").append(productName).append("%").toString();
        }

        // 2.填充自己的SQL查询逻辑
        List<Product> products = productMapper.selectByNameAndProductId(productName, productId);
        List<ProductListVo> productListVos = Lists.newArrayList();
        for (Product productItem : products) {
            ProductListVo productListVo = assembleProductListVo(productItem);
            productListVos.add(productListVo);
        }

        // 3.pageHelper-收尾
        PageInfo pageResult = new PageInfo(products);
        pageResult.setList(productListVos);
        return ServerResponse.createBySuccess(pageResult);
    }

    /**
     * 前台获取产品详情
     * @param productId 产品id
     * @return 包含产品vo对象的响应数据对象
     */
    @Override
    public ServerResponse<ProductDetailVo> getProductDetail(Integer productId) {
        if (productId == null) {
            return ServerResponse.createByErrorCodeMessage(
                    ResponseCode.ILLEGAL_ARGUMENT.getCode(),
                    ResponseCode.ILLEGAL_ARGUMENT.getDesc()
            );
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return ServerResponse.createByErrorMessage("产品已下架或者删除");
        }
        if (product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()) {
            return ServerResponse.createByErrorMessage("产品已下架或者删除");
        }
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        return ServerResponse.createBySuccess(productDetailVo);
    }

    /**
     * 前台获取产品列表，包含产品搜索及动态排序功能的实现
     * @param keyword 产品名称
     * @param categoryId 产品分类id
     * @param pageNum 产品列表分页预设页数
     * @param pageSize 产品分页单页预设条目数
     * @param orderBy 产品排序依据
     * @return 包含产品排序信息对象的响应数据对象
     */
    @Override
    public ServerResponse<PageInfo> getProductByKeywordCategory(String keyword, Integer categoryId, int pageNum, int pageSize, String orderBy) {
        if (StringUtils.isBlank(keyword) && categoryId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        List<Integer> categoryIdList = new ArrayList<>();
        if (categoryId != null) {
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            if (category == null && StringUtils.isBlank(keyword)) {
                // 没有该分类，并且还没有关键字，此时需要返回一个空的结果集，并不报错
                PageHelper.startPage(pageNum, pageSize);
                List<ProductListVo> productListVos = Lists.newArrayList();
                PageInfo pageInfo = new PageInfo(productListVos);
                return ServerResponse.createBySuccess(pageInfo);
            }
            categoryIdList = iCategoryService.selectCategoryAndChildrenById(category.getId()).getData();
        }
        if (StringUtils.isNotBlank(keyword)) {
            keyword = new StringBuilder().append("%").append(keyword).append("%").toString();
        }
        // 排序处理
        PageHelper.startPage(pageNum, pageSize);
        if (StringUtils.isNotBlank(orderBy)) {
            if (Const.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy)) {
                String[] orderByArray = orderBy.split("_");
                PageHelper.orderBy(orderByArray[0] + " " + orderByArray[1]);
            }
        }
        List<Product> products = productMapper.selectByNameAndCategoryIds(
                StringUtils.isBlank(keyword) ? null : keyword,
                categoryIdList.size() == 0 ? null : categoryIdList);
        List<ProductListVo> productListVos = Lists.newArrayList();
        for (Product product : products) {
            ProductListVo productListVo = assembleProductListVo(product);
            productListVos.add(productListVo);
        }

        // 注意需要将products放入PageInfo构造器中
        PageInfo pageInfo = new PageInfo(products);
        pageInfo.setList(productListVos);
        return ServerResponse.createBySuccess(pageInfo);
    }
}
