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
import org.apache.lucene.search.join.JoinUtil;
import org.apache.lucene.search.join.ScoreMode;
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
public class ProductService {
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private static final String FIELD_NAME = "sampleName";
    @Value("${index.dir}")
    private String indexDir;
    private IndexSearcher searcherProduct;
    private IndexSearcher searcherOffer;
    private KeywordAnalyzer analyzer;

    @PostConstruct
    void init() throws IOException {
        analyzer = new KeywordAnalyzer();
        try {
            var directoryProductIndex = FSDirectory.open(Path.of(indexDir + "/product"));
            IndexReader indexReaderProduct = DirectoryReader.open(directoryProductIndex);
            searcherProduct = new IndexSearcher(indexReaderProduct);
        } catch (IndexNotFoundException e) {
            log.error("Index [product] не найден: {}", indexDir);
        }
        try {
            var directoryOfferIndex = FSDirectory.open(Path.of(indexDir + "/offer"));
            IndexReader indexReaderOffer = DirectoryReader.open(directoryOfferIndex);
            searcherOffer = new IndexSearcher(indexReaderOffer);
        } catch (IndexNotFoundException e) {
            log.error("Index [offer] не найден: {}", indexDir);
        }

    }

    public List<Document> querySearch(String inField, String queryString) throws IOException, ParseException {
        Query query = new QueryParser(inField, analyzer)
                .parse(queryString);
        TopDocs topDocs = searcherProduct.search(query, 20);
        List<Document> documents = new ArrayList<>();
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            documents.add(searcherProduct.storedFields().document(scoreDoc.doc));
        }
        return documents;
    }

    public List<Document> term(String inField, String queryString) throws IOException {
        Query query = new TermQuery(new Term(inField, queryString));
        TopDocs topDocs = searcherProduct.search(query, 20);
        List<Document> documents = new ArrayList<>();
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            documents.add(searcherProduct.doc(scoreDoc.doc));
        }
        return documents;
    }

    public List<Document> termJoin(String productField,
                                   String queryProductString, String offerField, String queryOfferString) throws IOException {
        boolean multipleValuesPerDocument = false;

        Query fromQueryProduct = new TermQuery(new Term(productField, queryProductString));
        Query fromQueryOffer = new TermQuery(new Term(offerField, queryOfferString));

        Query joinQuery = JoinUtil.createJoinQuery("id", multipleValuesPerDocument, "product_id", fromQueryProduct, searcherProduct, ScoreMode.None);

        BooleanQuery.Builder finalQuery = new BooleanQuery.Builder();
        BooleanQuery.Builder q1 = new BooleanQuery.Builder();
        q1.add(fromQueryOffer, BooleanClause.Occur.SHOULD);

        finalQuery.add(q1.build(), BooleanClause.Occur.MUST);
        finalQuery.add(joinQuery, BooleanClause.Occur.MUST);
        Query queryForSearching = finalQuery.build();

        TopDocs topDocs = searcherOffer.search(queryForSearching, 100);
        List<Document> documents = new ArrayList<>();
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            documents.add(searcherOffer.doc(scoreDoc.doc));
        }
        return documents;
    }
}
