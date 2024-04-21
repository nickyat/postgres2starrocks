package pro.adeo.sn2rs.sr.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pro.adeo.sn2rs.BatchService;

import java.io.IOException;

@RestController
@Tag(name = "Index: заполнить данными PG")
public class SrController {
    private final BatchService batchService;

    public SrController(BatchService batchService) {
        this.batchService = batchService;
    }


    @Operation(summary = "заполнить index из SupplierNomenclature")
    @PostMapping("/fillOffers")
    String fillOffers(@RequestParam Integer limit) throws IOException {
        return batchService.fillOfferIndex(limit);
    }

    @Operation(summary = "заполнить index из GoodsNomenclature")
    @PostMapping("/fillProduct")
    String fillProduct(@RequestParam Integer limit) throws IOException {
        return batchService.fillProductIndex(limit);
    }

}
