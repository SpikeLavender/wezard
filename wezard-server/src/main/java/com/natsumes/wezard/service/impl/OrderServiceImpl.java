package com.natsumes.wezard.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.natsumes.wezard.entity.Cart;
import com.natsumes.wezard.entity.Response;
import com.natsumes.wezard.entity.form.OrderCreateForm;
import com.natsumes.wezard.entity.vo.OrderItemVo;
import com.natsumes.wezard.entity.vo.OrderVo;
import com.natsumes.wezard.enums.OrderStatusEnum;
import com.natsumes.wezard.enums.PaymentTypeEnum;
import com.natsumes.wezard.enums.ProductStatusEnum;
import com.natsumes.wezard.enums.ResponseEnum;
import com.natsumes.wezard.pojo.*;
import com.natsumes.wezard.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.natsumes.wezard.enums.ResponseEnum.CART_SELECTED_IS_EMPTY;


@Service
@EnableBinding(Sink.class)
@Slf4j
public class OrderServiceImpl implements OrderService {

	@Reference
	private DubboShippingService dubboShippingService;

	@Autowired
	private CartService cartService;

    @Reference
    private DubboProductService dubboProductService;

	@Reference
	private DubboOrderService dubboOrderService;

	@Reference
	private DubboOrderItemService dubboOrderItemService;

	private final static String PAY_SUCCESS = "SUCCESS";

    @Override
    public Response<OrderVo> create(Integer uId, OrderCreateForm orderCreateForm) {
        if (orderCreateForm.getCreateType() != null && orderCreateForm.getCreateType() == 1) {
            return create(uId, orderCreateForm.getProductId(), orderCreateForm.getProductNum(), orderCreateForm.getShippingId());
        }
        return create(uId, orderCreateForm.getShippingId());
    }


    @Transactional
    public Response<OrderVo> create(Integer uId, Integer productId, Integer productNum, Integer shippingId) {
        //收货地址校验（总之要查出来）
        Shipping shipping = dubboShippingService.selectByUidAndShippingId(uId, shippingId);
        if (shipping == null) {
            return Response.error(ResponseEnum.SHIPPING_NOT_EXIST);
        }

        if (productNum == null || productNum < 1) {
            return Response.error(ResponseEnum.SYSTEM_ERROR, "商品数量无效");
        }
        //根据productId查数据库
        Product product = dubboProductService.selectByPrimaryKey(productId);
        //是否有商品
        if (product == null) {
            return Response.error(ResponseEnum.PRODUCT_NOT_EXIST, "商品不存在. productId = " + productId);
        }
        //上下架状态校验
        if (!ProductStatusEnum.ON_SALE.getCode().equals(product.getStatus())) {
            return Response.error(ResponseEnum.PRODUCT_OFF_SALE_OR_DELETE, "商品不是在售状态. " + product.getName());
        }
        //库存是否充足
        if (product.getStock() < productNum) {
            return Response.error(ResponseEnum.PRODUCT_STOCK_ERROR, "商品库存不足. " + product.getName());
        }

        List<OrderItem> orderItems = new ArrayList<>();
        String orderNo = generateOrderNo();

        OrderItem orderItem = buildOrderItem(uId, orderNo, productNum, product);
        orderItems.add(orderItem);

        //减库存
        product.setStock(product.getStock() - productNum);
        int row = dubboProductService.updateByPrimaryKeySelective(product);
        if (row <= 0) {
            return Response.error(ResponseEnum.SYSTEM_ERROR);
        }

        //计算总价，只计算被选中的商品
        //生成订单，入库：order 和 order_item，事务控制
        Order order = buildOrder(uId, orderNo, shippingId, orderItems);
        int rowForOrder;
        rowForOrder = dubboOrderService.insertSelective(order);
        if (rowForOrder <= 0 ) {
            return Response.error(ResponseEnum.SYSTEM_ERROR);
        }

        int rowOrderItem = dubboOrderItemService.batchInsert(orderItems);
        if (rowOrderItem <= 0 ) {
            return Response.error(ResponseEnum.SYSTEM_ERROR);
        }

        //构造orderVo对象
        OrderVo orderVo = buildOrderVo(order, orderItems, shipping);
        return Response.success(orderVo);
    }


