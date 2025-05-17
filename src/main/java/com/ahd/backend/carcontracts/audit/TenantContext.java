package com.ahd.backend.carcontracts.audit;


public class TenantContext {
    private static final ThreadLocal<Long> company = new ThreadLocal<>();
    private static final ThreadLocal<Long> user    = new ThreadLocal<>();
    public static void setCompany(Long c){ company.set(c); }
    public static Long   getCompany(){ return company.get(); }
    public static void setUser(Long u){ user.set(u); }
    public static Long   getUser(){ return user.get(); }
}
