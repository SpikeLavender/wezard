package com.natsumes.wezard.service.impl;

import com.alibaba.fastjson.JSON;
import com.natsumes.wezard.entity.Cart;
import com.natsumes.wezard.entity.Response;
import com.natsumes.wezard.entity.form.CartAddForm;
import com.natsumes.wezard.entity.form.CartUpdateForm;
import com.natsumes.wezard.entity.vo.CartProductVo;
import com.natsumes.wezard.entity.vo.CartVo;
import com.natsumes.wezard.enums.ProductStatusEnum;
import com.natsumes.wezard.enums.ResponseEnum;
import com.natsumes.wezard.pojo.Product;
import com.natsumes.wezard.service.CartService;
import com.natsumes.wezard.service.DubboProductService;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.natsumes.wezard.consts.StefanieConst.CART_REDIS_KEY_TEMPLATE;


@Service
public class CartServiceImpl implements CartService {

    @Reference
    private DubboProductService dubboProductService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public Response<CartVo> add(Integer uId, CartAddForm form) {

        Integer quantity = form.getQuantity();

        Product product = dubboProductService.selectByPrimaryKey(form.getProductId());

        //判断商品是否存在
        if (product == null) {
            return Response.error(ResponseEnum.PRODUCT_NOT_EXIST);
        }

        //商品是否正常在售
        if (!product.getStatus().equals(ProductStatusEnum.ON_SALE.getCode())) {
            return Response.error(ResponseEnum.PRODUCT_OFF_SALE_OR_DELETE);
        }

        //商品库存是否充足
        if (product.getStock() < quantity) {
            return Response.error(ResponseEnum.PRODUCT_STOCK_ERROR);
        }

        //写入redis
        //key: cart_1
        HashOperations<String, String, String> opsForHash = redisTemplate.opsForHash();
        String redisKey = String.format(CART_REDIS_KEY_TEMPLATE, uId);
        String value = opsForHash.get(redisKey, String.valueOf(product.getId()));

        Cart cart;
        if (StringUtils.isEmpty(value)) {
            //没有该商品
            cart = new Cart(product.getId(), quantity, form.getSelected());
        } else {
            //已经有了，数量 +1
            cart = JSON.parseObject(value, Cart.class);
            cart.setQuantity(cart.getQuantity() + quantity);
            cart.setProductSelected(form.getSelected());
        }

        opsForHash.put(redisKey, String.valueOf(product.getId()), JSON.toJSONString(cart));

        return list(uId);
    }

    @Override
    public Response<CartVo> list(Integer uId) {

        CartVo cartVo = new CartVo();

        HashOperations<String, String, String> opsForHash = redisTemplate.opsForHash();
        String redisKey = String.format(CART_REDIS_KEY_TEMPLATE, uId);

        Map<String, String> entries = opsForHash.entries(redisKey);

        Set<Integer> productIdSet = entries.keySet().stream().map(Integer::valueOf).collect(Collectors.toSet());

        List<Product> products = dubboProductService.selectByProductIdSet(productIdSet);

        Map<Integer, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        boolean selectedAll = true;
        Integer cartTotalQuantity = 0;
        Integer cartSelectedQuantity = 0;
        BigDecimal cartTotalPrice = BigDecimal.ZERO;

        List<CartProductVo> cartProductVos = new ArrayList<>();
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            Integer productId = Integer.valueOf(entry.getKey());
            Cart cart = JSON.parseObject(entry.getValue(), Cart.class);

            Product product = productMap.get(productId);

            if (product != null) {
                CartProductVo cartProductVo = new CartProductVo(productId,
                        cart.getQuantity(),
                        product.getName(),
                        product.getSubtitle(),
                        product.getMainImage(),
                        product.getPrice(),
                        product.getStatus(),
                        product.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())),
                        product.getStock(),
                        cart.getProductSelected()
                );
                cartProductVos.add(cartProductVo);

