package sys.cache;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class DBCacheLoader {
    private static final Collection<IDBLoader> lDs = new ArrayList<>();

    public static void add(IDBLoader loader) {
        lDs.add(loader);
    }

    public static void loadAll() throws SQLException {
        for (IDBLoader lD : lDs) {
            lD.load();
        }
    }
}
