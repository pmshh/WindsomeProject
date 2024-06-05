package com.windsome.controller.order;

import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@Slf4j
@RequiredArgsConstructor
public class PaymentController {

    @Value("${iamportApiKey}")
    private String apiKey;

    @Value("${iamportSecretKey}")
    private String secretKey;

    @PostMapping("/payment/verify/{imp_uid}")
    @ResponseBody
    public IamportResponse<Payment> paymentByImpUid(@PathVariable(value= "imp_uid") String imp_uid) throws IamportResponseException, IOException {
        // 해당 메소드는 imp_uid를 검사하고, 데이터를 보내주게 되는데,
        // 이 데이터와 처음 금액이 일치하는지를 확인하여 결제 성공 실패 여부를 리턴
        IamportClient iamportClient = new IamportClient(apiKey, secretKey);
        return iamportClient.paymentByImpUid(imp_uid);
    }
}