package sys.cache;

import database.DBConnectionHolder;
import database.DBSource;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public abstract class DBCache<T extends CacheUnit> implements IDBLoader {

    private final DBSource dbSource;
    protected final Map<Long, T> tpCache = new ConcurrentHashMap<>();
    protected final Map<String, T> codeCache = new ConcurrentHashMap<>();

    public DBCache() {
        final DBConnectionHolder dbConnHolder = DBConnectionHolder.instance();
        dbSource = dbConnHolder.getSource();
    }

    protected abstract Iterator<T> cacheVIterator();

    protected abstract Stream<T> cacheVStream();

    protected abstract T addByKey(final Long key, T type);

    protected abstract T addByCode(final String code, T type);

    protected abstract T get(final Long key);

    protected abstract T getByCode(final String code);

    protected abstract int cacheSize();

    protected final DBSource getDbSource() {
        return dbSource;
    }
}
