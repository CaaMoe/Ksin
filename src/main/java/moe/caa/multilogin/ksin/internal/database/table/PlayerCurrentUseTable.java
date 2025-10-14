package moe.caa.multilogin.ksin.internal.database.table;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.exposed.v1.core.Column;
import org.jetbrains.exposed.v1.core.dao.id.CompositeIdTable;

import java.util.UUID;

public class PlayerCurrentUseTable extends CompositeIdTable {
    public final Column<UUID> playerId = this.uuid("player_uuid");
    public final Column<Integer> skinId = this.integer("skin_id", null);

    public PlayerCurrentUseTable() {
        super("ksin_player_current_use");
    }

    @Override
    public @Nullable PrimaryKey getPrimaryKey() {
        return new PrimaryKey(new Column[]{playerId}, null);
    }
}
