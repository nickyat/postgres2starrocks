package pro.adeo.sn2rs.lucene.service;

import jakarta.annotation.PostConstruct;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class OfferService {
    private static final Logger log = LoggerFactory.getLogger(OfferService.class);
    private static final String FIELD_NAME = "sampleName";
    @Value("${index.dir}")
    private String indexDir;
    private IndexSearcher searcherOffer;
    private KeywordAnalyzer analyzer;

    @PostConstruct
    void init() throws IOException {
        try {
            var directorySn = FSDirectory.open(Path.of(indexDir + "/offer"));
            IndexReader indexReaderOffer = DirectoryReader.open(directorySn);
            searcherOffer = new IndexSearcher(indexReaderOffer);
            analyzer = new KeywordAnalyzer();
        } catch (IndexNotFoundException e) {
            log.error("Index [offers] не найден: {}", indexDir);
        }

    }

    public List<Document> querySearch(String inField, String queryString) throws IOException, ParseException {
        Query query = new QueryParser(inField, analyzer)
                .parse(queryString);
        TopDocs topDocs = searcherOffer.search(query, 20);
        List<Document> documents = new ArrayList<>();
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            documents.add(searcherOffer.storedFields().document(scoreDoc.doc));
        }
        return documents;
    }

    public List<Document> term(String inField, String queryString) throws IOException {
        Query query = new TermQuery(new Term(inField, queryString));
        TopDocs topDocs = searcherOffer.search(query, 20);
        List<Document> documents = new ArrayList<>();
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            documents.add(searcherOffer.doc(scoreDoc.doc));
        }
        return documents;
    }
}
