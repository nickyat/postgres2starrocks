package pro.adeo.sn2rs.lucene.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pro.adeo.sn2rs.lucene.service.LuceneService;

import java.io.IOException;
import java.util.List;

@RestController
@Tag(name = "Токенизатор: получить список токенов")
public class LuceneController {
    private final LuceneService luceneService;

    public LuceneController(LuceneService luceneService) {
        this.luceneService = luceneService;
    }

    @Operation(summary = "прогнать текст через StandardAnalyzer (распознает URLs и emails, удаляет стоп слова и приводит токены в нижний регистр)")
    @GetMapping("/useStandardAnalyzer")
    List<String> useStandardAnalyzer(@RequestParam String text) throws IOException {
        return luceneService.useStandardAnalyzer(text);
    }

    @Operation(summary = "прогнать текст через StopAnalyzer (включает LetterTokenizer (splits text by non-letter characters), LowerCaseFilter, StopFilter). Файл  resource/stopwords.txt")
    @GetMapping("/useStopAnalyzer")
    List<String> useStopAnalyzer(@RequestParam String text) throws IOException {
        return luceneService.useStopAnalyzer(text);
    }

    @Operation(summary = "прогнать текст через SimpleAnalyzer(НЕ распознает URLs и email, включает LetterTokenizer и LowerCaseFilter)")
    @GetMapping("/useSimpleAnalyzer")
    List<String> useSimpleAnalyzer(@RequestParam String text) throws IOException {
        return luceneService.useSimpleAnalyzer(text);
    }

    @Operation(summary = "прогнать текст через WhiteSpace(только WhitespaceTokenizer, разделение на токены через whitespace символы)")
    @GetMapping("/useWhitespaceAnalyzer")
    List<String> useWhitespaceAnalyzer(@RequestParam String text) throws IOException {
        return luceneService.useWhitespaceAnalyzer(text);
    }

    @Operation(summary = "прогнать текст через KeywordAnalyzer(все слова один токен)")
    @GetMapping("/useKeywordAnalyzer")
    List<String> useKeywordAnalyzer(@RequestParam String text) throws IOException {
        return luceneService.useKeywordAnalyzer(text);
    }

    @Operation(summary = "прогнать текст через RussianAnalyzer(словоформы) https://github.com/AKuznetsov/russianmorphology")
    @GetMapping("/useRussianAnalyzer")
    List<String> useRussianAnalyzer(@RequestParam String text) throws IOException {
        return luceneService.RussianAnalyzer(text);
    }

    @Operation(summary = "поиск через QueryParser (для text полей)")
    @GetMapping("/queryParser")
    List<Document> search(@RequestParam String inField, @RequestParam String queryString) throws IOException, ParseException {
        return luceneService.querySearch(inField, queryString);
    }

    @Operation(summary = "поиск через TerrmQuery (string поля, сase sensitive)")
    @GetMapping("/termQuery")
    List<Document> term(@RequestParam String inField, @RequestParam String queryString) throws IOException, ParseException {
        return luceneService.term(inField, queryString);
    }

}
