package net.kdt.pojavlaunch.modloaders.modpacks.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ModrinthApi implements ModpackApi {

    private static final String BASE_URL = "https://api.modrinth.com/v2";
    private static final Gson GSON = new Gson();

    private final ApiHandler mApiHandler;

    public ModrinthApi() {
        mApiHandler = new ApiHandler(BASE_URL);
    }

    @Override
    public SearchResult searchMod(SearchFilters searchFilters, SearchResult previousPageResult) {
        ModrinthSearchResult modrinthSearchResult = (ModrinthSearchResult) previousPageResult;
        HashMap<String, Object> params = new HashMap<>();

        // Build the facets filters
        StringBuilder facetString = new StringBuilder();
        facetString.append("[");
        facetString.append(String.format("[\"project_type:%s\"]", searchFilters.isModpack ? "modpack" : "mod"));
        if (searchFilters.mcVersion != null && !searchFilters.mcVersion.isEmpty())
            facetString.append(String.format(",[\"versions:%s\"]", searchFilters.mcVersion));
        facetString.append("]");
        params.put("facets", facetString.toString());
        params.put("query", searchFilters.name.replace(' ', '+'));
        params.put("limit", 50);
        params.put("index", "relevance");
        if (modrinthSearchResult != null)
            params.put("offset", modrinthSearchResult.previousOffset);

        try {
            JsonObject response = mApiHandler.get("search", params, JsonObject.class);
            if (response == null) return null;
            JsonArray responseHits = response.getAsJsonArray("hits");
            if (responseHits == null) return null;

            ModItem[] items = new ModItem[responseHits.size()];
            for (int i = 0; i < responseHits.size(); ++i) {
                JsonObject hit = responseHits.get(i).getAsJsonObject();
                items[i] = new ModItem(
                        Constants.SOURCE_MODRINTH,
                        hit.get("project_type").getAsString().equals("modpack"),
                        hit.get("project_id").getAsString(),
                        hit.get("title").getAsString(),
                        hit.get("description").getAsString(),
                        hit.get("icon_url").getAsString()
                );
            }
            if (modrinthSearchResult == null) modrinthSearchResult = new ModrinthSearchResult();
            modrinthSearchResult.previousOffset += responseHits.size();
            modrinthSearchResult.results = items;
            modrinthSearchResult.totalResultCount = response.get("total_hits").getAsInt();
            return modrinthSearchResult;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ModDetail getModDetails(ModItem item) {
        try {
            JsonArray response = mApiHandler.get(String.format("project/%s/version", item.id), JsonArray.class);
            if (response == null) return null;

            String[] names = new String[response.size()];
            String[] mcNames = new String[response.size()];
            String[] urls = new String[response.size()];
            String[] hashes = new String[response.size()];

            for (int i = 0; i < response.size(); ++i) {
                JsonObject version = response.get(i).getAsJsonObject();
                names[i] = version.get("name").getAsString();
                mcNames[i] = version.get("game_versions").getAsJsonArray().get(0).getAsString();
                urls[i] = version.get("files").getAsJsonArray().get(0).getAsJsonObject().get("url").getAsString();

                // Assume there may not be hashes, in case the API changes
                JsonObject hashesMap = version.getAsJsonArray("files").get(0).getAsJsonObject()
                        .get("hashes").getAsJsonObject();
                if (hashesMap == null || hashesMap.get("sha1") == null) {
                    hashes[i] = null;
                    continue;
                }

                hashes[i] = hashesMap.get("sha1").getAsString();
            }

            return new ModDetail(item, names, mcNames, urls, hashes);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ModLoader installMod(ModDetail modDetail, int selectedVersion) throws IOException {
        //TODO considering only modpacks for now
        return ModpackInstaller.installModpack(modDetail, selectedVersion, this::installMrpack);
    }

    private static ModLoader createInfo(ModrinthIndex modrinthIndex) {
        if (modrinthIndex == null) return null;
        Map<String, String> dependencies = modrinthIndex.dependencies;
        String mcVersion = dependencies.get("minecraft");
        if (mcVersion == null) return null;
        String modLoaderVersion;
        if ((modLoaderVersion = dependencies.get("forge")) != null) {
            return new ModLoader(ModLoader.MOD_LOADER_FORGE, modLoaderVersion, mcVersion);
        }
        if ((modLoaderVersion = dependencies.get("fabric-loader")) != null) {
            return new ModLoader(ModLoader.MOD_LOADER_FABRIC, modLoaderVersion, mcVersion);
        }
        if ((modLoaderVersion = dependencies.get("quilt-loader")) != null) {
            return new ModLoader(ModLoader.MOD_LOADER_QUILT, modLoaderVersion, mcVersion);
        }
        return null;
    }

    private ModLoader installMrpack(File mrpackFile, File instanceDestination) throws IOException {
        try (ZipFile modpackZipFile = new ZipFile(mrpackFile)) {
            ModrinthIndex modrinthIndex = GSON.fromJson(new JsonReader(new FileReader(modpackZipFile.getEntry("modrinth.index.json").getFile())), ModrinthIndex.class);

            ModDownloader modDownloader = new ModDownloader(instanceDestination);
            Set<String> downloadedFiles = new HashSet<>();
            for (ModrinthIndex.ModrinthIndexFile indexFile : modrinthIndex.files) {
                if (downloadedFiles.contains(indexFile.path)) continue;
                downloadedFiles.add(indexFile.path);
                modDownloader.submitDownload(indexFile.fileSize, indexFile.path, indexFile.hashes.sha1, indexFile.downloads);
            }
            modDownloader.awaitFinish(new DownloaderProgressWrapper(R.string.modpack_download_downloading_mods, ProgressLayout.INSTALL_MODPACK));

            ProgressLayout.setProgress(ProgressLayout.INSTALL_MODPACK, 0, R.string.modpack_download_applying_overrides, 1, 2);
            unzipEntry(modpackZipFile, "overrides/", instanceDestination);
            ProgressLayout.setProgress(ProgressLayout.INSTALL_MODPACK, 50, R.string.modpack_download_applying_overrides, 2, 2);
            unzipEntry(modpackZipFile, "client-overrides/", instanceDestination);
            return createInfo(modrinthIndex);
        }
    }

    private void unzipEntry(ZipFile zipFile, String path, File destination) throws IOException {
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (!entry.getName().startsWith(path)) continue;

            File entryDestination = new File(destination, entry.getName().substring(path.length()));
            if (!entry.isDirectory()) {
                entryDestination.getParentFile().mkdirs();
                try (InputStream in = zipFile.getInputStream(entry);
                     FileOutputStream out = new FileOutputStream(entryDestination)) {

                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        out.write(buffer, 0, len);
                    }
                }
            } else {
                entryDestination.mkdirs();
            }
        }
    }

    static class ModrinthSearchResult extends SearchResult {
        int previousOffset;
    }
}
