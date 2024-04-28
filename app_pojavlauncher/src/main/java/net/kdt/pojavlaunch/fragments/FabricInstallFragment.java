package net.kdt.pojavlaunch.fragments;

import net.kdt.pojavlaunch.modloaders.FabriclikeUtils;
import net.kdt.pojavlaunch.modloaders.ModloaderListenerProxy;

public class FabricInstallFragment extends FabriclikeInstallFragment {

    private static final String TAG = "FabricInstallFragment";
    private static ModloaderListenerProxy taskProxy;

    public FabricInstallFragment() {
        super(FabriclikeUtils.FABRIC_UTILS);
    }

    @Override
    protected ModloaderListenerProxy getTaskProxy() {
        return taskProxy;

