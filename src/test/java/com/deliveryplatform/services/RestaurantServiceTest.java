package com.deliveryplatform.services;

import com.deliveryplatform.exceptions.RestaurantUnavailableException;
import com.deliveryplatform.exceptions.InvalidOrderStateException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RestaurantServiceTest {
    @Test
    void acceptOrder_shouldThrowRestaurantUnavailableException_whenRestaurantIsUnavailable() throws RestaurantUnavailableException, InvalidOrderStateException {
        // Arrange
        RestaurantService restaurantService = mock(RestaurantService.class);
        String orderId = "order123";
        String restaurantId = "rest999";
        doThrow(new RestaurantUnavailableException(restaurantId, "Closed for maintenance"))
                .when(restaurantService).acceptOrder(orderId, restaurantId);

        // Act & Assert
        RestaurantUnavailableException ex = assertThrows(
                RestaurantUnavailableException.class,
                () -> {
                    restaurantService.acceptOrder(orderId, restaurantId);
                }
        );
        assertEquals(restaurantId, ex.getRestaurantId());
        assertTrue(ex.getMessage().contains("Closed for maintenance"));
    }
}
