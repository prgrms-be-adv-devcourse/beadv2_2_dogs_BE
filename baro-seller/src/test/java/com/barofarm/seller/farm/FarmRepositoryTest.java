package com.barofarm.seller.farm;

import com.barofarm.seller.config.BaseRepositoryTest;
import com.barofarm.seller.farm.domain.Farm;
import com.barofarm.seller.farm.infrastructure.FarmRepositoryAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

@Import(FarmRepositoryAdapter.class)
@DataJpaTest
class FarmRepositoryTest extends BaseRepositoryTest {

    @Nested
    @DisplayName("save 메서드")
    class Save {

        @Test
        @DisplayName("농장 정보를 저장한다.")
        void success() {
            // given
            Farm farm = saveFarm();

            // when
            Optional<Farm> found = farmRepository.findById(farm.getId());

            // then
            assertThat(found.get().getId()).isEqualTo(farm.getId());
            assertThat(found.get().getName()).isEqualTo(farm.getName());
            assertThat(found.get().getDescription()).isEqualTo(farm.getDescription());
            assertThat(found.get().getAddress()).isEqualTo(farm.getAddress());
            assertThat(found.get().getPhone()).isEqualTo(farm.getPhone());
            assertThat(found.get().getStatus()).isEqualTo(farm.getStatus());
        }
    }

    @Nested
    @DisplayName("findById 메서드")
    class FindById {

        @Test
        @DisplayName("ID로 농장을 조회한다.")
        void success() {
            // given
            Farm farm = saveFarm();

            // when
            Optional<Farm> found = farmRepository.findById(farm.getId());

            // then
            assertThat(found.get().getId()).isEqualTo(farm.getId());
            assertThat(found.get().getName()).isEqualTo(farm.getName());
            assertThat(found.get().getDescription()).isEqualTo(farm.getDescription());
            assertThat(found.get().getAddress()).isEqualTo(farm.getAddress());
            assertThat(found.get().getPhone()).isEqualTo(farm.getPhone());
            assertThat(found.get().getStatus()).isEqualTo(farm.getStatus());
        }

        @Test
        @DisplayName("존재하지 않는 ID 조회 시 빈 Optional을 반환한다.")
        void fail_not_found() {
            // given
            UUID randomId = UUID.randomUUID();

            // when
            Optional<Farm> found = farmRepository.findById(randomId);

            // then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("deleteById 메서드")
    class DeleteById {

        @Test
        @DisplayName("ID로 농장을 삭제한다.")
        void success() {
            // given
            Farm farm = saveFarm();

            // when
            farmRepository.deleteById(farm.getId());
            Optional<Farm> found = farmRepository.findById(farm.getId());

            // then
            assertThat(found).isNotPresent();
        }
    }

    @Nested
    @DisplayName("findAll 메서드")
    class FindAll {

        @Test
        @DisplayName("페이징 조회로 농장 목록을 반환한다.")
        void success() {
            // given
            saveFarm();
            saveFarm();
            saveFarm();

            Pageable pageable = Pageable.ofSize(2);

            // when
            Page<Farm> page = farmRepository.findAll(pageable);

            // then
            assertThat(page.getContent()).hasSize(2);
            assertThat(page.getTotalElements()).isEqualTo(3);
            assertThat(page.getTotalPages()).isEqualTo(2);
        }
    }

    private Farm saveFarm() {
        Farm farm = Farm.of(
            "테스트 농장",
            "테스트 설명",
            "서울시 송파구 잠실동",
            "010-1234-5678",
            null
        );
        return farmRepository.save(farm);
    }
}
