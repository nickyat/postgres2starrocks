package pro.adeo.sn2rs.sr.model

import java.util.*

data class Offer(
    var id: String,
    var pnClean: String,
    var pnDraft: String,
    var fabric: String,
    var linkedInfo: String?,
    var storageId: String,
    var regStorageId: String,
    var gnId: String?,
    var name: String,
    var cost: Int,
    var minCnt: Int?,
    var remains: String,
    var supplierCode: String?,
    var lastAvailable: Date?,
    var inPrice: Boolean,

)
