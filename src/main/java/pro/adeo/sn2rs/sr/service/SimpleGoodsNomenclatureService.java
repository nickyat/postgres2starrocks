package pro.adeo.sn2rs.sr.service;

import jakarta.annotation.PostConstruct;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pro.adeo.sn2rs.sr.model.Product;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class SimpleGoodsNomenclatureService {

    @Value("${index.dir}")
    private String indexDir;

    private IndexWriter writer;
    private Directory directory;

    @PostConstruct
    public void init() throws IOException {
        directory = FSDirectory.open(Path.of(indexDir + "/product"));
    }


    public void saveAll(List<Product> products) throws IOException {
        if (writer == null) {
            Analyzer analyzer = new KeywordAnalyzer();
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            writer = new IndexWriter(directory, config);
        }
        for (var doc : createProductDocument(products)) {
            writer.addDocument(doc);
        }

    }

    private List<List<IndexableField>> createProductDocument(List<Product> products) {
        String idField = "id";
        List<List<IndexableField>> docs = new ArrayList<>();
        for (var product : products) {
            List<IndexableField> doc = new ArrayList<>();
            doc.add(new StringField(idField, product.getId(), Field.Store.YES));
            // Поле участвующие в Join  должно быть SORTED_SET, без получим
            // Exception: unexpected docvalues type SORTED_SET for field 'id' (expected=SORTED). Re-index with correct docvalues type.
            doc.add(new SortedDocValuesField(idField, new BytesRef(product.getId())));
            doc.add(new KeywordField("pn_draft", product.getPnDraft(), Field.Store.YES));
            doc.add(new KeywordField("pn_clean", product.getPnClean(), Field.Store.YES));
            doc.add(new KeywordField("fabric", product.getFabric(), Field.Store.YES));
            doc.add(new TextField("name", product.getName(), Field.Store.YES));
            doc.add(new KeywordField("category_id", product.getCategoryId(), Field.Store.YES));
            doc.add(new KeywordField("gn_id", product.getGid(), Field.Store.YES));
//            if (product.getLinkedInfo() != null) {
//                doc.add(new StoredField("linked_info", product.getLinkedInfo()));
//            }
            docs.add(doc);
        }
        return docs;
    }

    public void closeIndex() throws IOException {
        writer.close();
    }
}