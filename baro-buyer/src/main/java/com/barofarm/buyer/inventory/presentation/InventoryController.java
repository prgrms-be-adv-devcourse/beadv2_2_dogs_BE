package com.barofarm.buyer.inventory.presentation;

import com.barofarm.buyer.inventory.application.InventoryService;
import com.barofarm.buyer.inventory.presentation.dto.InventoryInfo;
import com.barofarm.dto.ResponseDto;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.v1}/inventories")
@RequiredArgsConstructor
public class InventoryController implements InventorySwaggerApi {

    private final InventoryService inventoryService;

    @Override
    @GetMapping(params = "productId")
    public ResponseDto<List<InventoryInfo>> getInventoriesByProductId(
        @RequestParam @NotNull UUID productId
    ) {
        List<InventoryInfo> response = inventoryService.getInventoryInfosByProductId(productId);
        return ResponseDto.ok(response);
    }
}
