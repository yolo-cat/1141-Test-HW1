package com.deliveryplatform.models;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents an item within an order with pricing and quantity information.
 */
public class OrderItem {

    @NotBlank(message = "Item ID cannot be blank")
    private String itemId;

    @NotBlank(message = "Item name cannot be blank")
    private String name;

    @NotNull(message = "Quantity cannot be null")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    @NotNull(message = "Unit price cannot be null")
    @DecimalMin(value = "0.01", message = "Unit price must be at least 0.01")
    private BigDecimal unitPrice;

    @NotNull(message = "Total price cannot be null")
    @DecimalMin(value = "0.01", message = "Total price must be at least 0.01")
    private BigDecimal totalPrice;

    private String description;
    private String category;

    /**
     * Default constructor for frameworks.
     */
    public OrderItem() {
    }

    /**
     * Constructor for creating an order item.
     * 
     * @param itemId the unique identifier for this item
     * @param name the name of the item
     * @param quantity the quantity ordered
     * @param unitPrice the price per unit
     */
    public OrderItem(String itemId, String name, Integer quantity, BigDecimal unitPrice) {
        this.itemId = itemId;
        this.name = name;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = calculateTotalPrice();
    }

    /**
     * Constructor for creating an order item with description.
     * 
     * @param itemId the unique identifier for this item
     * @param name the name of the item
     * @param quantity the quantity ordered
     * @param unitPrice the price per unit
     * @param description optional description of the item
     */
    public OrderItem(String itemId, String name, Integer quantity, BigDecimal unitPrice, String description) {
        this(itemId, name, quantity, unitPrice);
        this.description = description;
    }

    /**
     * Calculates the total price for this item (quantity * unitPrice).
     * 
     * @return the total price
     */
    private BigDecimal calculateTotalPrice() {
        if (quantity != null && unitPrice != null) {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }

    /**
     * Updates the quantity and recalculates the total price.
     * 
     * @param newQuantity the new quantity
     */
    public void updateQuantity(Integer newQuantity) {
        if (newQuantity == null || newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.quantity = newQuantity;
        this.totalPrice = calculateTotalPrice();
    }

    /**
     * Updates the unit price and recalculates the total price.
     * 
     * @param newUnitPrice the new unit price
     */
    public void updateUnitPrice(BigDecimal newUnitPrice) {
        if (newUnitPrice == null || newUnitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Unit price must be positive");
        }
        this.unitPrice = newUnitPrice;
        this.totalPrice = calculateTotalPrice();
    }

    // Getters and Setters

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        this.totalPrice = calculateTotalPrice();
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        this.totalPrice = calculateTotalPrice();
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return Objects.equals(itemId, orderItem.itemId) &&
               Objects.equals(name, orderItem.name) &&
               Objects.equals(quantity, orderItem.quantity) &&
               Objects.equals(unitPrice, orderItem.unitPrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, name, quantity, unitPrice);
    }

    @Override
    public String toString() {
        return String.format("OrderItem{itemId='%s', name='%s', quantity=%d, unitPrice=%s, totalPrice=%s}",
                           itemId, name, quantity, unitPrice, totalPrice);
    }
}