package net.kdt.pojavlaunch.fragments;

import net.kdt.pojavlaunch.modloaders.FabriclikeModloader;
import net.kdt.pojavlaunch.modloaders.ModloaderListenerProxy;

public class QuiltInstallFragment extends FabriclikeInstallFragment {

    public static final String TAG = "QuiltInstallFragment";
    private static ModloaderListenerProxy modloaderListenerProxy;

    public QuiltInstallFragment() {
        super(FabriclikeModloader.QUILT_UTILS);
    }

    @Override
    protected ModloaderListenerProxy getModloaderListenerProxy() {
        return modloaderListenerProxy;
    }

    @Override
    protected void setModloaderListenerProxy(ModloaderListenerProxy listenerProxy) {
        modloaderListenerProxy = listenerProxy;
    }
}
