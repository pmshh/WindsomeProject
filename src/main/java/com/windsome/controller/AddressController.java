package com.windsome.controller;

import com.windsome.service.member.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.persistence.EntityNotFoundException;

@Controller
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    /**
     * 배송지 삭제
     */
    @DeleteMapping("/address/delete")
    public ResponseEntity<String> deleteAddress(@RequestBody Long[] addressIds) {
        try {
            addressService.deleteAddresses(addressIds);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body("일치하는 배송지가 없습니다.");
        }
        return ResponseEntity.ok().body("배송지가 삭제되었습니다.");
    }
}
