package pro.adeo.sn2rs.sr.repository;

import pro.adeo.sn2rs.sr.model.SupplierNomenclature;

import java.io.IOException;
import java.util.List;

public interface SupplierNomenclatureRepository {
    void saveAll(List<SupplierNomenclature> products) throws IOException;

    void result() throws IOException;
}
