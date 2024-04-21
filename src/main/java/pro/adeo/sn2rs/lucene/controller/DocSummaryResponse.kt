package pro.adeo.sn2rs.lucene.controller

import org.apache.lucene.document.Document

class DocSummaryResponse(private var documents: List<Document>) {
    var total: Int = documents.size
    var doc = documents
}
