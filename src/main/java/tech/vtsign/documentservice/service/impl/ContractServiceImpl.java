package tech.vtsign.documentservice.service.impl;

import com.google.common.primitives.Bytes;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import tech.vtsign.documentservice.domain.Contract;
import tech.vtsign.documentservice.domain.DigitalSignature;
import tech.vtsign.documentservice.domain.Document;
import tech.vtsign.documentservice.exception.BadRequestException;
import tech.vtsign.documentservice.exception.NotFoundException;
import tech.vtsign.documentservice.exception.UnauthorizedException;
import tech.vtsign.documentservice.repository.ContractRepository;
import tech.vtsign.documentservice.repository.DigitalSignatureRepository;
import tech.vtsign.documentservice.service.ContractService;
import tech.vtsign.documentservice.utils.DSUtil;
import tech.vtsign.documentservice.utils.FileUtil;
import tech.vtsign.documentservice.utils.KeyReaderUtil;

import java.security.PublicKey;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {
    private final ContractRepository contractRepository;
    private final DigitalSignatureRepository digitalSignatureRepository;

    @Override
    @SneakyThrows
    public List<Document> getDocumentsByContractAndReceiver(UUID contractId, UUID receiverId) {
        Optional<Contract> opt = contractRepository.findById(contractId);
        if (opt.isEmpty()) {
            throw new BadRequestException("Not found contract");
        } else {
            Contract contract = opt.get();
            List<Document> listDocument = contract.getDocuments();
            byte[] totalBytes = new byte[0];
            for (Document document : listDocument) {
                String url = document.getUrl();
                byte[] fileBytes = FileUtil.readByteFromURL(url);
                totalBytes = Bytes.concat(totalBytes, fileBytes);
            }


            Optional<DigitalSignature> optional = contract.getDigitalSignatures().stream()
                    .filter(digital -> digital.getUserUUID() == contract.getSenderUUID())
                    .findFirst();
            DigitalSignature digitalSignature = optional.orElseThrow(() -> new UnauthorizedException("Invalid sender"));

            byte[] dsBytes = FileUtil.readByteFromURL(digitalSignature.getUrl());
            byte[] publicKeyBytes = FileUtil.readByteFromURL(digitalSignature.getPublicKey());
            PublicKey publicKey = KeyReaderUtil.getPublicKey(publicKeyBytes);

            if (DSUtil.verify(totalBytes, dsBytes, publicKey)) {
                return contract.getDocuments();
            }

            throw new UnauthorizedException("Documents have been changed");
        }
    }

    @Override
    public Contract findContractById(UUID contractUUID) {
        Optional<Contract> contract = contractRepository.findById(contractUUID);
        return contract.orElseThrow(() -> new NotFoundException("Contract Not Found"));
    }

    @Override
    public List<Contract> findAllTemplateByUserId(UUID userUUID, String status) {
        List<DigitalSignature> digitalSignatures = digitalSignatureRepository.findByIdAndStatus(userUUID, status);
        return digitalSignatures.stream()
                .map(DigitalSignature::getContract)
                .collect(Collectors.toList());
    }

}
