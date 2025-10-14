package moe.caa.multilogin.ksin.internal.database.table;

import org.jetbrains.exposed.v1.core.Column;
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable;

public class RepairedSkinTable extends IntIdTable {
    public final Column<String> originalTextureUrl = this.varchar("original_texture_url", 255, null);
    public final Column<String> repairedSkinVariant = this.varchar("repaired_skin_variant", 255, null);
    public final Column<String> repairedSkinCapeAlias = this.varchar("repaired_skin_alias", 255, null);

    public final Column<String> repairedSkinTextureValue = this.varchar("repaired_skin_texture_value", 255, null);
    public final Column<String> repairedSkinTextureSignature = this.varchar("repaired_skin_texture_signature", 255, null);

    public RepairedSkinTable() {
        super("ksin_repaired_skin_cache", "skin_id");

        uniqueIndex(new Column[]{
                originalTextureUrl,
                repairedSkinVariant,
                repairedSkinCapeAlias
        }, null);
    }
}
