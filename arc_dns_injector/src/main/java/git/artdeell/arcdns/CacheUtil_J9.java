package git.artdeell.arcdns;

import static git.artdeell.arcdns.CacheUtilCommons.NEVER_EXPIRATION;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class CacheUtil_J9 {

    private static final String INET_ADDRESS_CACHED_ADDRESSES_CLASS_NAME = "java.net.InetAddress$CachedAddresses";

    public static void setInetAddressCache(String host, String[] ips, long expireMillis)
            throws UnknownHostException, IllegalAccessException, InstantiationException, InvocationTargetException, ClassNotFoundException, NoSuchFieldException {
        long expiration = getExpiration(expireMillis);
        Object cachedAddresses = newCachedAddresses(host, ips, expiration);

        putInCache(host, cachedAddresses);
        addToExpirySet(cachedAddresses);
    }

    private static long getExpiration(long expireMillis) {
        return expireMillis == NEVER_EXPIRATION ? NEVER_EXPIRATION : System.nanoTime() + expireMillis * 1_000_000;
    }

    private static Object newCachedAddresses(String host, String[] ips, long expiration)
            throws ClassNotFoundException, UnknownHostException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return getConstructorOfInetAddressCachedAddresses().newInstance(host, CacheUtilCommons.toInetAddressArray(host, ips), expiration);
    }

    private static Constructor<?> getConstructorOfInetAddressCachedAddresses() throws ClassNotFoundException {
        try {
            return Class.forName(INET_ADDRESS_CACHED_ADDRESSES_CLASS_NAME).getDeclaredConstructors()[0];
        } catch (ClassNotFoundException e) {
            throw new ClassNotFoundException("InetAddress$CachedAddresses class not found", e);
        }
    }

    private static void putInCache(String host, Object cachedAddresses) throws NoSuchFieldException, IllegalAccessException {
        getCacheOfInetAddress().put(host, cachedAddresses);
    }

    private static void addToExpirySet(Object cachedAddresses) throws NoSuchFieldException, IllegalAccessException {
        getExpirySetOfInetAddress().add(cachedAddresses);
    }

    public static void removeInetAddressCache(String host) throws NoSuchFieldException, IllegalAccessException {
        getCacheOfInetAddress().remove(host);
        removeHostFromExpirySet(host);
    }

    private static void removeHostFromExpirySet(String host) throws NoSuchFieldException, IllegalAccessException {
        for (Iterator<Object> iterator = getExpirySetOfInetAddress().iterator(); iterator.hasNext(); ) {
            Object cachedAddresses = iterator.next();
            if (getHostOfInetAddressCachedAddresses(cachedAddresses).equals(host)) {
                iterator.remove();
            }
        }
    }

    private static String getHostOfInetAddressCachedAddresses(Object cachedAddresses)
            throws NoSuchFieldException, IllegalAccessException {
        return getHostFieldOfInetAddressCachedAddresses().get(cachedAddresses).toString();
    }

    private static Field getHostFieldOfInetAddressCachedAddresses() throws NoSuchFieldException {
        Field hostField = null;
        try {
            hostField = Class.forName(INET_ADDRESS_CACHED_ADDRESSES_CLASS_NAME).getDeclaredField("host");
        } catch (ClassNotFoundException e) {
            throw new NoSuchFieldException("host field not found in InetAddress$CachedAddresses");
        }
        hostField.setAccessible(true);
        return hostField;
    }

    @SuppressWarnings("unchecked")
    private static ConcurrentMap<String, Object> getCacheOfInetAddress()
            throws NoSuchFieldException, IllegalAccessException {
        return (ConcurrentMap<String, Object>) getCacheAndExpirySetOfInetAddress()[0];
    }

    @SuppressWarnings("unchecked")
    private static ConcurrentSkipListSet<Object> getExpirySetOfInetAddress()
            throws NoSuchFieldException, IllegalAccessException {
        return (ConcurrentSkipListSet<Object>) getCacheAndExpirySetOfInetAddress()[1];
    }

    private static Object[] getCacheAndExpirySetOfInetAddress()
            throws NoSuchFieldException, IllegalAccessException {
        Field cacheField = InetAddress.class.getDeclaredField("cache");
        cacheField.setAccessible(true);

        Field expirySetField = InetAddress.class.getDeclaredField("expirySet");
        expirySetField.setAccessible(true);

        return new Object[]{cacheField.get(InetAddress.class), expirySetField.get(InetAddress.class)};
    }

    public static void clearInetAddressCache() throws NoSuchFieldException, IllegalAccessException {
        getCacheOfInetAddress().clear();
        getExpirySetOfInetAddress().clear();
    }

    private CacheUtil_J9() {
    }
}
