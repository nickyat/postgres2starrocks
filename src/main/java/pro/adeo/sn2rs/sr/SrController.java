package pro.adeo.sn2rs.sr;

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
    @PostMapping("/fillSn")
    String fillSn(@RequestParam Integer limit) throws IOException {
        return batchService.fillSn(limit);
    }

}
