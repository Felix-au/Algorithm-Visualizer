package com.algorithmvisualizer.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateManager {

    private static final String DOT_ENV_PATH = ".env";
    private static final String DEFAULT_GITHUB_API_URL = "https://api.github.com/repos/Felix-au/AlgoBuddy-Release-Repositiry/releases/latest";

    public static class UpdateInfo {
        public final boolean isUpdateAvailable;
        public final String latestVersion;
        public final String downloadUrl;

        public UpdateInfo(boolean isUpdateAvailable, String latestVersion, String downloadUrl) {
            this.isUpdateAvailable = isUpdateAvailable;
            this.latestVersion = latestVersion;
            this.downloadUrl = downloadUrl;
        }
    }

    public static String getCurrentVersion() {
        Properties props = new Properties();
        File envFile = new File(DOT_ENV_PATH);
        System.out.println("[UpdateManager] Reading current version from: " + envFile.getAbsolutePath());

        if (!envFile.exists()) {
            System.err.println("[UpdateManager] .env file not found at " + envFile.getAbsolutePath());
            return "0.0";
        }

        try (FileInputStream fis = new FileInputStream(DOT_ENV_PATH)) {
            props.load(fis);
            String version = props.getProperty("VERSION", "0.0");
            System.out.println("[UpdateManager] Current version detected: " + version);
            return version;
        } catch (IOException e) {
            System.err.println("[UpdateManager] Error reading .env file: " + e.getMessage());
            return "0.0";
        }
    }

    public static String getUpdateUrl() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(DOT_ENV_PATH)) {
            props.load(fis);
            String url = props.getProperty("GITHUB_RELEASE_URL", DEFAULT_GITHUB_API_URL);
            System.out.println("[UpdateManager] Update URL from .env: " + url);
            return url;
        } catch (IOException e) {
            System.out.println("[UpdateManager] Using default update URL: " + DEFAULT_GITHUB_API_URL);
            return DEFAULT_GITHUB_API_URL;
        }
    }

    public static UpdateInfo checkForUpdates() throws IOException, InterruptedException {
        String currentVersion = getCurrentVersion();
        String updateUrl = getUpdateUrl();

        System.out.println("[UpdateManager] Starting update check...");
        System.out.println("[UpdateManager]   Target URL: " + updateUrl);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(updateUrl))
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "AlgoBuddy-App") // Added user-agent as GitHub API sometimes requires it
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("[UpdateManager] Response Status Code: " + response.statusCode());

            String json = response.body();

            // If /latest returns 404, it might be because there are only pre-releases
            if (response.statusCode() == 404 && updateUrl.endsWith("/latest")) {
                System.out.println("[UpdateManager] Latest release not found. Trying general releases list...");
                String listUrl = updateUrl.substring(0, updateUrl.length() - "/latest".length());
                request = HttpRequest.newBuilder()
                        .uri(URI.create(listUrl))
                        .header("Accept", "application/vnd.github.v3+json")
                        .header("User-Agent", "AlgoBuddy-App")
                        .GET()
                        .build();
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("[UpdateManager] List Response Status Code: " + response.statusCode());
                if (response.statusCode() == 200) {
                    json = response.body();
                    if (json.trim().startsWith("[")) {
                        System.out.println("[UpdateManager] Received releases list. Parsing first entry.");
                    }
                }
            }

            if (response.statusCode() == 200) {
                System.out.println("[UpdateManager] Response Body received (first 100 chars): " +
                        (json.length() > 100 ? json.substring(0, 100) + "..." : json));

                String latestVersion = getValueFromJson(json, "tag_name");
                String downloadUrl = getBrowserDownloadUrl(json);

                System.out.println("[UpdateManager] Parsed Latest Version: " + latestVersion);
                System.out.println("[UpdateManager] Parsed Download URL: " + downloadUrl);

                boolean updateAvailable = latestVersion != null && isNewer(latestVersion, currentVersion);
                System.out.println("[UpdateManager] Update Available: " + updateAvailable);

                return new UpdateInfo(updateAvailable, latestVersion, downloadUrl);
            } else {
                System.err.println("[UpdateManager] GitHub API Error: " + json);
                throw new IOException("GitHub API returned " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("[UpdateManager] Network/Parsing Error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private static String getValueFromJson(String json, String key) {
        String pattern = "\"" + key + "\":\\s*\"([^\"]*)\"";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    private static String getBrowserDownloadUrl(String json) {
        System.out.println("[UpdateManager] Searching for .exe in assets...");
        Pattern r = Pattern.compile("\"browser_download_url\":\\s*\"([^\"]*\\.exe)\"");
        Matcher m = r.matcher(json);
        if (m.find()) {
            String url = m.group(1);
            System.out.println("[UpdateManager] Found exe: " + url);
            return url;
        }

        System.out.println("[UpdateManager] No .exe found, taking first available asset...");
        return getValueFromJson(json, "browser_download_url");
    }

    private static boolean isNewer(String latest, String current) {
        try {
            double vLatest = Double.parseDouble(latest.replaceAll("[^0-9.]", ""));
            double vCurrent = Double.parseDouble(current.replaceAll("[^0-9.]", ""));
            return vLatest > vCurrent;
        } catch (NumberFormatException e) {
            // Fallback to simple string comparison if not numeric
            return !latest.equals(current);
        }
    }
}
