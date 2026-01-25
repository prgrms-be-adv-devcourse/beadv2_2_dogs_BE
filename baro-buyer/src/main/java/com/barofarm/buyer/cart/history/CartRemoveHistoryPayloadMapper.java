package com.barofarm.buyer.cart.history;

import com.barofarm.buyer.cart.domain.Cart;
import com.barofarm.buyer.cart.domain.CartItem;
import com.barofarm.buyer.cart.domain.CartRepository;
import com.barofarm.buyer.product.domain.ProductRepository;
import com.barofarm.log.history.mapper.HistoryPayloadMapper;
import com.barofarm.log.history.model.CartEventData;
import com.barofarm.log.history.model.HistoryEventType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CartRemoveHistoryPayloadMapper implements HistoryPayloadMapper {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartRemoveHistoryPayloadMapper(
        CartRepository cartRepository,
        ProductRepository productRepository
    ) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    @Override
    public HistoryEventType supports() {
        return HistoryEventType.CART_ITEM_REMOVED;
    }

    @Override
    public boolean mapBeforeProceed() {
        return true;
    }

    @Override
    public Object payload(Object[] args, Object returnValue) {
        final UUID buyerId = args != null && args.length >= 3 ? (UUID) args[0] : null;
        final String sessionKey = args != null && args.length >= 3 ? (String) args[1] : null;
        final UUID itemId = args != null && args.length >= 3 ? (UUID) args[2] : null;

        final CartEventData.CartEventDataBuilder builder =
            CartEventData.builder()
                .cartItemId(itemId);

        Optional<Cart> cart = resolveCart(buyerId, sessionKey);
        if (cart.isEmpty()) {
            return builder.build();
        }

        builder.cartId(cart.get().getId());

        Optional<CartItem> item = cart.get().getItems().stream()
            .filter(cartItem -> cartItem.getId().equals(itemId))
            .findFirst();
        if (item.isEmpty()) {
            return builder.build();
        }

        UUID productId = item.get().getProductId();
        builder.productId(productId);
        builder.quantity(item.get().getQuantity());
        productRepository.findById(productId)
            .ifPresent(product -> {
                builder.productName(product.getProductName());
                if (product.getCategory() != null) {
                    builder.categoryCode(product.getCategory().getCode());
                }
            });

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
