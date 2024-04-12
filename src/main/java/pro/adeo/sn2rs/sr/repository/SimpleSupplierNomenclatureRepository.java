package pro.adeo.sn2rs.sr.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pro.adeo.sn2rs.sr.model.SupplierNomenclature;

import java.sql.PreparedStatement;
import java.util.List;

@Repository
public class SimpleSupplierNomenclatureRepository implements SupplierNomenclatureRepository {
    private final JdbcTemplate jdbcTemplate;

    public SimpleSupplierNomenclatureRepository(@Qualifier("srJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void saveAll(List<SupplierNomenclature> products) {

        jdbcTemplate.batchUpdate("INSERT INTO supplier_nomenclature (pn_clean,pn_draft,fabric,linked_info, storage_id,gn_id, name) " +
                        "VALUES (?, ?, ?, ?,?,?,?)",
                products,
                8000,
                (PreparedStatement ps, SupplierNomenclature product) -> {
                    ps.setString(1, product.getPnClean());
                    ps.setString(2, product.getPnDraft());
                    ps.setString(3, product.getFabric());
                    ps.setString(4, product.getLinkedInfo());
                    ps.setInt(5, product.getStorageId());
                    ps.setInt(6, product.getGnId());
                    ps.setString(7, product.getName());
                });
    }
}