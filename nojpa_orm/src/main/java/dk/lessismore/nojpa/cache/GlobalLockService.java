package dk.lessismore.nojpa.cache;

import dk.lessismore.nojpa.db.methodquery.MQL;
import dk.lessismore.nojpa.reflection.db.model.ModelObject;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: seb
 */
public class GlobalLockService {

    final private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(GlobalLockService.class);

    private final static GlobalLockService me = new GlobalLockService();

    private static Calendar lastLockTime = Calendar.getInstance();
    private static Calendar lastRemoteLockTime = Calendar.getInstance();

    private GlobalLockService() { }

    public static GlobalLockService getInstance() {
        return me;
    }

    public interface LockedExecutor<T extends ModelObjectInterface> {
        void execute(T moi) throws Exception;
    }

    public void lockAndRun(ModelObjectInterface moi, LockedExecutor executor) throws Exception {
        lock(moi.getInterface().getName() + ":" + moi, moi, executor);
    }

    public void lockAndRun(String lockID, LockedExecutor executor) throws Exception {
        lock(lockID, null, executor);
    }

    static final ConcurrentHashMap<String, LockObject> mapOfIDs = new ConcurrentHashMap<String, LockObject>();

    private static class LockObject {
        final String lockID;
        Calendar creationDate;

        public LockObject(String lockID) {
            this.lockID = lockID;
            creationDate = Calendar.getInstance();
        }

    }

    private void lock(String lockID, ModelObjectInterface moi, LockedExecutor executor) throws Exception {

        LockObject lockObject = null;
        boolean locked = true;
        lastLockTime = Calendar.getInstance();
        while (locked) {
            synchronized (mapOfIDs) {
                lockObject = mapOfIDs.get(lockID);
                if (lockObject == null) {
                    locked = false;
                    lockObject = new LockObject(lockID);
                    mapOfIDs.put(lockID, lockObject);
                } else {
                    Calendar oneMin = Calendar.getInstance();
                    oneMin.add(Calendar.MINUTE, -1);
                    if (lockObject.creationDate.before(oneMin)) {
                        log.error("LockID(" + lockID + ") has been locked since(" + lockObject.creationDate.getTime() + ") ... Now removing lock!!! ");
                        locked = false;
                        mapOfIDs.remove(lockID);
                    }
                }
            }
            if (locked) {
                try {
                    synchronized (Thread.currentThread()) {
                        Thread.currentThread().wait(5);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        Exception toThrow = null;
        synchronized (lockObject.lockID) {
            log.debug("LOCKED (local) lockID(" + lockID + ")");

            try {
                ObjectCacheRemote.takeLock(lockObject.lockID);
                log.debug("LOCKED (remote) lockID(" + lockID + ")");
                if (moi != null) {
                    ModelObjectInterface modelObjectInterface = MQL.selectByID(((ModelObject) moi).getInterface(), moi.getObjectID());
                    executor.execute(modelObjectInterface);
                } else {
                    executor.execute(null);
                }

            } catch (Exception e) {
                toThrow = e;
            } finally {
                log.debug("LOCKED (-----release----) lockID(" + lockID + ")");
                ObjectCacheRemote.releaseLock(lockObject.lockID);
            }
        }
        synchronized (mapOfIDs) {
            mapOfIDs.remove(lockObject.lockID);
        }
        synchronized (Thread.currentThread()) {
            Thread.currentThread().notifyAll();
        }
        if (toThrow != null) {
            throw toThrow;
        }
    }

    public void lockFromRemote(String lockID) {
        log.debug("lockFromRemote(" + lockID + ")");
        LockObject lockObject;
        boolean locked = true;
        lastRemoteLockTime = Calendar.getInstance();
        while (locked) {
            synchronized (mapOfIDs) {
                lockObject = mapOfIDs.get(lockID);
                if (lockObject == null) {
                    locked = false;
                    lockObject = new LockObject(lockID);
                    mapOfIDs.put(lockID, lockObject);
                }
            }
            if (locked) {
                try {
                    synchronized (Thread.currentThread()) {
                        Thread.currentThread().wait(5);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void unlockFromRemote(String lockID) {
        log.debug("unlockFromRemote(" + lockID + ")");
        LockObject lockObject;
        synchronized (mapOfIDs) {
            lockObject = mapOfIDs.get(lockID);
            if (lockObject == null) {
                log.error("unlockFromRemote:We want to remove lockID(" + lockID + ") ... but it is already gone - should not be able to happen in a stable env.");
            } else {
                mapOfIDs.remove(lockID);
                synchronized (Thread.currentThread()) {
                    Thread.currentThread().notifyAll();
                }
            }
        }
    }

    private static List<MessageListener> mlList = new ArrayList<MessageListener>();

    public void addListener(MessageListener ml) {
        mlList.add(ml);
    }

    public void sendMessage(String message) {
        ObjectCacheRemote.sendMessage(message);
    }

    public void gotMessage(String message) {
        for (int i = 0; i < mlList.size(); i++) {
            try {
                mlList.get(i).gotMessage(message);
            } catch (Exception e) {
                log.error("Some exception in calling MessageListener: " + e, e);
            }
        }
    }

    public static Calendar getLastLockTime() {
        return lastLockTime;
    }

    public static Calendar getLastRemoteLockTime() {
        return lastRemoteLockTime;
    }
}
