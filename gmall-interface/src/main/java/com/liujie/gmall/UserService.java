package com.liujie.gmall;

import java.util.List;

public interface UserService {
    public void addUser(UserInfo userInfo);

    public UserInfo login(UserInfo userInfo);

    public List<UserAddress> getUserAddressList(String userId);

    public UserInfo verify(String userId);
}
