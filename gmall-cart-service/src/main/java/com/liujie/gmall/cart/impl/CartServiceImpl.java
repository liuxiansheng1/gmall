package com.liujie.gmall.cart.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.liujie.gmall.*;
import com.liujie.gmall.cart.mapper.CartInfoMapper;
import com.liujie.gmall.cart.constant.Constant;
import com.liujie.gmall.config.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.*;

@Service
public class CartServiceImpl implements CartService{



    @Autowired
    CartInfoMapper cartInfoMapper;

    @Reference
    ManageService manageService;

    @Autowired
    RedisUtil redisUtil;


    public void addToCart(String skuId,String userId,Integer skuNum){

        CartInfo cartInfoQuery = new CartInfo();
        cartInfoQuery.setSkuId(skuId);
        cartInfoQuery.setUserId(userId);
        CartInfo cartInfoExist = cartInfoMapper.selectOne(cartInfoQuery);
        if(cartInfoExist!=null){
           cartInfoExist.setSkuNum(cartInfoExist.getSkuNum()+skuNum);

           cartInfoMapper.updateByPrimaryKey(cartInfoExist);


        }else{
            SkuInfo skuInfo = manageService.getSkuInfo(skuId);

            CartInfo cartInfo = new CartInfo();
            cartInfo.setSkuId(skuId);
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());

            cartInfo.setSkuNum(skuNum);
            cartInfo.setUserId(userId);

            cartInfoMapper.insertSelective(cartInfo);
            cartInfoExist=cartInfo;

        }

        String userCartKey = Constant.USER_KEY_PREFIX+userId+Constant.USER_CART_KEY_SUFFIX;
        Jedis jedis = redisUtil.getJedis();

        String cartJson = JSON.toJSONString(cartInfoExist);
        jedis.hset(userCartKey,skuId,cartJson);

        //更新过期时间
        String userInfoKey = Constant.USER_KEY_PREFIX+userId+ Constant.USER_INFO_KEY_SUFFIX;
        Long ttl = jedis.ttl(userInfoKey);
        jedis.expire(userCartKey,ttl.intValue());
        jedis.close();

    }
    public List<CartInfo> getCartList(String userId){
        Jedis jedis = redisUtil.getJedis();
        String key = Constant.USER_KEY_PREFIX+userId+Constant.USER_CART_KEY_SUFFIX;
        List<String> skuInfoList= jedis.hvals(key);

        if(skuInfoList!=null && skuInfoList.size()>0){
            List<CartInfo> cartInfoList = new ArrayList<>(skuInfoList.size());
            for (String skuJson : skuInfoList) {
                CartInfo cartInfo = JSON.parseObject(skuJson, CartInfo.class);
                cartInfoList.add(cartInfo);
            }
            cartInfoList.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    return Long.compare(Long.parseLong(o2.getId()),Long.parseLong(o1.getId()));
                }
            });
            return cartInfoList;
        }else{
            List<CartInfo> cartInfoList1 = loadCartCache(userId);
            return cartInfoList1;
        }


    }

    public List<CartInfo> loadCartCache(String userId){
        List<CartInfo> cartInfoList = cartInfoMapper.selectCartListWithCurPrice(Long.parseLong(userId));
        if(cartInfoList==null||cartInfoList.size()==0){
            return null;
        }
        Jedis jedis = redisUtil.getJedis();
        String userCartKey = Constant.USER_KEY_PREFIX+userId+Constant.USER_CART_KEY_SUFFIX;
        String userInfoKey = Constant.USER_KEY_PREFIX+userId+Constant.USER_INFO_KEY_SUFFIX;
        Map cartMap = new HashMap(cartInfoList.size());
        for (CartInfo cartInfo : cartInfoList) {
            cartMap.put(cartInfo.getSkuId(),JSON.toJSONString(cartInfo));
        }
        jedis.hmset(userCartKey,cartMap);
        Long ttl = jedis.ttl(userInfoKey);
        jedis.expire(userCartKey,ttl.intValue());
        jedis.close();
        return cartInfoList;


    }

    public List<CartInfo> mergeToCartList(List<CartInfo> cartListFromCookie,String userId){
        List<CartInfo> cartInfoDB = cartInfoMapper.selectCartListWithCurPrice(Long.parseLong(userId));
        for (CartInfo cartInfock : cartListFromCookie) {
            boolean isMatch=false;
            for (CartInfo cartInfodb : cartInfoDB) {
                if(cartInfock.getSkuId().equals(cartInfodb.getSkuName())){
                    cartInfodb.setSkuNum(cartInfodb.getSkuNum()+cartInfock.getSkuNum());
                    cartInfoMapper.updateByPrimaryKeySelective(cartInfodb);
                    isMatch=true;
                }

            }
            if(!isMatch){
                cartInfock.setUserId(userId);
                cartInfoMapper.insertSelective(cartInfock);
            }
        }

        List<CartInfo> cartInfoList = loadCartCache(userId);
        for (CartInfo cartInfo : cartInfoList) {
            for (CartInfo cartCookie : cartListFromCookie) {
                if(cartInfo.getSkuId().equals(cartCookie.getSkuId())){
                    if(cartCookie.getIsChecked().equals("1")) {  //只有勾中才赋勾选状态
                        cartInfo.setIsChecked(cartCookie.getIsChecked());
                        checkCart(cartInfo.getSkuId(), cartCookie.getIsChecked(), userId);
                    }
                }
            }
        }
        return cartInfoList;
    }

    public void checkCart(String skuId,String isChecked,String userId){
        Jedis jedis = redisUtil.getJedis();
        String userCartKey = Constant.USER_KEY_PREFIX+userId+Constant.USER_CART_KEY_SUFFIX;
        String cartJson = jedis.hget(userCartKey, skuId);
        CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
        cartInfo.setIsChecked(isChecked);
        String cartCheckedJson = JSON.toJSONString(cartInfo);
        jedis.hset(userCartKey, skuId, cartCheckedJson);

        String userCheckedKey = Constant.USER_KEY_PREFIX + userId + Constant.USER_ISCHECKED_KEY_SUFFIX;
        if(isChecked.equals("1")){
            jedis.hset(userCheckedKey,skuId,cartCheckedJson);
        }else{
            jedis.hdel(userCheckedKey,skuId);
        }


        jedis.close();
    }

    public List<CartInfo> getCartCheckedList(String userId){
        String userCheckedKey = Constant.USER_KEY_PREFIX + userId + Constant.USER_ISCHECKED_KEY_SUFFIX;
        Jedis jedis = redisUtil.getJedis();
        List<String> cartCheckedJsonList = jedis.hvals(userCheckedKey);
        List<CartInfo> cartCheckedList=new ArrayList<>(cartCheckedJsonList.size());
        for (String cartJson : cartCheckedJsonList) {
            CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
            cartCheckedList.add(cartInfo);
        }
        jedis.close();
        return cartCheckedList;

    }


}
