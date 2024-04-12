package pro.adeo.sn2rs.pg.repository;

import pro.adeo.sn2rs.sr.model.SupplierNomenclature;

import java.util.List;

public interface SupplierNomenclatureRepository {
    void findAll(List<SupplierNomenclature> products);
}
