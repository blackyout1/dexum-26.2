package dexum.client.system.config;

import lombok.Getter;

import java.io.File;
import java.util.Objects;

@Getter
public class Config {
    private final String name;
    private final File file;

    public Config(String name) {
        this.name = name;
        this.file = new File(ConfigManager.getConfigsDir(), name + ".json");
    }

    public boolean save() {
        return ConfigManager.saveConfig(this);
    }

    public boolean load() {
        return ConfigManager.loadConfig(this);
    }

    public boolean delete() {
        return file.delete();
    }

    public boolean exists() {
        return file.exists();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Config other)) return false;
        return Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
