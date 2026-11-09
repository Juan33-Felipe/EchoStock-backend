package com.echostock.audit;

import com.echostock.common.error.ApiException;
import com.echostock.common.error.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Sincronización offline (RF-006). Cada auditoría se registra en su propia transacción, así que
 * un elemento rechazado no impide aceptar los demás.
 */
@Service
public class AuditBatchService {

    static final String REASON_SKU_NOT_FOUND = "SKU no encontrado";

    private static final Logger log = LoggerFactory.getLogger(AuditBatchService.class);

    private final AuditService auditService;

    public AuditBatchService(AuditService auditService) {
        this.auditService = auditService;
    }

    public AuditBatchResponse registerAll(List<AuditRequest> requests) {
        List<String> accepted = new ArrayList<>();
        List<String> duplicates = new ArrayList<>();
        List<AuditBatchResponse.Rejection> rejected = new ArrayList<>();

        for (AuditRequest request : requests) {
            String clientId = request == null ? null : request.clientAuditId();
            try {
                AuditService.Registration registration = auditService.register(request);
                (registration.created() ? accepted : duplicates).add(clientId);
            } catch (NotFoundException ex) {
                rejected.add(new AuditBatchResponse.Rejection(clientId, REASON_SKU_NOT_FOUND));
            } catch (ApiException ex) {
                rejected.add(new AuditBatchResponse.Rejection(clientId, ex.getMessage()));
            } catch (RuntimeException ex) {
                log.error("Error inesperado registrando la auditoría {} del lote", clientId, ex);
                rejected.add(new AuditBatchResponse.Rejection(clientId, "Error interno al registrar la auditoría"));
            }
        }
        log.info("Lote de auditorías: {} aceptadas, {} duplicadas, {} rechazadas",
                accepted.size(), duplicates.size(), rejected.size());
        return new AuditBatchResponse(accepted, duplicates, rejected);
    }
}
