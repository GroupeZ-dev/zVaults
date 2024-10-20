package fr.traqueur.vaults.storage.migrations;

import fr.maxlego08.sarah.database.Migration;
import fr.traqueur.vaults.api.data.SharedAccessDTO;

public class SharedAccessMigration extends Migration {

    private final String table;

    public SharedAccessMigration(String table) {
        this.table = table;
    }

    @Override
    public void up() {
        this.createOrAlter("%prefix%"+this.table, SharedAccessDTO.class);
    }
}
