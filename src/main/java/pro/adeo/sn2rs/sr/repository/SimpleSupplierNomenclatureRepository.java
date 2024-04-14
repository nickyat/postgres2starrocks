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
    //@Transactional
    public void saveAll(List<SupplierNomenclature> products) {
        for(var product:products) {
            jdbcTemplate.update("INSERT INTO supplier_nomenclature (pn_draft,fabric,linked_info, storage_id,gn_id, name) VALUES ('%s','%s','%s','%d','%d','%s')"
                    .formatted(escape(product.getPnDraft()), escape(product.getFabric()), product.getLinkedInfo(), product.getStorageId(), product.getGnId(), escape(product.getName())));
        }
    }
    public String escape(String s){
        // Для запросов !    "    $    '    (    )    -    /    <    @    \    ^    |    ~
        return s.replace("\\","\\\\")
                .replace("'","\\'");

    }
}