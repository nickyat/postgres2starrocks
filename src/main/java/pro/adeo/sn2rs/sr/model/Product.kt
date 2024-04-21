package pro.adeo.sn2rs.sr.model

data class Product(
    var id: Long = 0,
    var pnClean: String,
    var pnDraft: String,
    var fabric: String,
    var categoryId: Int,
    var gid: Int,
    var name: String,
)
