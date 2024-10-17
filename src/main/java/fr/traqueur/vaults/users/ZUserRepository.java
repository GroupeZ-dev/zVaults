package fr.traqueur.vaults.users;

import fr.traqueur.vaults.api.data.UserDTO;
import fr.traqueur.vaults.api.storage.Repository;
import fr.traqueur.vaults.api.users.User;

public class ZUserRepository implements Repository<User, UserDTO> {
    @Override
    public User toEntity(UserDTO userDTO) {
        return new ZUser(userDTO.uniqueId(), userDTO.name());
    }

    @Override
    public UserDTO toDTO(User entity) {
        return new UserDTO(entity.getUniqueId(), entity.getName());
    }
}
