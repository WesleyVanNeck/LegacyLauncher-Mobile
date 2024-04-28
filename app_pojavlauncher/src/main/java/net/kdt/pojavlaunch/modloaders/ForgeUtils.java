package net.kdt.pojavlaunch.modloaders;

import android.content.Intent;

import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.utils.DownloadUtils;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class ForgeUtils {
    private static final String FORGE_METADATA_URL = "https://maven.minecraftforge.net/net/minecraftforge/forge/maven-metadata.xml";
    private static final String FORGE_INSTALLER_URL_TEMPLATE = "https://maven.minecraftforge.net/net/minecraftforge/forge/%s/forge-%s-installer.jar";

    public static List<String> downloadForgeVersions() throws IOException {
        SAXParser saxParser = null;
        try {
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            saxParser = parserFactory.newSAXParser();
        } catch (SAXException | ParserConfigurationException e) {
            e.printStackTrace();
            return null;
        }

        if (saxParser == null) {
            return null;
        }

        return DownloadUtils.downloadStringCached(FORGE_METADATA_URL, "forge_versions", input -> {
            ForgeVersionListHandler handler = new ForgeVersionListHandler();
            try {
                saxParser.parse(new InputSource(new StringReader(input)), handler);
                return handler.getVersions();
            } catch (SAXException | IOException e) {
                throw new DownloadUtils.ParseException(e);
            }
        });
    }

    public static String getInstallerUrl(String version) {
        return String.format(FORGE_INSTALLER_URL_TEMPLATE, version, version);
    }

    public static void addAutoInstallArgs(Intent intent, File modInstallerJar, boolean createProfile) {
        intent.putExtra("javaArgs", "-javaagent:" + Tools.DIR_DATA + "/forge_installer/forge_installer.jar"
                + (createProfile ? "=NPS" : "") + // No Profile Suppression
                " -jar " + modInstallerJar.getAbsolutePath());
    }

    public static void addAutoInstallArgs(Intent intent, File modInstallerJar, String modpackFixupId) {
        intent.putExtra("javaArgs", "-javaagent:" + Tools.DIR_DATA + "/forge_installer/forge_installer.jar"
                + "=\"" + modpackFixupId + "\"" +
                " -jar " + modInstallerJar.getAbsolutePath());
    }

    public static File downloadForgeInstaller(String version) throws IOException {
        String url = getInstallerUrl(version);
        return DownloadUtils.downloadFileCached(url, "forge_installer/" + version + "_installer.jar");
    }
}

class ForgeVersionListHandler extends DefaultHandler {
    private List<String> versions;

    public List<String> getVersions() {
        return versions;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.equalsIgnoreCase("version")) {
            versions.add(attributes.getValue("id"));
        }
    }
}
