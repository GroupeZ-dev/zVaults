package fr.traqueur.vaults.storage.migrations;

import fr.maxlego08.sarah.database.Migration;
import fr.traqueur.vaults.api.data.UserDTO;

public class UserMigration extends Migration {

    private final String table;

    public UserMigration(String table) {
        this.table = table;
    }

    @Override
    public void up() {
        this.createOrAlter("%prefix%"+ table, UserDTO.class);
    }
}
