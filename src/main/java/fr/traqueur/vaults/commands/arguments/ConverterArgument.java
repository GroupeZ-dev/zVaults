package fr.traqueur.vaults.commands.arguments;

import fr.traqueur.commands.api.arguments.ArgumentConverter;
import fr.traqueur.vaults.converters.Converters;

public class ConverterArgument implements ArgumentConverter<Converters> {
    @Override
    public Converters apply(String s) {
        try {
            return Converters.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
