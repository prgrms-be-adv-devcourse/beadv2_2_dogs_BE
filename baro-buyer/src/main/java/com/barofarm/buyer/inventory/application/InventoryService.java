package com.barofarm.buyer.inventory.application;

import static com.barofarm.buyer.inventory.exception.InventoryErrorCode.ALREADY_CANCELED;
import static com.barofarm.buyer.inventory.exception.InventoryErrorCode.INVENTORY_HAS_ACTIVE_RESERVATIONS;
import static com.barofarm.buyer.inventory.exception.InventoryErrorCode.INVENTORY_NOT_FOUND;
import static com.barofarm.buyer.inventory.exception.InventoryErrorCode.INVENTORY_RESERVATION_NOT_FOUND;

import com.barofarm.buyer.inventory.application.dto.request.InventoryCancelCommand;
import com.barofarm.buyer.inventory.application.dto.request.InventoryConfirmCommand;
import com.barofarm.buyer.inventory.application.dto.request.InventoryCreateCommand;
import com.barofarm.buyer.inventory.application.dto.request.InventoryReserveCommand;
import com.barofarm.buyer.inventory.domain.Inventory;
import com.barofarm.buyer.inventory.domain.InventoryRepository;
import com.barofarm.buyer.inventory.domain.InventoryReservation;
import com.barofarm.buyer.inventory.domain.InventoryReservationRepository;
import com.barofarm.buyer.inventory.domain.InventoryReservationStatus;
import com.barofarm.exception.CustomException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryReservationRepository inventoryReservationRepository;

    @Transactional
    public void reserveInventory(InventoryReserveCommand command) {

        try {
            List<InventoryReservation> reservations =
                inventoryReservationRepository.findAllByOrderId(command.orderId());
            boolean allReserved = !reservations.isEmpty()
                && reservations.stream()
                    .allMatch(r -> r.getInventoryReservationStatus() == InventoryReservationStatus.RESERVED);
            if (allReserved) {
                return;
            }

            for (InventoryReserveCommand.Item item : command.items()) {
                Inventory inventory = inventoryRepository.findById(item.inventoryId())
                    .orElseThrow(() -> new CustomException(INVENTORY_NOT_FOUND));

                inventory.markReserve(item.quantity());
                inventory.addInventoryReservation(
                    InventoryReservation.of(command.orderId(), item.quantity())
                );
            }
        } catch (DataIntegrityViolationException e) {
            List<InventoryReservation> existing =
                inventoryReservationRepository.findAllByOrderId(command.orderId());
            boolean allReserved = !existing.isEmpty()
                && existing.stream()
                    .allMatch(r -> r.getInventoryReservationStatus() == InventoryReservationStatus.RESERVED);
            if (allReserved) {
                return;
            }
            throw e;
        }
    }

    public void confirmInventory(InventoryConfirmCommand command) {
        List<InventoryReservation> reservations =
            inventoryReservationRepository.findAllByOrderId(command.orderId());

        if (reservations.isEmpty()) {
            throw new CustomException(INVENTORY_RESERVATION_NOT_FOUND);
        }

        boolean allConfirmed = reservations.stream()
            .allMatch(r -> r.getInventoryReservationStatus() == InventoryReservationStatus.CONFIRMED);
        if (allConfirmed) {
            return;
        }

        boolean anyCanceled = reservations.stream()
            .anyMatch(r -> r.getInventoryReservationStatus() == InventoryReservationStatus.CANCELED);
        if (anyCanceled) {
            throw new CustomException(ALREADY_CANCELED);
        }

        for (InventoryReservation inventoryReservation : reservations) {
            Inventory inventory = inventoryReservation.getInventory();
            Long requestedQuantity = inventoryReservation.getReservedQuantity();

            inventory.confirm(requestedQuantity);
            inventoryReservation.confirm();
        }
    }

    @Transactional
    public void cancelInventory(InventoryCancelCommand command) {

        List<InventoryReservation> reservations =
            inventoryReservationRepository.findAllByOrderId(command.orderId());

        if (reservations.isEmpty()) {
            return;
        }

        boolean allCanceled = reservations.stream()
            .allMatch(r -> r.getInventoryReservationStatus() == InventoryReservationStatus.CANCELED);
        if (allCanceled) {
            return;
        }

        for (InventoryReservation inventoryReservation : reservations) {
            Inventory inventory = inventoryReservation.getInventory();
            Long requestedQuantity = inventoryReservation.getReservedQuantity();

            inventory.markCancel(requestedQuantity);
            inventoryReservation.markCancel();
        }
    }

    @Transactional
    public UUID createInventory(InventoryCreateCommand command) {
        Inventory inventory = Inventory.of(
            command.productId(),
            command.quantity(),
            command.unit()
        );
        Inventory saved = inventoryRepository.save(inventory);
        return saved.getId();
    }

    @Transactional
    public void deleteInventory(UUID inventoryId) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
            .orElseThrow(() -> new CustomException(INVENTORY_NOT_FOUND));

        if (inventory.getReservedQuantity() > 0) {
            throw new CustomException(INVENTORY_HAS_ACTIVE_RESERVATIONS);
        }

        inventoryRepository.delete(inventory);
    }
}
