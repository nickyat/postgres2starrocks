package pro.adeo.sn2rs.sr.service;

import jakarta.annotation.PostConstruct;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.adeo.sn2rs.sr.model.Offer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class SimpleSupplierNomenclatureService {

    @Value("${index.dir}")
    private String indexDir;

    private IndexWriter writer;
    private Directory directory;

    @PostConstruct
    public void init() throws IOException {
        directory = FSDirectory.open(Path.of(indexDir + "/offers"));
    }


    public void saveAll(List<Offer> products) throws IOException {
        if (writer == null) {
            writer = new IndexWriter(directory, new IndexWriterConfig(new KeywordAnalyzer()));
        }
        for (var doc : createOfferDocument(products)) {
            writer.addDocument(doc);
        }

    }

    private List<List<IndexableField>> createOfferDocument(List<Offer> offers) {
        List<List<IndexableField>> docs = new ArrayList<>();
        for (var offer : offers) {
            List<IndexableField> doc = new ArrayList<>();
            doc.add(new LongField("offer_id", offer.getOfferId(), Field.Store.YES));
            doc.add(new StringField("pn_clean", offer.getPnClean(), Field.Store.YES));
            doc.add(new StringField("pn_draft", offer.getPnDraft(), Field.Store.YES));
            doc.add(new StringField("fabric", offer.getFabric(), Field.Store.YES));
            doc.add(new TextField("name", offer.getName(), Field.Store.YES));
            doc.add(new IntField("storage_id", offer.getStorageId(), Field.Store.YES));
            doc.add(new IntField("gn_id", offer.getGnId(), Field.Store.YES));
            if (offer.getLinkedInfo() != null) {
                doc.add(new StoredField("linked_info", offer.getLinkedInfo()));
            }
            docs.add(doc);
        }
        return docs;
    }

    public void closeIndex() throws IOException {
        writer.close();
    }
}