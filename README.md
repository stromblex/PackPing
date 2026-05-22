## Local Config (`config/packping.json`)

```json
{
  "updateUrl": "",
  "localVersion": "0.0.1",
  "delay": 3000
}
```

## Server JSON

```json
[
  {
    "minecraft": "1.21",
    "version": "1.0.0",
    "download": "https://example.com/modpack-1.0.0.zip",
    "changelog": "New biomes added\nPerformance fixes\nNew mods included",

    "toast": {
      "title": "§eMC 1.21.1 is out!",
      "subtitle": "§7New modpack version coming soon"
    },

    "settings": {
      "notifications": {
        "checkOnStartup": true,
        "showFullscreen": true,
        "showChat": true,
        "showToast": true
      },
      "fullscreen": {
        "title": "Update Available",
        "downloadButton": "Download",
        "skipButton": "Skip",
        "closeGameAfterDownload": false
      },
      "chat": {
        "chatTitle": "§e▶ Modpack update available!",
        "chatVersionText": "§7Version: §f%current% §7→ §a%latest%",
        "chatChangesText": "§7Changes: §f%changelog%",
        "chatDownloadText": "§7Download:"
      }
    }
  },
]
```

## Build

```
./gradlew :fabric:build
./gradlew :neoforge:build
```

