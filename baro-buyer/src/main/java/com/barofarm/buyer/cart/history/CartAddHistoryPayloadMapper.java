package com.barofarm.buyer.cart.history;

import com.barofarm.buyer.cart.application.dto.AddItemCommand;
import com.barofarm.buyer.cart.application.dto.CartInfo;
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
        AddItemCommand command = null;
        if (args != null && args.length >= 3) {
            command = (AddItemCommand) args[2];
        }

        UUID cartId = null;
        UUID cartItemId = null;
        if (returnValue instanceof CartInfo cartInfo) {
            cartId = cartInfo.cartId();
            if (command != null) {
                cartItemId = findItemId(cartInfo, command);
            }
        }

        return CartEventData.builder()
            .cartId(cartId)
            .cartItemId(cartItemId)
            .productId(command != null ? command.productId() : null)
            .quantity(command != null ? command.quantity() : null)
            .build();
    }

    private UUID findItemId(CartInfo cartInfo, AddItemCommand command) {
        if (cartInfo.items() == null) {
            return null;
        }
        for (CartItemInfo item : cartInfo.items()) {
            if (item.productId().equals(command.productId())) {
                if (command.optionInfoJson() == null
                    || command.optionInfoJson().equals(item.optionInfoJson())) {
                    return item.itemId();
                }
            }
        }
        return null;
    }
}
