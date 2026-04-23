package studio.seer.anvil.model;

public record PhantomColumn(
        String geoid,
        String qualifiedName,
        String tableGeoid,
        String dataSource
) {}
