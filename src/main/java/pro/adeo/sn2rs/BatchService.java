package pro.adeo.sn2rs;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import pro.adeo.sn2rs.sr.model.SupplierNomenclature;
import pro.adeo.sn2rs.sr.repository.SupplierNomenclatureRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class BatchService {
    @Value("${index.fetch.limit}")
    private int fetchLimit;
    private final JdbcTemplate jdbcTemplate;
    private final SupplierNomenclatureRepository snRepository;

    public BatchService(@Qualifier(value = "pgJdbcTemplate") JdbcTemplate jdbcTemplate, SupplierNomenclatureRepository snRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.snRepository = snRepository;
    }

    void run() throws IOException {
        AtomicInteger count = new AtomicInteger();
        List<SupplierNomenclature> batch = new ArrayList<>();
        var result = jdbcTemplate.queryForObject("select now()", String.class);
        System.out.println("Time from PG DB: " + result);
        jdbcTemplate.setFetchSize(8000);
        String limitTerm = "";
        if (fetchLimit > 0) {
            limitTerm = " limit " + fetchLimit;
        }
        jdbcTemplate.query("select * from prices.supplier_nomenclature " + limitTerm, rs -> {
            while (rs.next()) {

                // process it
                if (rs.getString("pn_draft") != null && rs.getString("fabric") != null) {
                    batch.add(new SupplierNomenclature(
                            rs.getLong("id"),
                            rs.getString("pn_clean"),
                            rs.getString("pn_draft"),
                            rs.getString("fabric"),
                            rs.getString("linked_info"),

                            rs.getInt("storage_id"),
                            rs.getInt("gn_id"),
                            rs.getString("name")

                    ));
                } else {
                    System.out.printf("skip: pn_draft: %s fabric: %s %s %n", rs.getString("pn_draft"), rs.getString("fabric"), rs.getString("name"));
                }
                if (batch.size() >= 8000) {
                    try {
                        flushBatch(batch);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    batch.clear();
                }
                count.getAndIncrement();
                if (count.get() % 10000 == 0) {
                    System.out.println("rows: " + count.get());
                }
            }
        });
        if (!batch.isEmpty()) {
            try {
                flushBatch(batch);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
// try search
        snRepository.result();
    }

    private void flushBatch(List<SupplierNomenclature> batch) throws IOException {
        snRepository.saveAll(batch);
    }


}
