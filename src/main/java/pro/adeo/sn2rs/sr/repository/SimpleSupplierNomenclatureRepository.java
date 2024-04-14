package pro.adeo.sn2rs.sr.repository;

import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Repository;
import pro.adeo.sn2rs.sr.model.SupplierNomenclature;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Repository
public class SimpleSupplierNomenclatureRepository implements SupplierNomenclatureRepository {

    Path tmpDir = Path.of("/tmp/sn");
    private IndexWriter writer;
    private Directory directory;

    public SimpleSupplierNomenclatureRepository() throws IOException {
        directory = FSDirectory.open(tmpDir);
        writer = new IndexWriter(directory, new IndexWriterConfig());
    }

    @Override
    public void saveAll(List<SupplierNomenclature> products) throws IOException {

        for (var doc : createDocument(products)) {
            writer.addDocument(doc);
        }

    }

    @Override
    public void result() throws IOException {
        try (IndexReader reader = DirectoryReader.open(writer)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            TermQuery termQuery = new TermQuery(new Term("fabric", "GAC"));
            TopDocs topDocs = searcher.search(termQuery, 100);
            System.out.println("Query " + termQuery + " matched " + topDocs.totalHits + " documents:");
            StoredFields storedFields = reader.storedFields();
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                // Using the doc's ID, we load a `Document`, and load the `text` field (the only field).
                String storedText = storedFields.document(scoreDoc.doc).get("fabric")
                        +" "
                        + storedFields.document(scoreDoc.doc).get("name");
                // Each document has a BM25 score based on their relevance to the query.
                // See https://en.wikipedia.org/wiki/Okapi_BM25 for details on the BM25 formula.
                //
                // The parts of the formula relevant to this example are:
                // 1. A search term occurring more frequently in a document makes the score go up.
                // 2. More total terms in the field makes the score go down.
                //
                // In this case both of our matching documents contain the word `fox` once, so they're tied there.
                // The last document is shorter than the first document, so the last document gets a higher score.
                System.out.println(scoreDoc.score + " - " + scoreDoc.doc + " - " + storedText);
                //
            }
        }
    }

    private List<List<IndexableField>> createDocument(List<SupplierNomenclature> products) {
        List<List<IndexableField>> docs = new ArrayList<>();
        for (var product : products) {
            List<IndexableField> doc = new ArrayList<>();
            doc.add(new StringField("pn_draft", product.getPnDraft(), Field.Store.YES));
            doc.add(new StringField("fabric", product.getFabric(), Field.Store.YES));
            doc.add(new TextField("name", product.getName(), Field.Store.YES));
            doc.add(new IntField("storage_id", product.getStorageId(), Field.Store.YES));
            doc.add(new IntField("gn_id", product.getGnId(), Field.Store.YES));
            if (product.getLinkedInfo()!=null) {
                doc.add(new StoredField("linked_info", product.getLinkedInfo()));
            }
            docs.add(doc);
        }
        return docs;
    }
}