package com.barofarm.seller.farm.presentation.dto;

import com.barofarm.seller.farm.application.dto.request.FarmCreateCommand;
import jakarta.validation.constraints.NotBlank;

public record FarmCreateRequestDto(

    @NotBlank(message = "농장 이름은 필수입니다.")
    String name,

    @NotBlank(message = "농장 설명은 필수입니다.")
    String description,

    @NotBlank(message = "주소는 필수입니다.")
    String address,

    @NotBlank(message = "전화번호는 필수입니다.")
    String phone

) {
    public FarmCreateCommand toCommand() {
        return new FarmCreateCommand(
            name,
            description,
            address,
            phone
        );
    }
}
