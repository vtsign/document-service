package tech.vtsign.documentservice.service;

import tech.vtsign.documentservice.domain.XFDF;

import java.util.UUID;

public interface XFDFService {

    XFDF save(XFDF xfdf);

    void deleteAllByDocumentId(UUID documentId);
}
