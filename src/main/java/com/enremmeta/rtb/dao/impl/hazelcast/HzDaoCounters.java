package com.enremmeta.rtb.dao.impl.hazelcast;

import com.enremmeta.rtb.dao.DaoCounters;

public class HzDaoCounters implements DaoCounters {

    private final HazelcastService svc;

    public HzDaoCounters(HazelcastService svc) {
        super();
        this.svc = svc;
    }

    @Override
    public long get(String key) {
        return svc.getHazelcastInstance().getAtomicLong(key).get();
    }

    @Override
    public void set(String key, long val) {
        svc.getHazelcastInstance().getAtomicLong(key).set(val);

    }

    @Override
    public long getAndSet(String key, long val) {
        return svc.getHazelcastInstance().getAtomicLong(key).getAndSet(val);
    }

    @Override
    public long addAndGet(String key, long val) {
        return svc.getHazelcastInstance().getAtomicLong(key).addAndGet(val);
    }

}
