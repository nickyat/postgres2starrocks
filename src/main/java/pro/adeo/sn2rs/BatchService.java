package pro.adeo.sn2rs;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import pro.adeo.sn2rs.sr.model.Offer;
import pro.adeo.sn2rs.sr.model.Product;
import pro.adeo.sn2rs.sr.service.SimpleGoodsNomenclatureService;
import pro.adeo.sn2rs.sr.service.SimpleSupplierNomenclatureService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class BatchService {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleSupplierNomenclatureService simpleSupplierNomenclatureService;
    private final SimpleGoodsNomenclatureService simpleGoodsNomenclatureService;

    public BatchService(@Qualifier(value = "pgJdbcTemplate") JdbcTemplate jdbcTemplate,
                        SimpleSupplierNomenclatureService simpleSupplierNomenclatureService,
                        SimpleGoodsNomenclatureService simpleGoodsNomenclatureService
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleSupplierNomenclatureService = simpleSupplierNomenclatureService;
        this.simpleGoodsNomenclatureService = simpleGoodsNomenclatureService;
    }

    public String fillOfferIndex(Integer fetchLimit) throws IOException {
        AtomicInteger count = new AtomicInteger();
        List<Offer> batch = new ArrayList<>();
        var result = jdbcTemplate.queryForObject("select now()", String.class);
        System.out.println("Time from PG DB: " + result);
        jdbcTemplate.setFetchSize(8000);
        String limitTerm = "";
        if (fetchLimit > 0) {
            limitTerm = " limit " + fetchLimit;
        }
        jdbcTemplate.query("select * from prices.supplier_nomenclature where gn_id=5765 " + limitTerm, rs -> {
            while (rs.next()) {

                // process it
                if (rs.getString("pn_draft") != null && rs.getString("fabric") != null) {
                    batch.add(new Offer(
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
                        simpleSupplierNomenclatureService.saveAll(batch);
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
                simpleSupplierNomenclatureService.saveAll(batch);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        simpleSupplierNomenclatureService.closeIndex();
        return "Done";
    }

    public String fillProductIndex(Integer fetchLimit) throws IOException {
        AtomicInteger count = new AtomicInteger();
        List<Product> batch = new ArrayList<>();
        var result = jdbcTemplate.queryForObject("select now()", String.class);
        System.out.println("Time from PG DB: " + result);
        jdbcTemplate.setFetchSize(8000);
        String limitTerm = "";
        if (fetchLimit > 0) {
            limitTerm = " limit " + fetchLimit;
        }
        jdbcTemplate.query("select * from prices.goods_nomenclature where id in (5765,5766) " + limitTerm, rs -> {
            while (rs.next()) {

                // process it
                if (rs.getString("pn_draft") != null && rs.getString("fabric") != null) {
                    batch.add(new Product(
                            rs.getLong("id"),
                            rs.getString("pn_clean"),
                            rs.getString("pn_draft"),
                            rs.getString("fabric"),
                            rs.getInt("category_id"),
                            rs.getInt("gid"),
                            rs.getString("name")
                    ));
                } else {
                    System.out.printf("skip: pn_draft: %s fabric: %s %s %n", rs.getString("pn_draft"), rs.getString("fabric"), rs.getString("name"));
                }
                if (batch.size() >= 8000) {
                    try {
                        simpleGoodsNomenclatureService.saveAll(batch);
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
                simpleGoodsNomenclatureService.saveAll(batch);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        simpleGoodsNomenclatureService.closeIndex();
        return "Done";
    }


}
