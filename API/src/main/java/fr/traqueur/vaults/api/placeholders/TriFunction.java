package fr.traqueur.vaults.api.placeholders;

@FunctionalInterface
public interface TriFunction<T, U, V, R> {

    R apply(T t, U u, V v);

}
