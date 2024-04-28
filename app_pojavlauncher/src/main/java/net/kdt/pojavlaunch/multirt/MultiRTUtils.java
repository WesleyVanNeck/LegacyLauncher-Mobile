package net.kdt.pojavlaunch.multirt;

import static net.kdt.pojavlaunch.Tools.NATIVE_LIB_DIR;
import static org.apache.commons.io.FileUtils.listFiles;

import android.system.Os;
import android.util.Log;

import com.kdt.mcgui.ProgressLayout;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CountingInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiRTUtils {

    private static final HashMap<String, Runtime> runtimeCache = new HashMap<>();

    private static final File RUNTIME_FOLDER = new File(Tools.MULTIRT_HOME);
    private static final String JAVA_VERSION_PREFIX = "JAVA_VERSION=\"";
    private static final String OS_ARCH_PREFIX = "OS_ARCH=\"";

    public static List<Runtime> getRuntimes() {
        if (!RUNTIME_FOLDER.exists() && !RUNTIME_FOLDER.mkdirs()) {
            throw new RuntimeException("Failed to create runtime directory");
        }

        ArrayList<Runtime> runtimes = new ArrayList<>();
        File[] files = RUNTIME_FOLDER.listFiles();
        if (files != null) {
            for (File f : files) {
                runtimes.add(readRuntime(f.getName()));
            }
        } else {
            throw new RuntimeException("The runtime directory does not exist");
        }

        return runtimes;
    }

    public static String getExactJreName(int majorVersion) {
        List<Runtime> runtimes = getRuntimes();
        for (Runtime r : runtimes) {
            if (r.getJavaVersion() == majorVersion) {
                return r.getName();
            }
        }

        return null;
    }

    public static String getNearestJreName(int majorVersion) {
        List<Runtime> runtimes = getRuntimes();
        int diffFactor = Integer.MAX_VALUE;
        String result = null;
        for (Runtime r : runtimes) {
            if (r.getJavaVersion() < majorVersion) {
                continue; // lower - not useful
            }

            int currentFactor = r.getJavaVersion() - majorVersion;
            if (diffFactor > currentFactor) {
                result = r.getName();
                diffFactor = currentFactor;
            }
        }

        return result;
    }

    public static void installRuntimeNamed(String nativeLibDir, InputStream runtimeInputStream, String name) throws IOException {
        File dest = new File(RUNTIME_FOLDER, name);
        if (dest.exists()) {
            FileUtils.deleteDirectory(dest);
        }

        uncompressTarXZ(runtimeInputStream, dest);
        runtimeInputStream.close();
        unpack200(nativeLibDir, RUNTIME_FOLDER + File.separator + name);
        ProgressLayout.clearProgress(ProgressLayout.UNPACK_RUNTIME);
        readRuntime(name);
    }

    public static void postPrepare(String name) throws IOException {
        File dest = new File(RUNTIME_FOLDER, name);
        if (!dest.exists()) {
            return;
        }

        Runtime runtime = readRuntime(name);
        String libFolder = "lib";
        File libDir = new File(dest, libFolder);
        if (libDir.exists()) {
            File[] libFiles = libDir.listFiles();
            if (libFiles != null) {
                for (File libFile : libFiles) {
                    if (libFile.getName().equals("libfreetype.so.6")) {
                        File ftOut = new File(libDir, "libfreetype.so");
                        if (!libFile.renameTo(ftOut)) {
                            throw new IOException("Failed to rename freetype");
                        }
                    }
                }
            }
        }

        // Refresh libraries
        copyDummyNativeLib("libawt_xawt.so", dest, libFolder);
    }

    public static void installRuntimeNamedBinpack(InputStream universalFileInputStream, InputStream platformBinsInputStream, String name, String binpackVersion) throws IOException {
        File dest = new File(RUNTIME_FOLDER, name);
        if (dest.exists()) {
            FileUtils.deleteDirectory(dest);
        }

        installRuntimeNamedNoRemove(universalFileInputStream, dest);
        installRuntimeNamedNoRemove(platformBinsInputStream, dest);

        unpack200(NATIVE_LIB_DIR, RUNTIME_FOLDER + File.separator + name);

        File binpackVersionFile = new File(dest, "pojav_version");
        try (FileOutputStream fos = new FileOutputStream(binpackVersionFile)) {
            fos.write(binpackVersion.getBytes());
        }

        ProgressLayout.clearProgress(ProgressLayout.UNPACK_RUNTIME);

        forceReread(name);
    }

    public static String readBinpackVersion(String name) {
        File binpackVersionFile = new File(RUNTIME_FOLDER, name + File.separator + "pojav_version");
        if (binpackVersionFile.exists()) {
            try {
                return Tools.read(binpackVersionFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static void removeRuntimeNamed(String name) throws IOException {
        File dest = new File(RUNTIME_FOLDER, name);
        if (dest.exists()) {
            FileUtils.deleteDirectory(dest);
            runtimeCache.remove(name);
        }
    }

    public static File getRuntimeHome(String name) {
        File dest = new File(RUNTIME_FOLDER, name);
        if (!dest.exists() || MultiRTUtils.forceReread(name).getVersionString() == null) {
            throw new RuntimeException("Selected runtime is broken!");
        }
        return dest;
    }

    public static Runtime forceReread(String name) {
        runtimeCache.remove(name);
        return readRuntime(name);
    }

    public static Runtime readRuntime(String name) {
        Runtime runtime = runtimeCache.get(name);
        if (runtime != null) {
            return runtime;
        }

        File releaseFile = new File(RUNTIME_FOLDER, name + File.separator + "release");
        if (!releaseFile.exists()) {
            return new Runtime(name);
        }

        try {
            String content = Tools.read(releaseFile.getAbsolutePath());
            String javaVersion = Tools.extractUntilCharacter(content, JAVA_VERSION_PREFIX, '"');
            String osArch = Tools.extractUntilCharacter(content, OS_ARCH_PREFIX, '"');
            if (javaVersion != null && osArch != null) {
                String[] javaVersionSplit = javaVersion.split("\\.");
                int javaVersionInt;
                if (javaVersionSplit[0].equals("1")) {
                    javaVersionInt = Integer.parseInt(javaVersionSplit[1]);
                } else {
                    javaVersionInt = Integer.parseInt(javaVersionSplit[0]);
                }
                runtime = new Runtime(name, javaVersion, osArch, javaVersionInt);
            } else {
                runtime = new Runtime(name);
            }
        } catch (IOException e) {
            runtime = new Runtime(name);
        }

        runtimeCache.put(name, runtime);
        return runtime;
    }

    /**
     * Unpacks all .pack files into .jar Serves only for java 8, as java 9 brought project jigsaw
     * @param nativeLibraryDir The native lib path, required to execute the unpack200 binary
     * @param runtimePath The path to the runtime to walk into
     */
    private static void unpack200(String nativeLibraryDir, String runtimePath) {
        File basePath = new File(runtimePath);
        Collection<File> files = listFiles(basePath, new String[]{"pack"}, true);

        File workdir = new File(nativeLibraryDir);

        ExecutorService executor = Executors.newFixedThreadPool(files.size());
        for (File jarFile : files) {
            executor.submit(() -> {
                try {
                    Process process = new ProcessBuilder().directory(workdir)
                            .command("./libunpack200.so", "-r", jarFile.getAbsolutePath(), jarFile.getAbsolutePath().replace(".pack", ""))
                            .start();
                    process.waitFor();
                } catch (InterruptedException | IOException e) {
                    Log.e("MULTIRT", "Failed to unpack the runtime !");
                }
            });
        }
        executor.shutdown();
    }

    @SuppressWarnings("SameParameterValue")
    private static void copyDummyNativeLib(String name, File dest, String libFolder) throws IOException {
        File fileLib = new File(dest, libFolder + File.separator + name);
        FileInputStream is = new FileInputStream(new File(NATIVE_LIB_DIR, name));
        FileOutputStream os = new FileOutputStream(fileLib);
        IOUtils.copy(is, os);
        is.close();
        os.close();
    }

    private static void installRuntimeNamedNoRemove(InputStream runtimeInputStream, File dest) throws IOException {
        uncompressTarXZ(runtimeInputStream, dest);
        runtimeInputStream.close();
    }

    private static void uncompressTarXZ(final InputStream tarFileInputStream, final File dest) throws IOException {
        FileUtils.ensureDirectory(dest);

        byte[] buffer = new byte[8192];
        TarArchiveInputStream tarIn = new TarArchiveInputStream(
                new XZCompressorInputStream(tarFileInputStream)
        );
        TarArchiveEntry tarEntry = tarIn.getNextTarEntry();
        // tarIn is a TarArchiveInputStream
        while (tarEntry != null) {

            final String tarEntryName = tarEntry.getName();
            // publishProgress(null, "Unpacking " + tarEntry.getName());
            ProgressLayout.setProgress(ProgressLayout.UNPACK_RUNTIME, 100, R.string.global_unpacking, tarEntryName);

            File destPath = new File(dest, tarEntry.getName());
            FileUtils.ensureParentDirectory(destPath);
            if (tarEntry.isSymbolicLink()) {
                try {
                    // android.system.Os
                    // Libcore one support all Android versions
                    Os.symlink(tarEntry.getName(), tarEntry.getLinkName());
                } catch (Throwable e) {
                    Log.e("MultiRT", e.toString());
                }

            } else if (tarEntry.isDirectory()) {
                FileUtils.ensureDirectory(destPath);
            } else {
                CountingInputStream countingInputStream = new CountingInputStream(tarIn);
                FileUtils.copyInputStreamToFile(countingInputStream, destPath);
                long size = countingInputStream.getCount();
                if (size != tarEntry.getSize()) {
                    throw new IOException("Failed to unpack the runtime: incorrect file size");
                }
            }
            tarEntry = tarIn.getNextTarEntry();
        }
        tarIn.close();
    }
}
