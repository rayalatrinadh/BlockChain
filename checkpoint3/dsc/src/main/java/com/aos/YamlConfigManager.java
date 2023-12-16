package com.aos;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class YamlConfigManager {

    private static final String FILE_PATH = "dsc-config.yaml";
    private static final String FILE_PATH1 = "dsc-key.yaml";

    public YamlConfigManager() {
        try {
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            Yaml yaml = new Yaml(options);
            File configFile = new File(FILE_PATH);
            Map<String, Object> config;

            if (configFile.exists()) {
                try (FileInputStream inputStream = new FileInputStream(configFile)) {
                    config = (Map<String, Object>) yaml.load(inputStream);
                    if (config == null) {
                        config = createDefaultConfig();
                    }
                }
            } else {
                config = createDefaultConfig();
            }

            try (FileWriter writer = new FileWriter(configFile)) {
                yaml.dump(config, writer);
            }

            DumperOptions keyoptions = new DumperOptions();
            keyoptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            Yaml keyyaml = new Yaml(keyoptions);
            File keyconfigFile = new File(FILE_PATH1);
            Map<String, Object> keyconfig;

            if (keyconfigFile.exists()) {
                try (FileInputStream inputStream = new FileInputStream(keyconfigFile)) {
                    keyconfig = (Map<String, Object>) keyyaml.load(inputStream);
                    if (keyconfig == null) {
                        keyconfig = keycreateDefaultConfig();
                    }
                }
            } else {
                keyconfig = keycreateDefaultConfig();
            }

            try (FileWriter writer = new FileWriter(keyconfigFile)) {
                keyyaml.dump(keyconfig, writer);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws IOException {
        try {
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            Yaml yaml = new Yaml(options);
            File configFile = new File(FILE_PATH);
            Map<String, Object> config;

            if (configFile.exists()) {
                try (FileInputStream inputStream = new FileInputStream(configFile)) {
                    config = (Map<String, Object>) yaml.load(inputStream);
                    if (config == null) {
                        config = createDefaultConfig();
                    }
                }
            } else {
                config = createDefaultConfig();
            }

            try (FileWriter writer = new FileWriter(configFile)) {
                yaml.dump(config, writer);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        YamlConfigManager y = new YamlConfigManager();
        // String res = getConfigValue("validator","proof_pom", "threads_hash");
        // System.out.println("res=" + res);
        // updateConfigValue("wallet", "public_key", "newPublicKeyValue");
        // updateConfigValue("validator", "fingerprint", "newFingerprintValue");
        // updatePrivateKeyValue("wallet", "private_key", "New PrivateKey");
        // res = getPrivateKeyValue("wallet", "private_key");
        // System.out.println("private keyres=" + res);

    }

    private static Map<String, Object> keycreateDefaultConfig() {
        Map<String, Object> keyconfig = new LinkedHashMap<>();
        keyconfig.put("wallet", Map.of("private_key", "")); // Empty string for public_key initially

        return keyconfig;
    }

    public static Map<String, Object> createDefaultConfig() {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("wallet", Map.of("public_key", "")); // Empty string for public_key initially
        config.put("pool", Map.of(
                "server", "127.0.0.1",
                "port", 10001,
                "threads", 2));
        config.put("blockchain", Map.of(
                "server", "127.0.0.1",
                "port", 10002,
                "threads", 2));
        config.put("metronome", Map.of(
                "server", "127.0.0.1",
                "port", 10003,
                "threads", 2));
        config.put("validator", Map.of(
                "fingerprint", "", // Empty string for fingerprint initially
                "public_key", "", // Empty string for public_key initially
                "server", "127.0.0.1",
                "port", 10004,
                "proof_pow", Map.of(
                        "enable", true,
                        "threads_hash", 2),
                "proof_pom", Map.of(
                        "enable", false,
                        "threads_hash", 2,
                        "memory", "1G"),
                "proof_pos", Map.of(
                        "enable", false,
                        "threads_hash", 2,
                        "disk", "10G",
                        "buckets", 256,
                        "bucket_size", 32768,
                        "threads_io", 1,
                        "vault", "~/dsc-pos.vault")));
        // config.put();

        return config;
    }

    public static String getConfigValue(String mainKey, String subKey) {
        Yaml yaml = new Yaml(new Constructor(Map.class));
        File configFile = new File(FILE_PATH);
        if (configFile.exists()) {
            try (FileInputStream inputStream = new FileInputStream(configFile)) {
                Map<String, Map<String, Object>> config = (Map<String, Map<String, Object>>) yaml.load(inputStream);
                if (config != null && config.containsKey(mainKey)) {
                    Map<String, Object> mainConfig = config.get(mainKey);
                    if (mainConfig != null && mainConfig.containsKey(subKey)) {
                        return mainConfig.get(subKey).toString();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null; // Return null if the value is not found
    }

    public static String getConfigValue(String mainKey, String subKey, String subSubKey) {
        Yaml yaml = new Yaml(new Constructor(Map.class));
        File configFile = new File(FILE_PATH);
        if (configFile.exists()) {
            try (FileInputStream inputStream = new FileInputStream(configFile)) {
                Map<String, Map<String, Map<String, Object>>> config = (Map<String, Map<String, Map<String, Object>>>) yaml
                        .load(inputStream);
                if (config != null && config.containsKey(mainKey)) {
                    Map<String, Map<String, Object>> mainConfig = config.get(mainKey);
                    if (mainConfig != null && mainConfig.containsKey(subKey)) {
                        Map<String, Object> subConfig = mainConfig.get(subKey);
                        if (subConfig != null && subConfig.containsKey(subSubKey)) {
                            return subConfig.get(subSubKey).toString();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null; // Return null if the value is not found
    }

    public static String getPrivateKeyValue(String mainKey, String subKey) {
        Yaml yaml = new Yaml(new Constructor(Map.class));
        File configFile = new File(FILE_PATH1);
        if (configFile.exists()) {
            try (FileInputStream inputStream = new FileInputStream(configFile)) {
                Map<String, Map<String, Object>> config = (Map<String, Map<String, Object>>) yaml.load(inputStream);
                if (config != null && config.containsKey(mainKey)) {
                    Map<String, Object> mainConfig = config.get(mainKey);
                    if (mainConfig != null && mainConfig.containsKey(subKey)) {
                        return mainConfig.get(subKey).toString();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null; // Return null if the value is not found
    }

    public static void updateConfigValue(String mainKey, String subKey, String newValue) throws IOException {
        Yaml yaml = new Yaml(new Constructor(Map.class));
        File configFile = new File(FILE_PATH);
        Map<String, Map<String, Object>> config;

        if (configFile.exists()) {
            try (FileInputStream inputStream = new FileInputStream(configFile)) {
                config = (Map<String, Map<String, Object>>) yaml.load(inputStream);
                if (config != null && config.containsKey(mainKey)) {
                    Map<String, Object> mainConfig = config.get(mainKey);
                    if (mainConfig != null) {
                        mainConfig.put(subKey, newValue);
                        // Write updated configuration back to file
                        DumperOptions options = new DumperOptions();
                        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                        yaml = new Yaml(options);
                        try (FileWriter writer = new FileWriter(configFile)) {
                            yaml.dump(config, writer);
                        }
                    }
                }
            }
        } else {
            throw new IOException("Configuration file not found.");
        }
    }

    public static void updatePrivateKeyValue(String mainKey, String subKey, String newValue) throws IOException {
        Yaml yaml = new Yaml(new Constructor(Map.class));
        File configFile = new File(FILE_PATH1);
        Map<String, Map<String, Object>> config;

        if (configFile.exists()) {
            try (FileInputStream inputStream = new FileInputStream(configFile)) {
                config = (Map<String, Map<String, Object>>) yaml.load(inputStream);
                if (config != null && config.containsKey(mainKey)) {
                    Map<String, Object> mainConfig = config.get(mainKey);
                    if (mainConfig != null) {
                        mainConfig.put(subKey, newValue);
                        // Write updated configuration back to file
                        DumperOptions options = new DumperOptions();
                        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                        yaml = new Yaml(options);
                        try (FileWriter writer = new FileWriter(configFile)) {
                            yaml.dump(config, writer);
                        }
                    }
                }
            }
        } else {
            throw new IOException("dsc-key.yaml file not found.");
        }
    }

}

// ++++++++++++++++++++++++++++++++++++//
// package com.aos;
// import java.io.File;
// import java.io.FileInputStream;
// import java.io.FileWriter;
// import java.io.IOException;
// import java.util.Map;
// import org.yaml.snakeyaml.Yaml;
// import org.yaml.snakeyaml.constructor.Constructor;

// public class YamlConfigManager {

// private static final String FILE_PATH = "dsc-config.yaml";

// public static void main(String[] args) {
// try {
// Yaml yaml = new Yaml(new Constructor(Map.class));
// File configFile = new File(FILE_PATH);
// Map<String, Object> config;

// if (configFile.exists()) {
// // Read existing file
// try (FileInputStream inputStream = new FileInputStream(configFile)) {
// config = (Map<String, Object>) yaml.load(inputStream);
// // If the file exists but is empty, initialize with default config
// if (config == null) {
// config = createDefaultConfig();
// }
// }
// } else {
// // Create default config
// config = createDefaultConfig();
// }

// // Write to file
// try (FileWriter writer = new FileWriter(configFile)) {
// yaml.dump(config, writer);
// }
// } catch (IOException e) {
// e.printStackTrace();
// } catch (ClassCastException e) {
// System.out.println("Error: The file does not have the expected format.");
// }
// }

// private static Map<String, Object> createDefaultConfig() {
// // Default configuration with an empty wallet
// return Map.of(
// "wallet", Map.of("public_key", ""),
// "pool", Map.of("server", "127.0.0.1", "port", 10001, "threads",
// 2),"blockchain",Map.of("server","127.0.0.1","port","10002","threads","2"),"metronome",Map.of("server","127.0.0.1","port","10003","threads","2"),"validator",Map.of("fingerprint","","public_key","","proof_pow",Map.of("enable","True","threads_hash","2")),"proof_pom",Map.of("enable","false","threads_hash","2","memory","1G"),"Proof_pos",Map.of("enable","false","threads_hash","2","#memory","buckets*bucket_size*128~1GB","disk","10G","buckets","256","bucket_size","32768","threads_io","1","vault","~/dsc-pos.vault")
// // ... add the rest of the configuration here ...
// );
// }
// }
