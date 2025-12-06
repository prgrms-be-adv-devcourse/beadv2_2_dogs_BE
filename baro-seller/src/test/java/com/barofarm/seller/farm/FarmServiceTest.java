package com.barofarm.seller.farm;

import com.barofarm.seller.config.BaseServiceTest;
import com.barofarm.seller.farm.application.dto.request.FarmCreateCommand;
import com.barofarm.seller.farm.application.dto.request.FarmUpdateCommand;
import com.barofarm.seller.farm.application.dto.response.FarmCreateInfo;
import com.barofarm.seller.farm.application.dto.response.FarmDetailInfo;
import com.barofarm.seller.farm.application.dto.response.FarmUpdateInfo;
import com.barofarm.seller.farm.domain.Farm;
import com.barofarm.seller.farm.exception.FarmException;
import com.barofarm.seller.seller.domain.Seller;
import com.barofarm.seller.seller.exception.SellerException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.UUID;
import static com.barofarm.seller.farm.exception.FarmErrorCode.FARM_NOT_FOUND;
import static com.barofarm.seller.seller.exception.SellerErrorCode.SELLER_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class FarmServiceTest extends BaseServiceTest {

    @Nested
    @DisplayName("createFarm 메서드")
    class CreateFarm {

        @Test
        @DisplayName("정상적으로 농장을 생성하고 201 응답을 반환한다")
        void success() {
            // given
            Seller seller = saveSeller();

            FarmCreateCommand command = new FarmCreateCommand(
                "테스트 농장",
                "테스트 설명",
                "서울시 송파구 잠실동",
                "010-1234-5678"
            );

            // when
            ResponseEntity<FarmCreateInfo> response = farmService.createFarm(seller.getId(), command);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody().name()).isEqualTo("테스트 농장");
            assertThat(response.getBody().description()).isEqualTo("테스트 설명");
            assertThat(response.getBody().address()).isEqualTo("서울시 송파구 잠실동");
            assertThat(response.getBody().phone()).isEqualTo("010-1234-5678");
            assertThat(response.getBody().sellerId()).isEqualTo(seller.getId());
        }

        @Test
        @DisplayName("존재하지 않는 판매자 ID로 요청하면 SellerException을 발생시킨다")
        void fail_seller_not_found() {
            // given
            UUID notExistSellerId = UUID.randomUUID();

            FarmCreateCommand command = new FarmCreateCommand(
                "테스트 농장",
                "테스트 설명",
                "서울시 송파구 잠실동",
                "010-1234-5678"
            );

            // when & then
            assertThatThrownBy(() -> farmService.createFarm(notExistSellerId, command))
                .isInstanceOf(SellerException.class)
                .hasMessage(SELLER_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("updateFarm 메서드")
    class UpdateFarm {

        @Test
        @DisplayName("정상적으로 농장을 수정하고 200 응답을 반환한다")
        void success() {
            // given
            Seller seller = saveSeller();
            Farm farm = saveFarm(seller);

            FarmUpdateCommand command = new FarmUpdateCommand(
                "테스트 농장",
                "테스트 설명",
                "서울시 송파구 잠실동",
                "010-1234-5678"
            );

            // when
            ResponseEntity<FarmUpdateInfo> response = farmService.updateFarm(farm.getId(), command);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().name()).isEqualTo("테스트 농장");
            assertThat(response.getBody().description()).isEqualTo("테스트 설명");
            assertThat(response.getBody().address()).isEqualTo("서울시 송파구 잠실동");
            assertThat(response.getBody().phone()).isEqualTo("010-1234-5678");
            assertThat(response.getBody().id()).isEqualTo(farm.getId());
        }

        @Test
        @DisplayName("존재하지 않는 농장 ID로 요청하면 FarmException을 발생시킨다")
        void fail_farm_not_found() {
            // given
            UUID notExistFarmId = UUID.randomUUID();

            FarmUpdateCommand farmUpdateCommand = new FarmUpdateCommand(
                "테스트 농장",
                "테스트 설명",
                "서울시 송파구 잠실동",
                "010-1234-5678"
            );

            // when & then
            assertThatThrownBy(() -> farmService.updateFarm(notExistFarmId, farmUpdateCommand))
                .isInstanceOf(FarmException.class)
                .hasMessage(FARM_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("findFarm 메서드")
    class FindFarm {

        @Test
        @DisplayName("정상적으로 농장을 조회하고 200 응답을 반환한다")
        void success() {
            // given
            Seller seller = saveSeller();
            Farm farm = saveFarm(seller);

            // when
            ResponseEntity<FarmDetailInfo> response = farmService.findFarm(farm.getId());

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().name()).isEqualTo("테스트 농장");
            assertThat(response.getBody().description()).isEqualTo("테스트 설명");
            assertThat(response.getBody().address()).isEqualTo("서울시 송파구 잠실동");
            assertThat(response.getBody().phone()).isEqualTo("010-1234-5678");
            assertThat(response.getBody().id()).isEqualTo(farm.getId());
        }

        @Test
        @DisplayName("존재하지 않는 농장 ID로 요청하면 FarmException을 발생시킨다")
        void fail_farm_not_found() {
            // given
            UUID notExistFarmId = UUID.randomUUID();

            // when & then
            assertThatThrownBy(() -> farmService.findFarm(notExistFarmId))
                .isInstanceOf(FarmException.class)
                .hasMessage(FARM_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("deleteFarm 메서드")
    class DeleteFarm {

        @Test
        @DisplayName("정상적으로 농장을 삭제하고 204 응답을 반환한다")
        void success() {
            // given
            Seller seller = saveSeller();
            Farm farm = saveFarm(seller);

            ResponseEntity<Void> response = farmService.deleteFarm(farm.getId());

            // when & then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(farmRepository.findById(farm.getId())).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 농장 ID로 요청하면 FarmException을 발생시킨다")
        void fail_farm_not_found() {
            // given
            UUID notExistFarmId = UUID.randomUUID();

            // when & then
            assertThatThrownBy(() -> farmService.findFarm(notExistFarmId))
                .isInstanceOf(FarmException.class)
                .hasMessage(FARM_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("findFarmList 메서드")
    class FindFarmList {

        @Test
        @DisplayName("정상적으로 농장 리스트를 조회하고 200 응답을 반환한다")
        void success() {
            // given
            Seller seller = saveSeller();
            Farm farm1 = saveFarm(seller);
            Farm farm2 = saveFarm(seller);
            Farm farm3 = saveFarm(seller);
            Farm farm4 = saveFarm(seller);

            Pageable pageable = PageRequest.of(0, 2);
            // when
            ResponseEntity<List<FarmDetailInfo>> response = farmService.findFarmList(pageable);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().size()).isEqualTo(2);
        }
    }

    private Farm saveFarm(Seller seller) {
        Farm farm = Farm.of(
            "테스트 농장",
            "테스트 설명",
            "서울시 송파구 잠실동",
            "010-1234-5678",
            seller
        );
        return farmRepository.save(farm);
    }

    private Seller saveSeller(){
        Seller seller = Seller.of(
            "철수네 과일가게",
            "123-45-67890",
            "이철수",
            "KB국민은행",
            "123456-78-910111"
        );

        return sellerRepository.save(seller);
    }
}
