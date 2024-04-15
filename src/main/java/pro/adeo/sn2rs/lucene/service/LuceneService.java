package pro.adeo.sn2rs.lucene.service;

import jakarta.annotation.PostConstruct;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.morphology.russian.RussianAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class LuceneService {
    private static final Logger log = LoggerFactory.getLogger(LuceneService.class);
    private static final String FIELD_NAME = "sampleName";
    @Value("${index.dir}")
    private String indexDir;
    private IndexSearcher searcher;
    private StandardAnalyzer analyzer;

    @PostConstruct
    void init() throws IOException {
        try {
            var directory = FSDirectory.open(Path.of(indexDir));
            IndexReader indexReader = DirectoryReader.open(directory);
            searcher = new IndexSearcher(indexReader);
            analyzer = new StandardAnalyzer();
        } catch (IndexNotFoundException e) {
            log.error("Index не найден: {}", indexDir);
        }
    }

    public List<String> analyze(String text, Analyzer analyzer) throws IOException {
        List<String> result = new ArrayList<>();
        TokenStream tokenStream = analyzer.tokenStream(FIELD_NAME, text);
        CharTermAttribute attr = tokenStream.addAttribute(CharTermAttribute.class);
        tokenStream.reset();
        while (tokenStream.incrementToken()) {
            result.add(attr.toString());
        }
        return result;
    }

    public List<String> useStandardAnalyzer(String text) throws IOException {
        return analyze(text, new StandardAnalyzer());
    }

    public List<String> useStopAnalyzer(String text) throws IOException {
        String resourceName = "stopwords.txt";

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(resourceName).getFile());
        return analyze(text, new StopAnalyzer(Path.of(file.getAbsolutePath())));
    }

    public List<String> useSimpleAnalyzer(String text) throws IOException {
        return analyze(text, new SimpleAnalyzer());
    }

    public List<String> useWhitespaceAnalyzer(String text) throws IOException {
        return analyze(text, new WhitespaceAnalyzer());
    }

    public List<String> useKeywordAnalyzer(String text) throws IOException {
        return analyze(text, new KeywordAnalyzer());
    }

    public List<String> RussianAnalyzer(String text) throws IOException {
        return analyze(text, new RussianAnalyzer());
    }

    public List<Document> search(String inField, String queryString) throws IOException, ParseException {
        Query query = new QueryParser(inField, analyzer)
                .parse(queryString);
        TopDocs topDocs = searcher.search(query, 20);
        List<Document> documents = new ArrayList<>();
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            documents.add(searcher.doc(scoreDoc.doc));
        }
        return documents;
    }

    public List<Document> term(String inField, String queryString) throws IOException, ParseException {
        Query query = new TermQuery(new Term(inField, queryString));
        TopDocs topDocs = searcher.search(query, 20);
        List<Document> documents = new ArrayList<>();
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            documents.add(searcher.doc(scoreDoc.doc));
        }
        return documents;
    }
}
