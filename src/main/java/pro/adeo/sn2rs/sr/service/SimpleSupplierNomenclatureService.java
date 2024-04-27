package pro.adeo.sn2rs.sr.service;

import jakarta.annotation.PostConstruct;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
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
        directory = FSDirectory.open(Path.of(indexDir + "/offer"));
    }


    public void saveAll(List<Offer> offers) throws IOException {
        if (writer == null) {
            Analyzer analyzer = new KeywordAnalyzer();
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            writer = new IndexWriter(directory, config);
        }
        for (var doc : createOfferDocuments(offers)) {
            writer.addDocument(doc);
        }

    }

    private List<IndexableField> createOfferDocument(Offer offer) {
        String idField = "id";
        String toField = "product_id";
        List<IndexableField> doc = new ArrayList<>();
        doc.add(new StringField(idField, offer.getId(), Field.Store.YES));
        // Поле участвующие в Join  должно быть SORTED_SET, без получим
        // Exception: unexpected docvalues type SORTED_SET for field 'id' (expected=SORTED). Re-index with correct docvalues type.
        doc.add(new SortedDocValuesField(idField, new BytesRef(offer.getId())));

        doc.add(new KeywordField("pn_clean", offer.getPnClean(), Field.Store.YES));
        doc.add(new KeywordField("pn_draft", offer.getPnDraft(), Field.Store.YES));
        doc.add(new KeywordField("fabric", offer.getFabric(), Field.Store.YES));
        doc.add(new TextField("name", offer.getName(), Field.Store.YES));
        doc.add(new KeywordField("storage_id", offer.getStorageId(), Field.Store.YES));
        doc.add(new KeywordField("reg_storage_id", offer.getRegStorageId(), Field.Store.YES));

        if (offer.getGnId() != null) {
            doc.add(new StringField(toField, offer.getGnId(), Field.Store.YES));
        }
        doc.add(new SortedDocValuesField(toField, new BytesRef(offer.getGnId())));

        doc.add(new IntField("cost", offer.getCost(), Field.Store.YES));
        if (offer.getMinCnt() != null) doc.add(new IntField("min_cnt", offer.getMinCnt(), Field.Store.YES));
        doc.add(new KeywordField("remains", offer.getRemains(), Field.Store.YES));
        if (offer.getSupplierCode() != null)
            doc.add(new KeywordField("supplier_code", offer.getSupplierCode(), Field.Store.YES));
        if (offer.getLastAvailable() != null)
            doc.add(new KeywordField("last_available", offer.getLastAvailable().toString(), Field.Store.YES));
        doc.add(new KeywordField("in_price", String.valueOf(offer.getInPrice()), Field.Store.YES));

        if (offer.getLinkedInfo() != null) {
            doc.add(new StoredField("linked_info", offer.getLinkedInfo()));
        }


        return doc;
    }

    private List<List<IndexableField>> createOfferDocuments(List<Offer> offers) {

        List<List<IndexableField>> docs = new ArrayList<>();
        for (var offer : offers) {
            if (offer.getGnId() == null) continue;
            docs.add(createOfferDocument(offer));
        }
        return docs;
    }

    public void closeIndex() throws IOException {
        writer.close();
        writer = null;
    }

    public void updatePrice(Offer offer) throws IOException {
        if (writer == null) {
            Analyzer analyzer = new KeywordAnalyzer();
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            writer = new IndexWriter(directory, config);
        }
        writer.updateDocument(new Term("id", offer.getId()), createOfferDocument(offer));
    }


}