package pro.adeo.sn2rs.sr.model

data class Offer(
    var id: Long = 0,
     var pnClean: String,
    var pnDraft: String,
    var fabric: String,
    var linkedInfo: String?,
    var storageId: Int,
    var gnId: Int,
    var name: String,
)
