package moe.caa.multilogin.ksin.internal.database

import org.jetbrains.exposed.v1.core.dao.id.CompositeIdTable
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object PlayerCurrentUseTable : CompositeIdTable("ksin_player_current_use") {
    val playerId = this.uuid("player_uuid")
    val skinId = this.integer("skin_id")

    override val primaryKey = PrimaryKey(playerId)
}


object RepairedSkinTable : IntIdTable("ksin_repaired_skin_cache", "skin_id") {
    val originalTextureUrl = this.varchar("original_texture_url", 255)
    val repairedSkinVariant = this.varchar("repaired_skin_variant", 255)
    val repairedSkinCapeAlias = this.varchar("repaired_skin_alias", 255)

    val repairedSkinTextureValue = this.varchar("repaired_skin_texture_value", 255)
    val repairedSkinTextureSignature = this.varchar("repaired_skin_texture_signature", 255)

    init {
        uniqueIndex(originalTextureUrl, repairedSkinVariant, repairedSkinCapeAlias)
    }
}