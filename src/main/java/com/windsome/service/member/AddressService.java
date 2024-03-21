package com.windsome.service.member;

import com.windsome.entity.member.Address;
import com.windsome.repository.member.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AddressService {

    private final AddressRepository addressRepository;

    /**
     * 배송지 삭제
     */
    public void deleteAddresses(Long[] addressIds) {
        for (Long addressId : addressIds) {
            Address address = addressRepository.findById(addressId).orElseThrow(EntityNotFoundException::new);
            addressRepository.delete(address);
        }
    }

    /**
     * 회원의 기본 배송지 조회
     */
    public Address getAddressByMemberIdAndIsDefault(Long memberId, boolean isDefault) {
        return addressRepository.findByMemberIdAndIsDefault(memberId, isDefault).orElseThrow(EntityNotFoundException::new);
    }

    /**
     * 회원의 배송지 목록 조회
     */
    public List<Address> getAllAddressByMemberId(Long memberId) {
        return addressRepository.findAllByMemberIdOrderByIsDefaultDesc(memberId);
    }

    /**
     * 배송지 저장
     */
    public void saveAddress(Address address) {
        addressRepository.save(address);
    }
}
