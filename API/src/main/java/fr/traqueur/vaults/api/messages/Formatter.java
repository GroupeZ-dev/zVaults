package fr.traqueur.vaults.api.messages;

import fr.traqueur.vaults.api.VaultsPlugin;

import java.util.function.Function;

public class Formatter {

    private final String pattern;
    private final Function<VaultsPlugin, String> supplier;

    private Formatter(String pattern, Object supplier) {
        this.pattern = pattern;
        this.supplier = (api) -> supplier.toString();
    }

    private Formatter(String pattern, Function<VaultsPlugin, String> supplier) {
        this.pattern = pattern;
        this.supplier = supplier;
    }

    public static Formatter format(String pattern, Object supplier) {
        return new Formatter(pattern, supplier);
    }

    public static Formatter format(String pattern, Function<VaultsPlugin, String> supplier) {
        return new Formatter(pattern, supplier);
    }

    public String handle(VaultsPlugin api, String message) {
        return message.replaceAll(this.pattern, String.valueOf(this.supplier.apply(api)));
    }
}