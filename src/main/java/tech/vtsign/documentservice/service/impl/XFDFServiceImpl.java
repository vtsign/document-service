package tech.vtsign.documentservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.vtsign.documentservice.domain.XFDF;
import tech.vtsign.documentservice.repository.XFDFRepository;
import tech.vtsign.documentservice.service.XFDFService;

@Service
@RequiredArgsConstructor
public class XFDFServiceImpl implements XFDFService {
    private final XFDFRepository xfdfRepository;

    @Override
    public XFDF save(XFDF xfdf) {
        return xfdfRepository.save(xfdf);
    }
}
