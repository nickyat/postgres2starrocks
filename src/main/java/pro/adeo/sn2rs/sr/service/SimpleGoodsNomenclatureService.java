package pro.adeo.sn2rs.sr.service;

import jakarta.annotation.PostConstruct;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
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
            writer = new IndexWriter(directory, new IndexWriterConfig(new StandardAnalyzer()));
        }
        for (var doc : createProductDocument(products)) {
            writer.addDocument(doc);
        }

    }

    private List<List<IndexableField>> createProductDocument(List<Product> products) {
        List<List<IndexableField>> docs = new ArrayList<>();
        for (var product : products) {
            List<IndexableField> doc = new ArrayList<>();
            doc.add(new IntField("product_id", product.getProductId(), Field.Store.YES));
            doc.add(new StringField("pn_draft", product.getPnDraft(), Field.Store.YES));
            doc.add(new StringField("pn_clean", product.getPnClean(), Field.Store.YES));
            doc.add(new StringField("fabric", product.getFabric(), Field.Store.YES));
            doc.add(new TextField("name", product.getName(), Field.Store.YES));
            doc.add(new IntField("category_id", product.getCategoryId(), Field.Store.YES));
            doc.add(new IntField("gn_id", product.getGid(), Field.Store.YES));
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