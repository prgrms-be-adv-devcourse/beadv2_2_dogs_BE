package com.barofarm.buyer.cart.history;

import com.barofarm.buyer.cart.application.dto.CartInfo;
import com.barofarm.buyer.cart.application.dto.CartItemCreateCommand;
import com.barofarm.buyer.cart.application.dto.CartItemInfo;
import com.barofarm.log.history.mapper.HistoryPayloadMapper;
import com.barofarm.log.history.model.CartEventData;
import com.barofarm.log.history.model.HistoryEventType;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CartAddHistoryPayloadMapper implements HistoryPayloadMapper {

    @Override
    public HistoryEventType supports() {
        return HistoryEventType.CART_ITEM_ADDED;
    }

    @Override
    public Object payload(Object[] args, Object returnValue) {
        CartItemCreateCommand command = null;
        if (args != null && args.length >= 3) {
            command = (CartItemCreateCommand) args[2];
        }

        UUID cartId = null;
        UUID cartItemId = null;
        String productName = null;
        String categoryCode = null;
        if (returnValue instanceof CartInfo cartInfo) {
            cartId = cartInfo.cartId();
            if (command != null) {
                CartItemInfo matchedItem = findItem(cartInfo, command);
                if (matchedItem != null) {
                    cartItemId = matchedItem.itemId();
                    productName = matchedItem.productName();
                    categoryCode = matchedItem.productCategoryCode();
                }
            }
        }

        return CartEventData.builder()
            .cartId(cartId)
            .cartItemId(cartItemId)
            .productId(command != null ? command.productId() : null)
            .productName(productName)
            .categoryCode(categoryCode)
            .quantity(command != null ? command.quantity() : null)
            .build();
    }

    private CartItemInfo findItem(CartInfo cartInfo, CartItemCreateCommand command) {
        if (cartInfo.items() == null) {
            return null;
        }
        for (CartItemInfo item : cartInfo.items()) {
            if (item.productId().equals(command.productId())) {
                if (command.inventoryId() == null
                    || command.inventoryId().equals(item.inventoryId())) {
                    return item;
                }
            }
        }
        return null;
    }
}
