/*
 * @ {#} InstrumentServiceImpl.java   1.0     12/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.services.impl;

import fit.instrument_service.dtos.request.ChangeInstrumentModeRequest;
import fit.instrument_service.dtos.request.InstallReagentRequest;
import fit.instrument_service.dtos.request.ModifyReagentStatusRequest;
import fit.instrument_service.dtos.response.InstrumentReagentResponse;
import fit.instrument_service.dtos.response.InstrumentResponse;
import fit.instrument_service.embedded.Vendor;
import fit.instrument_service.entities.Instrument;
import fit.instrument_service.entities.InstrumentModeLog;
import fit.instrument_service.entities.InstrumentReagent;
import fit.instrument_service.enums.AuditAction;
import fit.instrument_service.enums.InstrumentMode;
import fit.instrument_service.enums.InstrumentStatus;
import fit.instrument_service.enums.ReagentStatus;
import fit.instrument_service.events.InstrumentActivatedEvent;
import fit.instrument_service.events.InstrumentDeactivatedEvent;
import fit.instrument_service.exceptions.NotFoundException;
import fit.instrument_service.mappers.InstrumentMapper;
import fit.instrument_service.repositories.InstrumentModeLogRepository;
import fit.instrument_service.repositories.InstrumentReagentRepository;
import fit.instrument_service.repositories.InstrumentRepository;
import fit.instrument_service.services.AuditLogService;
import fit.instrument_service.services.InstrumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/*
 * @description: Implementation of InstrumentService for managing Instruments.
 * @author: Tran Hien Vinh
 * @date:   12/11/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InstrumentServiceImpl implements InstrumentService {
    private final InstrumentRepository instrumentRepository;

    private final InstrumentModeLogRepository instrumentModeLogRepository;

    private final InstrumentMapper instrumentMapper;

    private final InstrumentReagentRepository instrumentReagentRepository;

    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public InstrumentResponse changeInstrumentMode(String instrumentId, ChangeInstrumentModeRequest request) {
        // Tìm Instrument theo ID
        Instrument instrument = instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new NotFoundException("Instrument not found with id: " + instrumentId));

        // Lấy thông tin từ request
        InstrumentMode newMode = InstrumentMode.valueOf(request.getNewMode());
        String reason = request.getReason();
        InstrumentMode previousMode = instrument.getMode();

        // Phải cung cấp lý do khi chuyển sang Maintenance hoặc Inactive
        if ((newMode == InstrumentMode.MAINTENANCE || newMode == InstrumentMode.INACTIVE) && !StringUtils.hasText(reason)) {
            throw new IllegalArgumentException("Reason is required when changing mode to " + newMode);
        }

        // Ghi log
        InstrumentModeLog logEntry = new InstrumentModeLog();
        logEntry.setInstrumentId(instrument.getId());
        logEntry.setPreviousMode(previousMode);
        logEntry.setNewMode(newMode);
        logEntry.setReason(reason);

        instrumentModeLogRepository.save(logEntry);

        // Cập nhật Instrument
        instrument.setMode(newMode);
        instrument.setLastModeChangeReason(reason);
        Instrument updatedInstrument = instrumentRepository.save(instrument);

        // Trả về Response
        return instrumentMapper.toResponse(updatedInstrument);
    }

    @Override
    @Transactional
    public void handleInstrumentActivation(InstrumentActivatedEvent event) {
        // Tìm xem instrument có tồn tại không
        Optional<Instrument> instrumentOpt = instrumentRepository.findById(event.getId());

        Instrument instrument;

        if (instrumentOpt.isPresent()) {
            log.info("Handling Re-Activation event for existing instrument id: {}", event.getId());
            instrument = instrumentOpt.get();

            // Cập nhật lại thông tin
            instrument.setName(event.getName());
            instrument.setModel(event.getModel());
            instrument.setType(event.getType());
            instrument.setSerialNumber(event.getSerialNumber());
            instrument.setVendor(new Vendor(
                    event.getVendorId(),
                    event.getVendorName(),
                    event.getVendorContact()
            ));

            // Reset trạng thái từ (INACTIVE + ERROR) về (INACTIVE + AVAILABLE)
            instrument.setMode(InstrumentMode.INACTIVE);
            instrument.setStatus(InstrumentStatus.AVAILABLE);

            // Ghi log cho việc "Re-activate"
            InstrumentModeLog logEntry = new InstrumentModeLog();
            logEntry.setInstrumentId(instrument.getId());
            logEntry.setPreviousMode(InstrumentMode.INACTIVE);
            logEntry.setNewMode(InstrumentMode.INACTIVE);
            logEntry.setReason("Re-activated from Warehouse.");
            instrumentModeLogRepository.save(logEntry);

        } else {
            log.info("Handling new InstrumentActivatedEvent for id: {}", event.getId());
            instrument = new Instrument();
            instrument.setId(event.getId()); // id từ event

            // Map thông tin
            instrument.setName(event.getName());
            instrument.setModel(event.getModel());
            instrument.setType(event.getType());
            instrument.setSerialNumber(event.getSerialNumber());
            instrument.setVendor(new Vendor(
                    event.getVendorId(),
                    event.getVendorName(),
                    event.getVendorContact()
            ));

            // Set trạng thái ban đầu
            instrument.setMode(InstrumentMode.INACTIVE);
            instrument.setStatus(InstrumentStatus.AVAILABLE);
        }

        // Lưu
        instrumentRepository.save(instrument);
        log.info("Successfully upserted instrument record with id: {}", instrument.getId());
    }

    @Override
    public void handleInstrumentDeactivated(InstrumentDeactivatedEvent event) {
        String instrumentId = event.getId();

        // Tìm instrument
        Optional<Instrument> instrumentOpt = instrumentRepository.findById(instrumentId);

        // Nếu không tìm thấy, ghi log và ném ngoại lệ
        if (instrumentOpt.isEmpty()) {
            log.warn("Received InstrumentDeactivatedEvent for unknown instrument id: {}. Skipping.", instrumentId);
            throw new NotFoundException("Instrument not found with id: " + instrumentId);
        }

        Instrument instrument = instrumentOpt.get();
        log.info("Handling InstrumentDeactivatedEvent for instrument id: {}", instrumentId);

        InstrumentMode previousMode = instrument.getMode();

        // Cập nhật trạng thái
        // Set Mode = INACTIVE và Status = ERROR để vô hiệu hóa hoàn toàn
        instrument.setMode(InstrumentMode.INACTIVE);
        instrument.setStatus(InstrumentStatus.ERROR);
        instrument.setLastModeChangeReason("Deactivated from Warehouse.");

        instrumentRepository.save(instrument);

        // Ghi log kiểm toán
        InstrumentModeLog logEntry = new InstrumentModeLog();
        logEntry.setInstrumentId(instrument.getId());
        logEntry.setPreviousMode(previousMode);
        logEntry.setNewMode(InstrumentMode.INACTIVE);
        logEntry.setReason("Deactivated from Warehouse.");
        instrumentModeLogRepository.save(logEntry);

        log.info("Successfully processed deactivation for instrument id: {}", instrument.getId());
    }
    @Override
    @Transactional
    public InstrumentReagentResponse installReagent(String instrumentId, InstallReagentRequest request) {
        log.info("Installing reagent onto instrument {}: {}", instrumentId, request.getLotNumber());

        // 1. Đảm bảo thiết bị tồn tại
        if (!instrumentRepository.existsById(instrumentId)) {
            throw new NotFoundException("Instrument not found with id: " + instrumentId);
        }

        // 2. Tạo đối tượng Vendor (embedded)
        Vendor vendor = new Vendor(request.getVendorId(), request.getVendorName(), request.getVendorContact());

        // 3. Kiểm tra xem Lô này đã tồn tại (và chưa bị xóa) trên thiết bị này chưa
        List<InstrumentReagent> existing = instrumentReagentRepository
                .findByInstrumentIdAndLotNumberAndIsDeletedFalse(instrumentId, request.getLotNumber());


        if (!existing.isEmpty()) { // Kiểm tra xem danh sách có rỗng hay không
            throw new IllegalArgumentException(
                    "Reagent with Lot Number '" + request.getLotNumber() + "' is already installed on this instrument."
            );
        }

        // 3. Tạo entity InstrumentReagent mới
        InstrumentReagent reagent = new InstrumentReagent();
        reagent.setInstrumentId(instrumentId);
        reagent.setReagentName(request.getReagentName());
        reagent.setLotNumber(request.getLotNumber());
        reagent.setQuantity(request.getQuantity());
        reagent.setExpirationDate(request.getExpirationDate());
        reagent.setVendor(vendor);
        reagent.setStatus(ReagentStatus.NOT_IN_USE); // Trạng thái ban đầu khi cài đặt
        reagent.setDeleted(false);

        // 4. Lưu vào DB (trường createdBy, createdAt sẽ tự động được điền)
        InstrumentReagent savedReagent = instrumentReagentRepository.save(reagent);

        Map<String, Object> details = Map.of(
                "instrumentId", instrumentId,
                "reagentName", savedReagent.getReagentName(),
                "lotNumber", savedReagent.getLotNumber(),
                "expirationDate", savedReagent.getExpirationDate().toString(),
                "vendorId", vendor.getVendorId(),
                "vendorName", vendor.getVendorName()
        );
        auditLogService.logAction(AuditAction.INSTALL_REAGENT, savedReagent.getId(), "InstrumentReagent", details);

        log.info("Successfully installed reagent id {} onto instrument {}", savedReagent.getId(), instrumentId);

        // 6. Trả về DTO Response
        return InstrumentMapper.toReagentResponse(savedReagent);
    }
    @Override
    @Transactional
    public InstrumentReagentResponse modifyReagentStatus(String instrumentId, String reagentId, ModifyReagentStatusRequest request) {
        log.info("Modifying reagent status for reagent {} on instrument {}", reagentId, instrumentId);

        // 1. Tìm hóa chất (đảm bảo nó thuộc đúng thiết bị)
        InstrumentReagent reagent = instrumentReagentRepository.findByIdAndInstrumentId(reagentId, instrumentId)
                .orElseThrow(() -> new NotFoundException("Reagent not found with id " + reagentId + " on instrument " + instrumentId));

        // 2. Đảm bảo hóa chất chưa bị xóa (Req 3.6.2.2)
        if (reagent.isDeleted()) {
            throw new NotFoundException("Reagent not found with id " + reagentId + " on instrument " + instrumentId);
        }

        ReagentStatus newStatus = ReagentStatus.valueOf(request.getStatus());

        // 3. Kiểm tra việc cập nhật trùng lặp (Req 3.6.2.2)
        if (reagent.getStatus() == newStatus) {
            throw new IllegalArgumentException("Reagent status is already " + newStatus);
        }

        // 4. Cập nhật trạng thái
        reagent.setStatus(newStatus);
        InstrumentReagent updatedReagent = instrumentReagentRepository.save(reagent);

        // 5. Ghi log kiểm toán (Req 3.6.2.2)
        Map<String, Object> details = Map.of(
                "instrumentId", instrumentId,
                "reagentName", updatedReagent.getReagentName(),
                "lotNumber", updatedReagent.getLotNumber(),
                "oldStatus", reagent.getStatus().name(),
                "newStatus", newStatus.name()
        );
        auditLogService.logAction(AuditAction.MODIFY_REAGENT, updatedReagent.getId(), "InstrumentReagent", details);

        log.info("Successfully modified reagent status for id {} to {}", updatedReagent.getId(), newStatus);

        // 6. Trả về DTO Response
        return InstrumentMapper.toReagentResponse(updatedReagent);
    }
}
