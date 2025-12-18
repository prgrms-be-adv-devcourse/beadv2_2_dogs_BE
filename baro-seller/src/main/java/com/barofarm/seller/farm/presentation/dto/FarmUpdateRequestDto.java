package com.barofarm.seller.farm.presentation.dto;

import com.barofarm.seller.farm.application.dto.request.FarmUpdateCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record FarmUpdateRequestDto(

    @NotBlank(message = "농장 이름은 필수입니다.")
    String name,

    @NotBlank(message = "농장 설명은 필수입니다.")
    String description,

    @NotBlank(message = "주소는 필수입니다.")
    String address,

    @NotBlank(message = "전화번호는 필수입니다.")
    String phone,

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @NotBlank(message = "이메일은 필수입니다.")
    String email,

    @NotNull(message = "설립년도는 필수입니다.")
    @Positive(message = "설립년도는 양수여야 합니다.")
    Integer establishedYear,

    @NotBlank(message = "농장 규모는 필수입니다.")
    String farmSize,

    @NotBlank(message = "재배 방식은 필수입니다.")
    String cultivationMethod

) {
    public FarmUpdateCommand toCommand() {
        return new FarmUpdateCommand(
            name,
            description,
            address,
            phone,
            email,
            establishedYear,
            farmSize,
            cultivationMethod
        );
    }
}
