package dexum.client.system.command;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.List;

@Getter
public abstract class Command {
    protected final Minecraft mc = Minecraft.getInstance();

    private final String name;
    private final String description;
    private final List<String> aliases;
    private final String usage;

    public Command(String name, String description, String usage, String... aliases) {
        this.name = name;
        this.description = description;
        this.usage = usage;
        this.aliases = Arrays.asList(aliases);
    }

    public abstract void execute(String[] args);

    public List<String> getSuggestions(String[] args) {
        return List.of();
    }

    protected void sendMessage(String message) {
        if (mc.player != null) {
            mc.player.sendSystemMessage(Component.literal("§7[§bDexum§7] §r" + message));
        }
    }

    protected void sendError(String message) {
        if (mc.player != null) {
            mc.player.sendSystemMessage(Component.literal("§7[§bDexum§7] §c" + message));
        }
    }

    protected void sendUsage() {
        sendMessage("§7Использование: §f" + usage);
    }

    public boolean isAlias(String alias) {
        return name.equalsIgnoreCase(alias) || aliases.stream().anyMatch(a -> a.equalsIgnoreCase(alias));
    }
}
