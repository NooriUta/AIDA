package studio.seer.tenantrouting;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultYggLineageRegistryTest {

    @Test
    void resourceForAnyAlias_returnsSameConnection() throws Exception {
        var registry = buildRegistry("http://localhost:2480", "hound_default", "root", "test");

        var conn1 = registry.resourceFor("acme-corp");
        var conn2 = registry.resourceFor("globex");
        var conn3 = registry.resourceFor("default");

        assertThat(conn1).isSameAs(conn2);
        assertThat(conn2).isSameAs(conn3);
    }

    @Test
    void databaseName_matchesConfig() throws Exception {
        var registry = buildRegistry("http://localhost:2480", "hound_default", "root", "test");
        assertThat(registry.resourceFor("any").databaseName()).isEqualTo("hound_default");
    }

    @Test
    void invalidate_doesNotThrow() throws Exception {
        var registry = buildRegistry("http://localhost:2480", "hound_default", "root", "test");
        registry.invalidate("acme-corp"); // must not throw
        registry.invalidateAll();
    }

    private DefaultYggLineageRegistry buildRegistry(String url, String db, String user, String pwd)
            throws Exception {
        var reg = new DefaultYggLineageRegistry();
        setField(reg, "yggLineageUrl", url);
        setField(reg, "yggLineageDb", db);
        setField(reg, "yggUser", user);
        setField(reg, "yggPassword", pwd);
        reg.init();
        return reg;
    }

    private void setField(Object obj, String name, String value) throws Exception {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(obj, value);
    }
}
