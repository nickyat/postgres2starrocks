package pro.adeo.sn2rs;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import pro.adeo.sn2rs.sr.model.SupplierNomenclature;
import pro.adeo.sn2rs.sr.repository.SupplierNomenclatureRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class BatchService {
    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
    private int batch_size;
    private JdbcTemplate jdbcTemplate;
    SupplierNomenclatureRepository snRepository;

    public BatchService(@Qualifier(value = "pgJdbcTemplate") JdbcTemplate jdbcTemplate , SupplierNomenclatureRepository snRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.snRepository = snRepository;
    }

    void run(){
        AtomicInteger count= new AtomicInteger();
        List<SupplierNomenclature> batch= new ArrayList<>();
        var result = jdbcTemplate.queryForObject("select now()", String.class);
        System.out.println("Time from PG DB: "+result);
        jdbcTemplate.setFetchSize(batch_size);
        jdbcTemplate.query("select * from prices.supplier_nomenclature", rs -> {
            while (rs.next()) {

                // process it
                if (rs.getString("pn_draft").length()+ rs.getString("fabric").length()<70) {
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
                }
                if (batch.size()>=batch_size){
                    flushBatch(batch);
                    batch.clear();
                }
                count.getAndIncrement();
                if (count.get() %10000==0){
                    System.out.println("rows: "+count.get());
                }
            }
        });

    }

    private void flushBatch(List<SupplierNomenclature> batch) {
        snRepository.saveAll(batch);
    }


}
