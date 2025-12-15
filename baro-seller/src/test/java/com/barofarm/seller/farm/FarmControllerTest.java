package com.barofarm.seller.farm;



//class FarmControllerTest extends BaseControllerSupport {
//
//    @Nested
//    @DisplayName("농장 정보 등록")
//    class CreateFarm {
//
//        @Test
//        @DisplayName("농장 정보를 정상적으로 등록한다")
//        void success() throws Exception {
//            // given
//            UUID mockSellerId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
//            UUID farmId = UUID.randomUUID();
//
//            FarmCreateRequestDto request = new FarmCreateRequestDto(
//                "테스트 농장",
//                "테스트 설명",
//                "서울시 송파구 잠실동",
//                "010-1234-5678"
//            );
//
//            FarmCreateInfo response = new FarmCreateInfo(
//                farmId,
//                "테스트 농장",
//                "테스트 설명",
//                "서울시 송파구 잠실동",
//                "010-1234-5678",
//                ACTIVE,
//                mockSellerId
//            );
//
//            given(farmService.createFarm(eq(mockSellerId), any()))
//                .willReturn(ResponseDto.ok(response));
//
//            // when & then
//            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/farms")
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .content(objectMapper.writeValueAsString(request)))
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.status").value(200))
//                .andExpect(jsonPath("$.data.id").value(farmId.toString()))
//                .andExpect(jsonPath("$.data.name").value("테스트 농장"))
//                .andExpect(jsonPath("$.data.description").value("테스트 설명"))
//                .andExpect(jsonPath("$.data.address").value("서울시 송파구 잠실동"))
//                .andExpect(jsonPath("$.data.phone").value("010-1234-5678"))
//                .andExpect(jsonPath("$.data.status").value(ACTIVE.toString()))
//                .andExpect(jsonPath("$.data.sellerId").value("550e8400-e29b-41d4-a716-446655440000"));
//        }
//
//        @ParameterizedTest(name = "{index}: name={0}, description={1}, address={2}, phone={3}")
//        @MethodSource("invalidFarmCreateRequests")
//        @DisplayName("유효하지 않은 농장 생성 요청은 400 Bad Request를 반환한다")
//        void failInvalidInputs(String name,
//                                 String description,
//                                 String address,
//                                 String phone) throws Exception {
//            // given
//            FarmCreateRequestDto request = new FarmCreateRequestDto(
//                name,
//                description,
//                address,
//                phone
//            );
//
//            // when & then
//            mockMvc.perform(post("/api/v1/farms")
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isBadRequest());
//        }
//
//        static Stream<Arguments> invalidFarmCreateRequests() {
//            return Stream.of(
//                // name null
//                Arguments.of(null, "설명", "주소", "010-1234-5678"),
//                // name blank
//                Arguments.of("   ", "설명", "주소", "010-1234-5678"),
//
//                // description null
//                Arguments.of("농장", null, "주소", "010-1234-5678"),
//                // description blank
//                Arguments.of("농장", "   ", "주소", "010-1234-5678"),
//
//                // address null
//                Arguments.of("농장", "설명", null, "010-1234-5678"),
//                // address blank
//                Arguments.of("농장", "설명", "   ", "010-1234-5678"),
//
//                // phone null
//                Arguments.of("농장", "설명", "주소", null),
//                // phone blank
//                Arguments.of("농장", "설명", "주소", "   ")
//            );
//        }
//    }
//
//    @Nested
//    @DisplayName("농장 정보 수정")
//    class UpdateFarm {
//
//        @Test
//        @DisplayName("농장 정보를 정상적으로 수정한다")
//        void success() throws Exception {
//            // given
//            UUID farmId = UUID.randomUUID();
//            UUID mockSellerId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
//
//            FarmUpdateRequestDto request = new FarmUpdateRequestDto(
//                "수정된 농장",
//                "수정된 설명",
//                "경기도 성남시 분당구",
//                "010-9999-8888"
//            );
//
//            FarmUpdateInfo response = new FarmUpdateInfo(
//                farmId,
//                "수정된 농장",
//                "수정된 설명",
//                "경기도 성남시 분당구",
//                "010-9999-8888",
//                ACTIVE,
//                mockSellerId
//            );
//
//            given(farmService.updateFarm(any(UUID.class), eq(farmId), any()))
//                .willReturn(ResponseDto.ok(response));
//
//            // when & then
//            mockMvc.perform(put("/api/v1/farms/{id}", farmId)
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .content(objectMapper.writeValueAsString(request)))
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.status").value(200))
//                .andExpect(jsonPath("$.data.id").value(farmId.toString()))
//                .andExpect(jsonPath("$.data.name").value("수정된 농장"))
//                .andExpect(jsonPath("$.data.description").value("수정된 설명"))
//                .andExpect(jsonPath("$.data.address").value("경기도 성남시 분당구"))
//                .andExpect(jsonPath("$.data.phone").value("010-9999-8888"))
//                .andExpect(jsonPath("$.data.status").value(ACTIVE.toString()))
//                .andExpect(jsonPath("$.data.sellerId").value("550e8400-e29b-41d4-a716-446655440000"));
//        }
//
//        @ParameterizedTest(name = "{index}: name={0}, description={1}, address={2}, phone={3}")
//        @MethodSource("invalidFarmUpdateRequests")
//        @DisplayName("유효하지 않은 농장 수정 요청은 400 Bad Request를 반환한다")
//        void failInvalidInputs(String name,
//                                 String description,
//                                 String address,
//                                 String phone) throws Exception {
//            // given
//            UUID farmId = UUID.randomUUID();
//
//            FarmUpdateRequestDto request = new FarmUpdateRequestDto(
//                name,
//                description,
//                address,
//                phone
//            );
//
//            // when & then
//            mockMvc.perform(put("/api/v1/farms/{id}", farmId)
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isBadRequest());
//        }
//
//        static Stream<Arguments> invalidFarmUpdateRequests() {
//            return Stream.of(
//                // name null
//                Arguments.of(null, "설명", "주소", "010-1234-5678"),
//                // name blank
//                Arguments.of("   ", "설명", "주소", "010-1234-5678"),
//
//                // description null
//                Arguments.of("농장", null, "주소", "010-1234-5678"),
//                // description blank
//                Arguments.of("농장", "   ", "주소", "010-1234-5678"),
//
//                // address null
//                Arguments.of("농장", "설명", null, "010-1234-5678"),
//                // address blank
//                Arguments.of("농장", "설명", "   ", "010-1234-5678"),
//
//                // phone null
//                Arguments.of("농장", "설명", "주소", null),
//                // phone blank
//                Arguments.of("농장", "설명", "주소", "   ")
//            );
//        }
//    }
//
//    @Nested
//    @DisplayName("농장 정보 상세 조회")
//    class FindFarm {
//
//        @Test
//        @DisplayName("농장 정보를 정상적으로 조회한다")
//        void success() throws Exception {
//            // given
//            UUID farmId = UUID.randomUUID();
//            UUID sellerId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
//
//            FarmDetailInfo response = new FarmDetailInfo(
//                farmId,
//                "테스트 농장",
//                "테스트 설명",
//                "서울시 송파구 잠실동",
//                "010-1234-5678",
//                ACTIVE,
//                sellerId
//            );
//
//            given(farmService.findFarm(eq(farmId)))
//                .willReturn(ResponseDto.ok(response));
//
//            // when & then
//            mockMvc.perform(get("/api/v1/farms/{id}", farmId))
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.status").value(200))
//                .andExpect(jsonPath("$.data.id").value(farmId.toString()))
//                .andExpect(jsonPath("$.data.name").value("테스트 농장"))
//                .andExpect(jsonPath("$.data.description").value("테스트 설명"))
//                .andExpect(jsonPath("$.data.address").value("서울시 송파구 잠실동"))
//                .andExpect(jsonPath("$.data.phone").value("010-1234-5678"))
//                .andExpect(jsonPath("$.data.status").value(ACTIVE.toString()))
//                .andExpect(jsonPath("$.data.sellerId").value(sellerId.toString()));
//        }
//    }
//
//    @Nested
//    @DisplayName("농장 목록 조회")
//    class FindFarmList {
//
//        @Test
//        @DisplayName("농장 목록을 페이지 단위로 조회한다")
//        void success() throws Exception {
//            // given
//            UUID farmId1 = UUID.randomUUID();
//            UUID farmId2 = UUID.randomUUID();
//            UUID sellerId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
//
//            List<FarmDetailInfo> content = List.of(
//                new FarmDetailInfo(
//                    farmId1,
//                    "테스트 농장 1",
//                    "테스트 설명 1",
//                    "서울시 송파구 잠실동 1",
//                    "010-1111-1111",
//                    ACTIVE,
//                    sellerId
//                ),
//                new FarmDetailInfo(
//                    farmId2,
//                    "테스트 농장 2",
//                    "테스트 설명 2",
//                    "서울시 송파구 잠실동 2",
//                    "010-2222-2222",
//                    ACTIVE,
//                    sellerId
//                )
//            );
//
//            CustomPage<FarmDetailInfo> customPage = new CustomPage<>(
//                content,
//                0,
//                10,
//                2,
//                1,
//                true,
//                true,
//                false,
//                false
//            );
//
//            given(farmService.findFarmList(any(Pageable.class)))
//                .willReturn(ResponseDto.ok(customPage));
//
//            // when & then
//            mockMvc.perform(get("/api/v1/farms")
//                    .param("page", "0")
//                    .param("size", "10")
//                    .param("sort", "name,asc"))
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.data.content.length()").value(2))
//                .andExpect(jsonPath("$.data.content[0].id").value(farmId1.toString()))
//                .andExpect(jsonPath("$.data.content[0].name").value("테스트 농장 1"))
//                .andExpect(jsonPath("$.data.content[0].description").value("테스트 설명 1"))
//                .andExpect(jsonPath("$.data.content[0].address").value("서울시 송파구 잠실동 1"))
//                .andExpect(jsonPath("$.data.content[0].phone").value("010-1111-1111"))
//                .andExpect(jsonPath("$.data.content[0].status").value(ACTIVE.toString()))
//                .andExpect(jsonPath("$.data.content[0].sellerId").value(sellerId.toString()))
//                .andExpect(jsonPath("$.data.content[1].id").value(farmId2.toString()))
//                .andExpect(jsonPath("$.data.content[1].name").value("테스트 농장 2"))
//                .andExpect(jsonPath("$.data.content[1].description").value("테스트 설명 2"))
//                .andExpect(jsonPath("$.data.content[1].address").value("서울시 송파구 잠실동 2"))
//                .andExpect(jsonPath("$.data.content[1].phone").value("010-2222-2222"))
//                .andExpect(jsonPath("$.data.content[1].status").value(ACTIVE.toString()))
//                .andExpect(jsonPath("$.data.content[1].sellerId").value(sellerId.toString()));
//        }
//    }
//
//    @Nested
//    @DisplayName("농장 삭제")
//    class DeleteFarm {
//
//        @Test
//        @DisplayName("농장을 정상적으로 삭제한다")
//        void success() throws Exception {
//            // given
//            UUID farmId = UUID.randomUUID();
//
//            given(farmService.deleteFarm(any(UUID.class), eq(farmId)))
//                .willReturn(ResponseDto.ok(null));
//
//            // when & then
//            mockMvc.perform(delete("/api/v1/farms/{id}", farmId))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value(200))
//                .andExpect(jsonPath("$.data").doesNotExist())
//                .andExpect(jsonPath("$.message").doesNotExist());
//        }
//    }
//}