                if (!cart.getProductSelected()) {
                    selectedAll = false;
                }
                //计算总价，只计算选中的
                if (cart.getProductSelected()) {
                    cartTotalPrice = cartTotalPrice.add(cartProductVo.getProductTotalPrice());
                    //只计算选中的
                    cartSelectedQuantity += cartProductVo.getQuantity();
                }
            }

            cartTotalQuantity += cart.getQuantity();
        }
        cartVo.setCartProductVoList(cartProductVos);
        //有一个没选中，就不叫全选
        cartVo.setSelectedAll(selectedAll);
        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setCartTotalQuantity(cartTotalQuantity);
        cartVo.setCartSelectedQuantity(cartSelectedQuantity);
        return Response.success(cartVo);
    }

    @Override
    public Response<CartVo> listNo() {
        CartVo cartVo = new CartVo();
        cartVo.setCartProductVoList(new ArrayList<>());
        cartVo.setSelectedAll(false);
        cartVo.setCartTotalPrice(BigDecimal.ZERO);
        cartVo.setCartTotalQuantity(0);
        cartVo.setCartSelectedQuantity(0);
        return Response.success(cartVo);
    }

    @Override
    public Response<Boolean> exist(Integer uId, Integer productId) {

        HashOperations<String, String, String> opsForHash = redisTemplate.opsForHash();
        String redisKey = String.format(CART_REDIS_KEY_TEMPLATE, uId);
        String value = opsForHash.get(redisKey, String.valueOf(productId));

        if (StringUtils.isEmpty(value)) {
            //没有该商品, 报错
            return Response.success(false);
        }
        //已经有了
        return Response.success(true);
    }

    @Override
    public Response<CartVo> update(Integer uId, Integer productId, CartUpdateForm form) {

        HashOperations<String, String, String> opsForHash = redisTemplate.opsForHash();
        String redisKey = String.format(CART_REDIS_KEY_TEMPLATE, uId);
        String value = opsForHash.get(redisKey, String.valueOf(productId));

        if (StringUtils.isEmpty(value)) {
            //没有该商品, 报错
            return Response.error(ResponseEnum.CART_PRODUCT_NOT_EXIST);
        }
        //已经有了, 修改内容
        Cart cart = JSON.parseObject(value, Cart.class);
        if (form.getQuantity() != null && form.getQuantity() >= 0) {
            cart.setQuantity(form.getQuantity());
        }

        if (form.getSelected() != null) {
            cart.setProductSelected(form.getSelected());
        }

        opsForHash.put(redisKey, String.valueOf(productId), JSON.toJSONString(cart));

        return list(uId);
    }

    @Override
    public Response<CartVo> delete(Integer uId, Integer productId) {
        String redisKey = String.format(CART_REDIS_KEY_TEMPLATE, uId);

        HashOperations<String, String, String> opsForHash = redisTemplate.opsForHash();
        String value = opsForHash.get(redisKey, String.valueOf(productId));

        if (StringUtils.isEmpty(value)) {
            //没有该商品, 报错
            return Response.error(ResponseEnum.CART_PRODUCT_NOT_EXIST);
        }
        //已经有了, 修改内容

        opsForHash.delete(redisKey, String.valueOf(productId));

        return list(uId);
    }

    @Override
    public Response<CartVo> selectAll(Integer uId) {
        HashOperations<String, String, String> opsForHash = redisTemplate.opsForHash();
        String redisKey = String.format(CART_REDIS_KEY_TEMPLATE, uId);
        listForCart(uId).forEach(cart -> {
            cart.setProductSelected(true);
            opsForHash.put(redisKey, String.valueOf(cart.getProductId()), JSON.toJSONString(cart));
        });

        return list(uId);
    }

    @Override
    public Response<CartVo> unSelectAll(Integer uId) {
        HashOperations<String, String, String> opsForHash = redisTemplate.opsForHash();
        String redisKey = String.format(CART_REDIS_KEY_TEMPLATE, uId);
        listForCart(uId).forEach(cart -> {
            cart.setProductSelected(false);
            opsForHash.put(redisKey, String.valueOf(cart.getProductId()), JSON.toJSONString(cart));
        });

        return list(uId);
    }

    //item
    @Override
    public Response<Integer> sum(Integer uId) {
        Integer sum = listForCart(uId).size();
        return Response.success(sum);
    }

    public List<Cart> listForCart(Integer uId) {
        HashOperations<String, String, String> opsForHash = redisTemplate.opsForHash();
        String redisKey = String.format(CART_REDIS_KEY_TEMPLATE, uId);

        Map<String, String> entries = opsForHash.entries(redisKey);

        List<Cart> carts = new ArrayList<>();

        for (Map.Entry<String, String> entry : entries.entrySet()) {
            carts.add(JSON.parseObject(entry.getValue(), Cart.class));
        }

        return carts;
    }
}
