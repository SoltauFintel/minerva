package gitper.cache.valkey;

public final class ValkeyPool {
    private final io.valkey.JedisPool pool;
    
    public ValkeyPool(String host, int port) {
        pool = new io.valkey.JedisPool(host, port);
    }
    
    public ValkeyResource getResource() {
        return new ValkeyResource(pool.getResource());
    }
}
