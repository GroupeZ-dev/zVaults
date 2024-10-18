package fr.traqueur.vaults.storage.migrations;

import fr.maxlego08.sarah.database.Migration;
import fr.traqueur.vaults.api.data.VaultDTO;

public class VaultsMigration extends Migration {

    private final String table;

    public VaultsMigration(String table) {
        this.table = table;
    }

    @Override
    public void up() {
        createOrAlter("%prefix%" + table, VaultDTO.class);
    }
}
