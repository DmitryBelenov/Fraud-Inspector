package sys.cache;

import java.sql.SQLException;

public interface IDBLoader {
    void load() throws SQLException;
}
