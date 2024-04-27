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
import java.util.Date;
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
        Date pDate = new Date(2024 - 1900, 3, 1);

        // where gn_id=5765
        jdbcTemplate.query("select * from prices.supplier_nomenclature " + limitTerm, rs -> {
            do {

                // process it
                if (rs.getString("pn_draft") != null && rs.getString("fabric") != null) {
                    batch.add(new Offer(
                            rs.getString("id"),
                            rs.getString("pn_clean"),
                            rs.getString("pn_draft"),
                            rs.getString("fabric"),
                            rs.getString("linked_info"),

                            rs.getString("storage_id"),
                            "0",
                            rs.getString("gn_id"),
                            rs.getString("name"),
                            1000,
                            1,
                            "10",
                            rs.getString("supplier_code"),
                            rs.getDate("not_available_since"),
                            rs.getDate("not_available_since") != null && rs.getDate("not_available_since").after(pDate)
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
            } while (rs.next());
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
        // where id in (5765)
        jdbcTemplate.query("select * from prices.goods_nomenclature  " + limitTerm, rs -> {
            do {
                // process it
                if (rs.getString("pn_draft") != null && rs.getString("fabric") != null) {
                    batch.add(new Product(
                            rs.getString("id"),
                            rs.getString("pn_clean"),
                            rs.getString("pn_draft"),
                            rs.getString("fabric"),
                            rs.getString("category_id"),
                            rs.getString("gid"),
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
            } while (rs.next());
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


    public String offerUpdateByPrice(int limit) throws IOException {
        String limitTerm = "";
        if (limit > 0) limitTerm = " limit " + limit;
        Date pDate = new Date(2024 - 1900, 3, 1);
        jdbcTemplate.query("select *,COALESCE(supplier_nomenclature_id,0) as sn_id from prices.price p left join prices.c_brands b on b.gid=p.gid   " + limitTerm, rs -> {
            do {
                // process it
                if (rs.getLong("sn_id") != 0) {
                    try {
                        simpleSupplierNomenclatureService.updatePrice(new Offer(
                                        rs.getString("sn_id"),
                                        rs.getString("pn_clean"),
                                        rs.getString("pn_draft"),
                                        rs.getString("brand"),
                                        null,
                                        rs.getString("storage_id"),
                                        "",
                                        rs.getString("gn_id"),
                                        rs.getString("pn_price_desc"),
                                        Math.round(rs.getFloat("cost_eval")) * 100,
                                        rs.getInt("min_cnt"),
                                        rs.getString("remains"),
                                        rs.getString("supplier_code"),
                                        pDate,
                                        true
                                )
                        );
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            } while (rs.next());
        });
        simpleSupplierNomenclatureService.closeIndex();
        return "done";
    }
}
