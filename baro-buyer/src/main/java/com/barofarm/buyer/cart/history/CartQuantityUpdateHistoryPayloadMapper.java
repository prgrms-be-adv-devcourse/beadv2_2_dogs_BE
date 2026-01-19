package com.barofarm.buyer.cart.history;

import com.barofarm.buyer.cart.domain.Cart;
import com.barofarm.buyer.cart.domain.CartItem;
import com.barofarm.buyer.cart.domain.CartRepository;
import com.barofarm.log.history.mapper.HistoryPayloadMapper;
import com.barofarm.log.history.model.CartEventData;
import com.barofarm.log.history.model.HistoryEventType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CartQuantityUpdateHistoryPayloadMapper implements HistoryPayloadMapper {

    private final CartRepository cartRepository;

    public CartQuantityUpdateHistoryPayloadMapper(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    @Override
    public HistoryEventType supports() {
        return HistoryEventType.CART_QUANTITY_UPDATED;
    }

    @Override
    public Object payload(Object[] args, Object returnValue) {
        final UUID buyerId = args != null && args.length >= 4 ? (UUID) args[0] : null;
        final String sessionKey = args != null && args.length >= 4 ? (String) args[1] : null;
        final UUID itemId = args != null && args.length >= 4 ? (UUID) args[2] : null;
        final Integer quantity = args != null && args.length >= 4 ? (Integer) args[3] : null;

        final CartEventData.CartEventDataBuilder builder =
            CartEventData.builder()
                .cartItemId(itemId)
                .quantity(quantity);

        Optional<Cart> cart = resolveCart(buyerId, sessionKey);
        if (cart.isEmpty()) {
            return builder.build();
        }

        builder.cartId(cart.get().getId());

        Optional<CartItem> item = cart.get().getItems().stream()
            .filter(cartItem -> cartItem.getId().equals(itemId))
            .findFirst();
        item.ifPresent(cartItem -> builder.productId(cartItem.getProductId()));

        return builder.build();
    }

    private Optional<Cart> resolveCart(UUID buyerId, String sessionKey) {
        if (buyerId != null) {
            return cartRepository.findByBuyerId(buyerId);
        }
        if (sessionKey != null) {
            return cartRepository.findBySessionKey(sessionKey);
        }
        return Optional.empty();
    }
}