	@Transactional
	public Response<OrderVo> create(Integer uId, Integer shippingId) {
		//收货地址校验（总之要查出来）
		Shipping shipping = dubboShippingService.selectByUidAndShippingId(uId, shippingId);
		if (shipping == null) {
			return Response.error(ResponseEnum.SHIPPING_NOT_EXIST);
		}

		//获取购物车，校验（是否有商品、库存）
		List<Cart> carts = cartService.listForCart(uId).stream()
				.filter(Cart::getProductSelected)
				.collect(Collectors.toList());
		if (CollectionUtils.isEmpty(carts)) {
			return Response.error(CART_SELECTED_IS_EMPTY);
		}

		//获取carts里的 productIds
		Set<Integer> productIdSet = carts.stream().map(Cart::getProductId).collect(Collectors.toSet());
		List<Product> products = dubboProductService.selectByProductIdSet(productIdSet);
		Map<Integer, Product> map = products.stream()
				.collect(Collectors.toMap(Product::getId, product -> product));

		List<OrderItem> orderItems = new ArrayList<>();
		String orderNo = generateOrderNo();

		for (Cart cart : carts) {
			//根据productId查数据库
			Product product = map.get(cart.getProductId());
			//是否有商品
			if (product == null) {
				return Response.error(ResponseEnum.PRODUCT_NOT_EXIST, "商品不存在. productId = " + cart.getProductId());
			}
			//上下架状态校验
			if (!ProductStatusEnum.ON_SALE.getCode().equals(product.getStatus())) {
				return Response.error(ResponseEnum.PRODUCT_OFF_SALE_OR_DELETE, "商品不是在售状态. " + product.getName());
			}
			//库存是否充足
			if (product.getStock() < cart.getQuantity()) {
				return Response.error(ResponseEnum.PRODUCT_STOCK_ERROR, "商品库存不足. " + product.getName());
			}

			OrderItem orderItem = buildOrderItem(uId, orderNo, cart.getQuantity(), product);
			orderItems.add(orderItem);

			//减库存
			product.setStock(product.getStock() - cart.getQuantity());
			int row = dubboProductService.updateByPrimaryKeySelective(product);
			if (row <= 0) {
				return Response.error(ResponseEnum.SYSTEM_ERROR);
			}
		}

		//计算总价，只计算被选中的商品
		//生成订单，入库：order 和 order_item，事务控制
		Order order = buildOrder(uId, orderNo, shippingId, orderItems);
		int rowForOrder = dubboOrderService.insertSelective(order);

		if (rowForOrder <= 0 ) {
			return Response.error(ResponseEnum.SYSTEM_ERROR);
		}

		int rowForOrderItem = dubboOrderItemService.batchInsert(orderItems);
		if (rowForOrderItem <= 0 ) {
			return Response.error(ResponseEnum.SYSTEM_ERROR);
		}

		//更新购物车（选中的商品）
		//redis有事务（打包命令），不能回滚
		for (Cart cart : carts) {
			cartService.delete(uId, cart.getProductId());
		}

		//构造orderVo对象
		OrderVo orderVo = buildOrderVo(order, orderItems, shipping);
		return Response.success(orderVo);
	}

	@Override
    @SuppressWarnings("unchecked")
	public Response<PageInfo> list(Integer uId, Integer pageNum, Integer pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		List<Order> orders = dubboOrderService.selectByUid(uId);
        PageInfo pageInfo = new PageInfo<>(orders);

		Set<String> orderNoSet = orders.stream().map(Order::getOrderNo).collect(Collectors.toSet());
		List<OrderItem> orderItems = dubboOrderItemService.selectByOrderNoSet(orderNoSet);
		Map<String, List<OrderItem>> orderItemMap = orderItems.stream()
				.collect(Collectors.groupingBy(OrderItem::getOrderNo));

		Set<Integer> shippingIdSet = orders.stream().map(Order::getShippingId).collect(Collectors.toSet());
		List<Shipping> shippings = dubboShippingService.selectByIdSet(shippingIdSet);
		Map<Integer, Shipping> shippingMap = shippings.stream()
				.collect(Collectors.toMap(Shipping::getId, shipping -> shipping));


		List<OrderVo> orderVos = new ArrayList<>();
		for (Order order : orders) {
			OrderVo orderVo = buildOrderVo(order, orderItemMap.get(order.getOrderNo()),
					shippingMap.get(order.getShippingId()));
			orderVos.add(orderVo);
		}

		pageInfo.setList(orderVos);

		return Response.success(pageInfo);
	}

	@Override
	public Response<OrderVo> detail(Integer uId, String orderNo) {
		Order order = dubboOrderService.selectByOrderNo(orderNo);
		if (order == null || !order.getUserId().equals(uId)) {
			return Response.error(ResponseEnum.ORDER_NOT_EXIST);
		}

		Set<String> orderNoSet = new HashSet<>();
		orderNoSet.add(order.getOrderNo());
		List<OrderItem> orderItems = dubboOrderItemService.selectByOrderNoSet(orderNoSet);

		Shipping shipping = dubboShippingService.selectByPrimaryKey(order.getShippingId());

		OrderVo orderVo = buildOrderVo(order, orderItems, shipping);
		return Response.success(orderVo);
	}

