package net.kdt.pojavlaunch.value.launcherprofiles;

import androidx.annotation.Keep;

@Keep
public class MinecraftSelectedUser {

    // Use private access modifier for encapsulation
    private String account;
    private String profile;

    // Add a constructor to initialize the fields
    public MinecraftSelectedUser(String account, String profile) {
        this.account = account;
        this.profile = profile;
    }

    // Generate getter and setter methods for the fields
    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }
}
