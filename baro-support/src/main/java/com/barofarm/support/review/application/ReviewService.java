package com.barofarm.support.review.application;

import com.barofarm.support.review.application.dto.request.ReviewCreateCommand;
import com.barofarm.support.review.application.dto.response.ReviewDetailInfo;
import com.barofarm.support.review.domain.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private ReviewRepository reviewRepository;

    @Transactional
    public ReviewDetailInfo createReview(ReviewCreateCommand command) {
        //order_item의 상태가 구매가 맞는지 확인(COMPLETED)

        //주문자 buyer_id가 현재 로그인한 사용자의 id가 맞는지

        // 해당 역할이 admin은 아닌지(buyer와 seller모두 리뷰 달 수 있어야 함)


        //제품이 현재 판매중 or discounted가 맞는지

        //이미 해당 item에 리뷰를 작성했는지(중복 체크)

        //order_item이 product와 일치하는

        //별점 검증

        return null;
    }
}
