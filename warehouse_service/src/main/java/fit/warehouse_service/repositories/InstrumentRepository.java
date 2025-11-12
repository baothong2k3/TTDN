/*
<<<<<<< HEAD
 * @ {#} InstrumentRepository.java   1.0     29/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.repositories;

import fit.warehouse_service.entities.Instrument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/*
 * @description: Repository interface for managing Instrument entities.
 * @author: Tran Hien Vinh
 * @date:   29/10/2025
 * @version:    1.0
 */
@Repository
public interface InstrumentRepository extends JpaRepository<Instrument, String>, JpaSpecificationExecutor<Instrument> {
    Optional<Instrument> findByName(String name);

}
