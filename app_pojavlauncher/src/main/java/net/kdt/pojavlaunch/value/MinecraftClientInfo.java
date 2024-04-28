package net.kdt.pojavlaunch.value;

import androidx.annotation.Keep;

import java.util.Objects;

@Keep
public class MinecraftClientInfo {

    private final String sha1;
    private final int size;
    private final String url;

    public MinecraftClientInfo(String sha1, int size, String url) {
        this.sha1 = sha1;
        this.size = size;
        this.url = url;
    }

    public String getSha1() {
        return sha1;
    }

    public int getSize() {
        return size;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "MinecraftClientInfo{" +
                "sha1='" + sha1 + '\'' +
                ", size=" + size +
                ", url='" + url + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MinecraftClientInfo)) return false;
        MinecraftClientInfo that = (MinecraftClientInfo) o;
        return getSize() == that.getSize() && Objects.equals(getSha1(), that.getSha1()) && Objects.equals(getUrl(), that.getUrl());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSha1(), getSize(), getUrl());
    }
}
