package com.barofarm.seller.farm;

import com.barofarm.seller.config.BaseControllerSupport;
import com.barofarm.seller.farm.application.dto.response.FarmCreateInfo;
import com.barofarm.seller.farm.application.dto.response.FarmDetailInfo;
import com.barofarm.seller.farm.application.dto.response.FarmUpdateInfo;
import com.barofarm.seller.farm.presentation.dto.FarmCreateRequestDto;
import com.barofarm.seller.farm.presentation.dto.FarmUpdateRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import static com.barofarm.seller.farm.domain.Status.ACTIVE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FarmControllerTest extends BaseControllerSupport {

    @Nested
    @DisplayName("농장 정보 등록")
    class CreateFarm {

        @Test
        @DisplayName("농장 정보를 정상적으로 등록한다")
        void success() throws Exception {
            // given
            UUID mockSellerId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
            UUID farmId = UUID.randomUUID();

            FarmCreateRequestDto request = new FarmCreateRequestDto(
                "테스트 농장",
                "테스트 설명",
                "서울시 송파구 잠실동",
                "010-1234-5678"
            );

            FarmCreateInfo response = new FarmCreateInfo(
                farmId,
                "테스트 농장",
                "테스트 설명",
                "서울시 송파구 잠실동",
                "010-1234-5678",
                ACTIVE,
                mockSellerId
            );

            given(farmService.createFarm(eq(mockSellerId), any()))
                .willReturn(ResponseEntity.status(HttpStatus.CREATED).body(response));

            // when & then
            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/farms")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(farmId.toString()))
                .andExpect(jsonPath("$.name").value("테스트 농장"))
                .andExpect(jsonPath("$.description").value("테스트 설명"))
                .andExpect(jsonPath("$.address").value("서울시 송파구 잠실동"))
                .andExpect(jsonPath("$.phone").value("010-1234-5678"))
                .andExpect(jsonPath("$.status").value(ACTIVE.toString()))
                .andExpect(jsonPath("$.sellerId").value("550e8400-e29b-41d4-a716-446655440000"));
        }

        @ParameterizedTest(name = "{index}: name={0}, description={1}, address={2}, phone={3}")
        @MethodSource("invalidFarmCreateRequests")
        @DisplayName("유효하지 않은 농장 생성 요청은 400 Bad Request를 반환한다")
        void fail_invalid_inputs(String name,
                                 String description,
                                 String address,
                                 String phone) throws Exception {
            // given
            FarmCreateRequestDto request = new FarmCreateRequestDto(
                name,
                description,
                address,
                phone
            );

            // when & then
            mockMvc.perform(post("/api/v1/farms")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        static Stream<Arguments> invalidFarmCreateRequests() {
            return Stream.of(
                // name null
                Arguments.of(null, "설명", "주소", "010-1234-5678"),
                // name blank
                Arguments.of("   ", "설명", "주소", "010-1234-5678"),

                // description null
                Arguments.of("농장", null, "주소", "010-1234-5678"),
                // description blank
                Arguments.of("농장", "   ", "주소", "010-1234-5678"),

                // address null
                Arguments.of("농장", "설명", null, "010-1234-5678"),
                // address blank
                Arguments.of("농장", "설명", "   ", "010-1234-5678"),

                // phone null
                Arguments.of("농장", "설명", "주소", null),
                // phone blank
                Arguments.of("농장", "설명", "주소", "   ")
            );
        }
    }

    @Nested
    @DisplayName("농장 정보 수정")
    class UpdateFarm {

        @Test
        @DisplayName("농장 정보를 정상적으로 수정한다")
        void success() throws Exception {
            // given
            UUID farmId = UUID.randomUUID();
            UUID mockSellerId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

            FarmUpdateRequestDto request = new FarmUpdateRequestDto(
                "수정된 농장",
                "수정된 설명",
                "경기도 성남시 분당구",
                "010-9999-8888"
            );

            FarmUpdateInfo response = new FarmUpdateInfo(
                farmId,
                "수정된 농장",
                "수정된 설명",
                "경기도 성남시 분당구",
                "010-9999-8888",
                ACTIVE,
                mockSellerId
            );

            given(farmService.updateFarm(eq(farmId), any()))
                .willReturn(ResponseEntity.ok(response));

            // when & then
            mockMvc.perform(put("/api/v1/farms/{id}", farmId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(farmId.toString()))
                .andExpect(jsonPath("$.name").value("수정된 농장"))
                .andExpect(jsonPath("$.description").value("수정된 설명"))
                .andExpect(jsonPath("$.address").value("경기도 성남시 분당구"))
                .andExpect(jsonPath("$.phone").value("010-9999-8888"))
                .andExpect(jsonPath("$.status").value(ACTIVE.toString()))
                .andExpect(jsonPath("$.sellerId").value("550e8400-e29b-41d4-a716-446655440000"));
        }

        @ParameterizedTest(name = "{index}: name={0}, description={1}, address={2}, phone={3}")
        @MethodSource("invalidFarmUpdateRequests")
        @DisplayName("유효하지 않은 농장 수정 요청은 400 Bad Request를 반환한다")
        void fail_invalid_inputs(String name,
                                 String description,
                                 String address,
                                 String phone) throws Exception {
            // given
            UUID farmId = UUID.randomUUID();

            FarmUpdateRequestDto request = new FarmUpdateRequestDto(
                name,
                description,
                address,
                phone
            );

            // when & then
            mockMvc.perform(put("/api/v1/farms/{id}", farmId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        static Stream<Arguments> invalidFarmUpdateRequests() {
            return Stream.of(
                // name null
                Arguments.of(null, "설명", "주소", "010-1234-5678"),
                // name blank
                Arguments.of("   ", "설명", "주소", "010-1234-5678"),

                // description null
                Arguments.of("농장", null, "주소", "010-1234-5678"),
                // description blank
                Arguments.of("농장", "   ", "주소", "010-1234-5678"),

                // address null
                Arguments.of("농장", "설명", null, "010-1234-5678"),
                // address blank
                Arguments.of("농장", "설명", "   ", "010-1234-5678"),

                // phone null
                Arguments.of("농장", "설명", "주소", null),
                // phone blank
                Arguments.of("농장", "설명", "주소", "   ")
            );
        }
    }

    @Nested
    @DisplayName("농장 정보 상세 조회")
    class FindFarm {

        @Test
        @DisplayName("농장 정보를 정상적으로 조회한다")
        void success() throws Exception {
            // given
            UUID farmId = UUID.randomUUID();
            UUID sellerId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

            FarmDetailInfo response = new FarmDetailInfo(
                farmId,
                "테스트 농장",
                "테스트 설명",
                "서울시 송파구 잠실동",
                "010-1234-5678",
                ACTIVE,
                sellerId
            );

            given(farmService.findFarm(eq(farmId)))
                .willReturn(ResponseEntity.ok(response));

            // when & then
            mockMvc.perform(get("/api/v1/farms/{id}", farmId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(farmId.toString()))
                .andExpect(jsonPath("$.name").value("테스트 농장"))
                .andExpect(jsonPath("$.description").value("테스트 설명"))
                .andExpect(jsonPath("$.address").value("서울시 송파구 잠실동"))
                .andExpect(jsonPath("$.phone").value("010-1234-5678"))
                .andExpect(jsonPath("$.status").value(ACTIVE.toString()))
                .andExpect(jsonPath("$.sellerId").value(sellerId.toString()));
        }
    }

    @Nested
    @DisplayName("농장 목록 조회")
    class FindFarmList {

        @Test
        @DisplayName("농장 목록을 페이지 단위로 조회한다")
        void success() throws Exception {
            // given
            UUID farmId1 = UUID.randomUUID();
            UUID farmId2 = UUID.randomUUID();
            UUID sellerId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

            List<FarmDetailInfo> response = List.of(
                new FarmDetailInfo(
                    farmId1,
                    "테스트 농장 1",
                    "테스트 설명 1",
                    "서울시 송파구 잠실동 1",
                    "010-1111-1111",
                    ACTIVE,
                    sellerId
                ),
                new FarmDetailInfo(
                    farmId2,
                    "테스트 농장 2",
                    "테스트 설명 2",
                    "서울시 송파구 잠실동 2",
                    "010-2222-2222",
                    ACTIVE,
                    sellerId
                )
            );

            given(farmService.findFarmList(any(Pageable.class)))
                .willReturn(ResponseEntity.ok(response));

            // when & then
            mockMvc.perform(get("/api/v1/farms")
                    .param("page", "0")
                    .param("size", "10")
                    .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(farmId1.toString()))
                .andExpect(jsonPath("$[0].name").value("테스트 농장 1"))
                .andExpect(jsonPath("$[0].description").value("테스트 설명 1"))
                .andExpect(jsonPath("$[0].address").value("서울시 송파구 잠실동 1"))
                .andExpect(jsonPath("$[0].phone").value("010-1111-1111"))
                .andExpect(jsonPath("$[0].status").value(ACTIVE.toString()))
                .andExpect(jsonPath("$[1].id").value(farmId2.toString()))
                .andExpect(jsonPath("$[1].name").value("테스트 농장 2"))
                .andExpect(jsonPath("$[1].description").value("테스트 설명 2"))
                .andExpect(jsonPath("$[1].address").value("서울시 송파구 잠실동 2"))
                .andExpect(jsonPath("$[1].phone").value("010-2222-2222"))
                .andExpect(jsonPath("$[1].status").value(ACTIVE.toString()));
        }
    }

    @Nested
    @DisplayName("농장 삭제")
    class DeleteFarm {

        @Test
        @DisplayName("농장을 정상적으로 삭제한다")
        void success() throws Exception {
            // given
            UUID farmId = UUID.randomUUID();

            // when & then
            mockMvc.perform(delete("/api/v1/farms/{id}", farmId))
                .andExpect(status().isNoContent());
        }
    }
}
