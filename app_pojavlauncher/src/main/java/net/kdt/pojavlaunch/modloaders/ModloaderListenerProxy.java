package net.kdt.pojavlaunch.modloaders;

import java.io.File;

/**
 * A proxy class for {@link ModloaderDownloadListener} that stores the result of a Modloader download and forwards the result to the attached listener.
 */
public class ModloaderListenerProxy implements ModloaderDownloadListener {
    /**

