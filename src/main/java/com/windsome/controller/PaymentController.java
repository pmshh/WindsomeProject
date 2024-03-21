package com.windsome.controller;

import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@Slf4j
@RequiredArgsConstructor
public class PaymentController {

    @PostMapping("/payment/verify/{imp_uid}")
    @ResponseBody
    public IamportResponse<Payment> paymentByImpUid(@PathVariable(value= "imp_uid") String imp_uid) throws IamportResponseException, IOException {
        IamportClient iamportClient = new IamportClient("4013418525116234", "FSeTbffI9qwh9hU4E5VZvkcePsp3j1FzLldMm6otxjX31neaEbB8PMj9hwsR183ItUhqc5ECapBuZkpK");
        return iamportClient.paymentByImpUid(imp_uid);
    }
}
