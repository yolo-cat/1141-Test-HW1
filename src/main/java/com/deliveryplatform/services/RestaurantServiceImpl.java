package com.deliveryplatform.services;

import com.deliveryplatform.exceptions.InvalidOrderStateException;
import com.deliveryplatform.exceptions.RestaurantUnavailableException;
import com.deliveryplatform.models.Order;
import com.deliveryplatform.models.OrderStatus;
import com.deliveryplatform.models.Restaurant;
import com.deliveryplatform.repositories.OrderRepository;
import com.deliveryplatform.repositories.RestaurantRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

/**
 * 餐廳服務實現 - 處理餐廳相關的訂單操作
 * 簡化版本，專注於核心功能：收單、開始製作、完成製作
 */
public class RestaurantServiceImpl implements RestaurantService {

    private static final Logger logger = LogManager.getLogger(RestaurantServiceImpl.class);

    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;

    public RestaurantServiceImpl(OrderRepository orderRepository, 
                               RestaurantRepository restaurantRepository) {
        this.orderRepository = orderRepository;
        this.restaurantRepository = restaurantRepository;
    }

    /**
     * 餐廳收單 - 核心需求方法
     */
    @Override
    public void acceptOrder(String orderId, String restaurantId) 
            throws RestaurantUnavailableException, InvalidOrderStateException {
        
        logger.info("餐廳 {} 嘗試接單 {}", restaurantId, orderId);
        
        try {
            // 驗證餐廳狀態
            Restaurant restaurant = validateRestaurantForOrder(restaurantId);
            
            // 驗證訂單狀態
            Order order = validateOrderForAcceptance(orderId, restaurantId);
            
            // 接受訂單
            order.accept("餐廳 " + restaurantId + " 接受了訂單");
            orderRepository.save(order);
            
            logger.info("餐廳 {} 成功接單 {}", restaurantId, orderId);
            
        } catch (RestaurantUnavailableException | InvalidOrderStateException e) {
            logger.warn("業務異常 - acceptOrder: orderId={}, restaurantId={}, 錯誤={}", 
                       orderId, restaurantId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("系統異常 - acceptOrder: orderId={}, restaurantId={}", orderId, restaurantId, e);
            throw new RuntimeException("接單失敗：系統錯誤", e);
        }
    }

    /**
     * 餐廳拒單 - 基本功能
     */
    @Override
    public void rejectOrder(String orderId, String restaurantId, String reason) 
            throws InvalidOrderStateException {
        
        logger.info("餐廳 {} 拒絕訂單 {} - 原因: {}", restaurantId, orderId, reason);
        
        try {
            Order order = validateOrderForRejection(orderId, restaurantId);
            order.reject(reason);
            orderRepository.save(order);
            
            logger.info("餐廳 {} 成功拒絕訂單 {} - 原因: {}", restaurantId, orderId, reason);
            
        } catch (InvalidOrderStateException e) {
            logger.warn("業務異常 - rejectOrder: orderId={}, restaurantId={}, 錯誤={}", 
                       orderId, restaurantId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("系統異常 - rejectOrder: orderId={}, restaurantId={}", orderId, restaurantId, e);
            throw new RuntimeException("拒單失敗：系統錯誤", e);
        }
    }

    /**
     * 標記訂單完成製作，可供外送 - 核心需求方法
     */
    @Override
    public void markOrderReady(String orderId, String restaurantId) 
            throws InvalidOrderStateException {
        
        logger.info("餐廳 {} 標記訂單 {} 完成製作", restaurantId, orderId);
        
        try {
            Order order = validateOrderForReadyMarking(orderId, restaurantId);
            order.markReadyForDelivery();
            orderRepository.save(order);
            
            logger.info("餐廳 {} 成功標記訂單 {} 完成製作", restaurantId, orderId);
            
        } catch (InvalidOrderStateException e) {
            logger.warn("業務異常 - markOrderReady: orderId={}, restaurantId={}, 錯誤={}", 
                       orderId, restaurantId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("系統異常 - markOrderReady: orderId={}, restaurantId={}", orderId, restaurantId, e);
            throw new RuntimeException("標記完成失敗：系統錯誤", e);
        }
    }

    /**
     * 餐廳開始製作訂單 - 核心需求方法
     */
    @Override
    public void startPreparingOrder(String orderId, String restaurantId) 
            throws InvalidOrderStateException {
        
        logger.info("餐廳 {} 開始製作訂單 {}", restaurantId, orderId);
        
        try {
            Order order = validateOrderForPreparation(orderId, restaurantId);
            order.startPreparing();
            orderRepository.save(order);
            
            logger.info("餐廳 {} 成功開始製作訂單 {}", restaurantId, orderId);
            
        } catch (InvalidOrderStateException e) {
            logger.warn("業務異常 - startPreparingOrder: orderId={}, restaurantId={}, 錯誤={}", 
                       orderId, restaurantId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("系統異常 - startPreparingOrder: orderId={}, restaurantId={}", orderId, restaurantId, e);
            throw new RuntimeException("開始製作失敗：系統錯誤", e);
        }
    }

    // 以下方法簡化為基本實現，專注核心功能

    @Override
    public boolean canAcceptOrders(String restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .map(Restaurant::canAcceptOrder)
                .orElse(false);
    }

    @Override
    public int getRemainingCapacity(String restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .map(Restaurant::getRemainingCapacity)
                .orElse(0);
    }

    @Override
    public void disableOrderAcceptance(String restaurantId, String reason) {
        restaurantRepository.findById(restaurantId).ifPresent(restaurant -> {
            restaurant.stopAcceptingOrders();
            restaurantRepository.save(restaurant);
            logger.info("餐廳 {} 停止接單 - 原因: {}", restaurantId, reason);
        });
    }

    @Override
    public void enableOrderAcceptance(String restaurantId) {
        restaurantRepository.findById(restaurantId).ifPresent(restaurant -> {
            restaurant.startAcceptingOrders();
            restaurantRepository.save(restaurant);
            logger.info("餐廳 {} 開始接單", restaurantId);
        });
    }

    // 簡化的驗證方法，專注核心邏輯

    private Restaurant validateRestaurantForOrder(String restaurantId) 
            throws RestaurantUnavailableException {
        
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantUnavailableException(restaurantId, "餐廳不存在"));
        
        if (!restaurant.canAcceptOrder()) {
            String reason = restaurant.isOpen() ? "餐廳暫停接單" : "餐廳已關閉";
            throw new RestaurantUnavailableException(restaurantId, reason);
        }
        
        return restaurant;
    }

    private Order validateOrderForAcceptance(String orderId, String restaurantId) 
            throws InvalidOrderStateException {
        
        Order order = getOrderAndValidateOwnership(orderId, restaurantId);
        
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStateException(orderId, order.getStatus(), OrderStatus.ACCEPTED,
                                               "訂單必須為 PENDING 狀態才能接單");
        }
        
        return order;
    }

    private Order validateOrderForRejection(String orderId, String restaurantId) 
            throws InvalidOrderStateException {
        
        Order order = getOrderAndValidateOwnership(orderId, restaurantId);
        
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStateException(orderId, order.getStatus(), OrderStatus.REJECTED,
                                               "訂單必須為 PENDING 狀態才能拒單");
        }
        
        return order;
    }

    private Order validateOrderForPreparation(String orderId, String restaurantId) 
            throws InvalidOrderStateException {
        
        Order order = getOrderAndValidateOwnership(orderId, restaurantId);
        
        if (order.getStatus() != OrderStatus.ACCEPTED) {
            throw new InvalidOrderStateException(orderId, order.getStatus(), OrderStatus.PREPARING,
                                               "訂單必須為 ACCEPTED 狀態才能開始製作");
        }
        
        return order;
    }

    private Order validateOrderForReadyMarking(String orderId, String restaurantId) 
            throws InvalidOrderStateException {
        
        Order order = getOrderAndValidateOwnership(orderId, restaurantId);
        
        if (order.getStatus() != OrderStatus.PREPARING) {
            throw new InvalidOrderStateException(orderId, order.getStatus(), OrderStatus.READY_FOR_DELIVERY,
                                               "訂單必須為 PREPARING 狀態才能標記完成");
        }
        
        return order;
    }

    private Order getOrderAndValidateOwnership(String orderId, String restaurantId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("訂單不存在: " + orderId));
        
        if (!restaurantId.equals(order.getRestaurantId())) {
            throw new IllegalArgumentException(
                String.format("訂單 %s 不屬於餐廳 %s", orderId, restaurantId));
        }
        
        return order;
    }
}