	@Override
	public Response cancel(Integer uId, String orderNo) {
		Order order = dubboOrderService.selectByOrderNo(orderNo);
		if (order == null || !order.getUserId().equals(uId)) {
			return Response.error(ResponseEnum.ORDER_NOT_EXIST);
		}

		//只有未付款才可以取消,看自己业务 todo
		if (!OrderStatusEnum.NO_PAY.getCode().equals(order.getStatus())) {
			return Response.error(ResponseEnum.ORDER_STATUS_ERROR);
		}

		order.setStatus(OrderStatusEnum.CANCELED.getCode());
		order.setCloseTime(new Date());
		int row = dubboOrderService.updateByPrimaryKeySelective(order);
		if (row <= 0) {
			return Response.error(ResponseEnum.SYSTEM_ERROR);
		}
		return Response.success();
	}

	@Override
	public void paid(String orderNo) {
		Order order = dubboOrderService.selectByOrderNo(orderNo);
		if (order == null) {
			//todo 告警
			throw new RuntimeException(ResponseEnum.ORDER_NOT_EXIST.getDesc() + ", 订单id: " + orderNo);
		}

		//只有[未付款]可以变成[已付款], 看自己业务 todo
		if (!OrderStatusEnum.NO_PAY.getCode().equals(order.getStatus())) {
			throw new RuntimeException(ResponseEnum.ORDER_STATUS_ERROR.getDesc() + ", 订单id: " + orderNo);
		}

		order.setStatus(OrderStatusEnum.PAID.getCode());
        //todo payInfo 增加支付时间字段
		order.setPaymentTime(new Date());
		int row = dubboOrderService.updateByPrimaryKeySelective(order);
		if (row <= 0) {
			throw new RuntimeException("将订单更新为已支付状态失败, 订单id: " + orderNo);
		}
	}

    private OrderVo buildOrderVo(Order order, List<OrderItem> orderItems, Shipping shipping) {
		OrderVo orderVo = new OrderVo();
		BeanUtils.copyProperties(order, orderVo);

		List<OrderItemVo> orderItemVos = orderItems.stream().map(e -> {
			OrderItemVo orderItemVo = new OrderItemVo();
			BeanUtils.copyProperties(e, orderItemVo);
			return orderItemVo;
		}).collect(Collectors.toList());

		orderVo.setOrderItemVoList(orderItemVos);

		if (shipping != null) {
			orderVo.setShippingId(shipping.getId());
			orderVo.setShippingVo(shipping);
		}

		return orderVo;
	}

	private Order buildOrder(Integer uId, String orderNo, Integer shippingId, List<OrderItem> orderItems) {

		BigDecimal payment = orderItems.stream()
				.map(OrderItem::getTotalPrice)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		Order order = new Order();
		order.setUserId(uId);
		order.setOrderNo(orderNo);
		order.setShippingId(shippingId);
		order.setPayment(payment);
		order.setPaymentType(PaymentTypeEnum.PAY_ONLINE.getCode());
		order.setPostage(0);
		order.setStatus(OrderStatusEnum.NO_PAY.getCode());

		return order;
	}

	/**
	 * 企业级：分布式唯一 ID
	 * todo
	 * @return
	 */
	private String generateOrderNo() {
		return String.valueOf(System.currentTimeMillis());
	}

	private OrderItem buildOrderItem(Integer uId, String orderNo, Integer quantity, Product product) {
		OrderItem orderItem = new OrderItem();
		orderItem.setUserId(uId);
		orderItem.setOrderNo(orderNo);
		orderItem.setProductId(product.getId());
		orderItem.setProductImage(product.getMainImage());
		orderItem.setProductName(product.getName());
		orderItem.setCurrentUnitPrice(product.getPrice());
		orderItem.setQuantity(quantity);
		orderItem.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
		return orderItem;
	}

	@StreamListener(Sink.INPUT)
    public void processPayInfo(Message<PayInfo> message) {
        log.info("接收到消息 => {}", message);
//        PayInfo payInfo = JSON.parseObject(msg, PayInfo.class);
        PayInfo payInfo = message.getPayload();

        log.info("接收到消息 => {}", payInfo);
        if (PAY_SUCCESS.equals(payInfo.getPlatformStatus())) {
            //修改订单里的状态
            this.paid(payInfo.getOrderNo());
        }

    }
}
