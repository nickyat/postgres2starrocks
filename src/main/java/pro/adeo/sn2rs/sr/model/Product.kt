package pro.adeo.sn2rs.sr.model

data class Product(
    var id: String,
    var pnClean: String,
    var pnDraft: String,
    var fabric: String,
    var categoryId: String,
    var gid: String,
    var name: String,
)
