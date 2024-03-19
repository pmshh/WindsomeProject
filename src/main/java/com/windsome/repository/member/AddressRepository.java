package com.windsome.repository.member;

import com.windsome.entity.member.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {

    Address findByMemberIdAndIsDefault(Long memberId, boolean isDefaultShippingAddress);

    List<Address> findAllByMemberIdOrderByIsDefaultDesc(Long memberId);
}
