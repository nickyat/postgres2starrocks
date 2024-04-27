package pro.adeo.sn2rs.lucene.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pro.adeo.sn2rs.lucene.service.OfferService;

import java.io.IOException;
import java.util.List;

@RestController()
@RequestMapping("/offer")
@Tag(name = "Поиск предложений (SupplierNomenclature)")
public class OfferController {
    private final OfferService offerService;

    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }


    @Operation(summary = "поиск через QueryParser (для text полей)")
    @GetMapping("/queryParser")
    List<Document> search(@RequestParam String inField, @RequestParam String queryString) throws IOException, ParseException {
        return offerService.querySearch(inField, queryString);
    }

    @Operation(summary = "поиск через TermQuery (string поля, сase sensitive) keyword analyser")
    @GetMapping("/termQuery")
    List<Document> term(@RequestParam String inField, @RequestParam String queryString) throws IOException {
        return offerService.term(inField, queryString);
    }
}
