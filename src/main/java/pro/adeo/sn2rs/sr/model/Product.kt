package pro.adeo.sn2rs.sr.model

data class Product(
    var productId: Int,
    var pnClean: String,
    var pnDraft: String,
    var fabric: String,
    var categoryId: Int,
    var gid: Int,
    var name: String,
)
