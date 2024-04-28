package net.kdt.pojavlaunch.modloaders;

import net.kdt.pojavlaunch.utils.DownloadUtils;
import net.kdt.pojavlaunch.utils.OptiFineUtils;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import java.util.ArrayList;
import java.util.List;

/**
 * A class that scrapes the OptiFine website for available versions and returns them as an {@link OptiFineUtils.OptiFineVersions} object.
 */
public class OptiFineScraper implements DownloadUtils.ParseCallback<OptiFineUtils.OptiFineVersions> {

    private final OptiFineUtils.OptiFineVersions versions = new OptiFineUtils.OptiFineVersions();
    private List<OptiFineUtils.OptiFineVersion> currentVersionList;
    private String currentMinecraftVersion;

    @Override
    public OptiFineUtils.OptiFineVersions process(String input) throws DownloadUtils.ParseException {
        HtmlCleaner htmlCleaner = new HtmlCleaner();
        TagNode rootNode = htmlCleaner.clean(input);
        traverseTagNode(rootNode);
        insertVersionContent(null);
        if (versions.optifineVersions.size() < 1 || versions.minecraftVersions.size() < 1) {
            throw new DownloadUtils.ParseException(null);
        }
        return versions;
    }

    private void traverseTagNode(TagNode tagNode) {
        if (isDownloadLine(tagNode) && currentMinecraftVersion != null) {
            traverseDownloadLine(tagNode);
        } else if (isMinecraftVersionTag(tagNode)) {
            insertVersionContent(tagNode);
        } else {
            for (TagNode childNode : tagNode.getChildTags()) {
                traverseTagNode(childNode);
            }
        }
    }

    private boolean isDownloadLine(TagNode tagNode) {
        return "tr".equals(tagNode.getName()) &&
                tagNode.hasAttribute("class") &&
                tagNode.getAttributeByName("class").startsWith("downloadLine");
    }

    private boolean isMinecraftVersionTag(TagNode tagNode) {
        return "h2".equals(tagNode.getName()) &&
                tagNode.getText().toString().startsWith("Minecraft ");
    }

    private void traverseDownloadLine(TagNode tagNode) {
        OptiFineUtils.OptiFineVersion optiFineVersion = new OptiFineUtils.OptiFineVersion();
        optiFineVersion.setMinecraftVersion(currentMinecraftVersion);
        for (TagNode subNode : tagNode.getChildTags()) {
            if (!"td".equals(subNode.getName())) continue;
            switch (subNode.getAttributeByName("class")) {
                case "colFile":
                    optiFineVersion.setVersionName(subNode.getText().toString());
                    break;
                case "colMirror":
                    optiFineVersion.setDownloadUrl(getLinkHref(subNode));
            }
        }
        currentVersionList.add(optiFineVersion);
    }

    private String getLinkHref(TagNode parent) {
        for (TagNode subNode : parent.getChildTags()) {
            if ("a".equals(subNode.getName()) && subNode.hasAttribute("href")) {
                return subNode.getAttributeByName("href").replace("http://", "https://");
            }
        }
        return null;
    }

    private void insertVersionContent(TagNode tagNode) {
        if (currentVersionList != null && currentMinecraftVersion != null) {
            versions.minecraftVersions.add(currentMinecraftVersion);
            versions.optifineVersions.add(currentVersionList);
        }
        if (tagNode != null) {
            currentMinecraftVersion = tagNode.getText().toString();
            currentVersionList = new ArrayList<>();
        }
    }
}
