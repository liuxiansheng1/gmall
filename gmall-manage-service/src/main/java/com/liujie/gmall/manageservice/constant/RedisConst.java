package com.liujie.gmall.manageservice.constant;

public class RedisConst {
    public static final String SKU_PREFIX =  "sku:";
    public static final String SKU_SUFFIX = ":info";
    public static final int SKULOCK_EXPIRE_PX= 10000;
    public static final int SKUKEY_TIMEOUT = 24*60*60;
}
