package com.meraj.rxjava_demo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserService {
    private final ArrayList<User> allUsers;

    public UserService() {
        allUsers = new ArrayList<>();

        allUsers.add(new User("skype", UserSecurityStatus.GUEST));
        allUsers.add(new User("viber", UserSecurityStatus.MODERATOR));
        allUsers.add(new User("whatsapp", UserSecurityStatus.ADMINISTRATOR));
        allUsers.add(new User("facebook", UserSecurityStatus.GUEST));
        allUsers.add(new User("duo", UserSecurityStatus.MODERATOR));
    }

    public List<User> fetchUserList() {
        return Collections.unmodifiableList(allUsers);
    }




}


