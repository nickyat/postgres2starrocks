package pro.adeo.sn2rs.sr.repository;

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
import pro.adeo.sn2rs.sr.model.SupplierNomenclature;

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


    public void saveAll(List<SupplierNomenclature> products) throws IOException {
        if (writer == null) {
            writer = new IndexWriter(directory, new IndexWriterConfig(new KeywordAnalyzer()));
        }
        for (var doc : createDocument(products)) {
            writer.addDocument(doc);
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
            if (product.getLinkedInfo() != null) {
                doc.add(new StoredField("linked_info", product.getLinkedInfo()));
            }
            docs.add(doc);
        }
        return docs;
    }

    public void closeIndex() throws IOException {
        writer.close();
    }
}