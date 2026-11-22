package com.hyundai.way.domain.invitation.repository;

import com.hyundai.way.domain.invitation.domain.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    Optional<Invitation> findByCodeLookupKey(String codeLookupKey);
}
