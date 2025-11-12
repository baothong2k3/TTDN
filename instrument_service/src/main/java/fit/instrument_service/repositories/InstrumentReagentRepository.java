package fit.instrument_service.repositories;

import fit.instrument_service.entities.InstrumentReagent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstrumentReagentRepository extends MongoRepository<InstrumentReagent, String> {
    // Tìm hóa chất bằng ID và ID thiết bị (để đảm bảo đúng)
    Optional<InstrumentReagent> findByIdAndInstrumentId(String id, String instrumentId);

    List<InstrumentReagent> findByInstrumentIdAndLotNumberAndIsDeletedFalse(String instrumentId, String lotNumber);
}