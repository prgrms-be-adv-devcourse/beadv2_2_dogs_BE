package com.barofarm.order.payment.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("${api.v1}/payments")
@RequiredArgsConstructor
public class PaymentViewController {

    @GetMapping("/success")
    public String paymentSuccess() {
        return "success";
    }

    @GetMapping("/fail")
    public String paymentFail() {
        return "fail";
    }
}
