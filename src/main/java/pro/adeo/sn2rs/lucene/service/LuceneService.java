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
    private IndexSearcher searcherSn;
    private IndexSearcher searcherGn;
    private KeywordAnalyzer analyzer;

    @PostConstruct
    void init() throws IOException {
        try {
            var directorySn = FSDirectory.open(Path.of(indexDir + "/offers"));
            IndexReader indexReaderSn = DirectoryReader.open(directorySn);
            searcherSn = new IndexSearcher(indexReaderSn);
            analyzer = new KeywordAnalyzer();
        } catch (IndexNotFoundException e) {
            log.error("Index [offers] не найден: {}", indexDir);
        }
        try {
            var directoryGn = FSDirectory.open(Path.of(indexDir + "/product"));
            IndexReader indexReaderGn = DirectoryReader.open(directoryGn);
            searcherSn = new IndexSearcher(indexReaderGn);
        } catch (IndexNotFoundException e) {
            log.error("Index [product] не найден: {}", indexDir);
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

    public List<Document> querySearch(String inField, String queryString) throws IOException, ParseException {
        Query query = new QueryParser(inField, analyzer)
                .parse(queryString);
        TopDocs topDocs = searcherSn.search(query, 20);
        List<Document> documents = new ArrayList<>();
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            documents.add(searcherSn.storedFields().document(scoreDoc.doc));
        }
        return documents;
    }

    public List<Document> term(String inField, String queryString) throws IOException {
        Query query = new TermQuery(new Term(inField, queryString));
        TopDocs topDocs = searcherSn.search(query, 20);
        List<Document> documents = new ArrayList<>();
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            documents.add(searcherSn.doc(scoreDoc.doc));
        }
        return documents;
    }
}
