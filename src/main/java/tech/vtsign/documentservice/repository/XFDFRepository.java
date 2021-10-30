package tech.vtsign.documentservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.vtsign.documentservice.domain.XFDF;

import java.util.UUID;

public interface XFDFRepository extends JpaRepository<XFDF, UUID> {
}
