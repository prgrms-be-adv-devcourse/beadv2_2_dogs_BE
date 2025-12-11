package com.barofarm.buyer.inventory.application;

import static com.barofarm.buyer.inventory.exception.InventoryErrorCode.INVALID_REQUEST;
import static com.barofarm.buyer.inventory.exception.InventoryErrorCode.INVENTORY_NOT_FOUND;

import com.barofarm.buyer.common.exception.CustomException;
import com.barofarm.buyer.inventory.application.dto.request.InventoryDecreaseCommand;
import com.barofarm.buyer.inventory.domain.Inventory;
import com.barofarm.buyer.inventory.domain.InventoryRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional
    public void decreaseStock(InventoryDecreaseCommand command) {

        Set<UUID> productIds = command.items().stream()
            .map(InventoryDecreaseCommand.Item::productId)
            .collect(Collectors.toSet());

        if (productIds.isEmpty()) {
            throw new CustomException(INVALID_REQUEST);
        }

        List<Inventory> inventories = inventoryRepository.findByProductIdInForUpdate(productIds);

        Map<UUID, Inventory> inventoryMap = inventories.stream()
            .collect(Collectors.toMap(
                Inventory::getProductId,
                Function.identity()
            ));

        for (InventoryDecreaseCommand.Item item : command.items()) {
            Inventory inventory = inventoryMap.get(item.productId());
            if (inventory == null) {
                throw new CustomException(INVENTORY_NOT_FOUND);
            }
            inventory.decrease(item.quantity());
        }
    }
}
