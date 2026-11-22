package com.hyundai.way.domain.invitation.repository;

import com.hyundai.way.domain.invitation.domain.Invitation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Invitation i where i.codeLookupKey = :codeLookupKey")
    Optional<Invitation> findByCodeLookupKeyForUpdate(@Param("codeLookupKey") String codeLookupKey);
}
