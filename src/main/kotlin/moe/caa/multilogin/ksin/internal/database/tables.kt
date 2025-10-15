package moe.caa.multilogin.ksin.internal.database

import moe.caa.multilogin.ksin.internal.SkinVariant
import org.jetbrains.exposed.v1.core.dao.id.CompositeIdTable
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object PlayerCurrentUseTable : CompositeIdTable("ksin_player_current_use") {
    val playerId = uuid("player_uuid")
    val skinId = integer("skin_id")

    override val primaryKey = PrimaryKey(playerId)
}


object RepairedSkinTable : IntIdTable("ksin_repaired_skin_cache", "skin_id") {
    val originalTextureUrl = varchar("original_texture_url", 255)
    val repairedSkinVariant = enumeration("repaired_skin_variant", SkinVariant::class)
    val repairedSkinCapeAlias = varchar("repaired_skin_alias", 255)

    val repairedSkinTextureValue = varchar("repaired_skin_texture_value", 255)
    val repairedSkinTextureSignature = varchar("repaired_skin_texture_signature", 255)

    init {
        uniqueIndex(originalTextureUrl, repairedSkinVariant, repairedSkinCapeAlias)
    }
}