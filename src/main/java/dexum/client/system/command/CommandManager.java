package dexum.client.system.command;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandManager {
    @Getter
    private static final List<Command> commands = new ArrayList<>();

    @Getter
    private static String prefix = ".";

    private static final Minecraft mc = Minecraft.getInstance();

    public static void register(Command command) {
        commands.add(command);
    }

    public static void register(Command... cmds) {
        commands.addAll(Arrays.asList(cmds));
    }

    public static Command getCommand(String name) {
        return commands.stream()
                .filter(cmd -> cmd.isAlias(name))
                .findFirst()
                .orElse(null);
    }

    public static boolean handleMessage(String message) {
        if (!message.startsWith(prefix)) return false;

        String input = message.substring(prefix.length()).trim();
        if (input.isEmpty()) return false;

        String[] parts = input.split(" ");
        String commandName = parts[0];
        String[] args = Arrays.copyOfRange(parts, 1, parts.length);

        Command command = getCommand(commandName);

        if (command == null) {
            sendError("Команда не найдена: " + commandName);
            sendMessage("Используйте §b.help§7 для списка команд");
            return true;
        }

        try {
            command.execute(args);
        } catch (Exception e) {
            sendError("Ошибка при выполнении команды: " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }

    private static void sendMessage(String message) {
        if (mc.player != null) {
            mc.player.sendSystemMessage(Component.literal("§7[§bDexum§7] §r" + message));
        }
    }

    private static void sendError(String message) {
        if (mc.player != null) {
            mc.player.sendSystemMessage(Component.literal("§7[§bDexum§7] §c" + message));
        }
    }

    public static void setPrefix(String newPrefix) {
        prefix = newPrefix;
    }

    public static List<String> getSuggestions(String message) {
        if (!message.startsWith(prefix)) return List.of();

        String input = message.substring(prefix.length());
        String[] parts = input.split(" ", -1);

        if (parts.length == 1) {
            String partial = parts[0].toLowerCase();
            return commands.stream()
                    .map(Command::getName)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .map(name -> prefix + name)
                    .sorted()
                    .toList();
        }

        String commandName = parts[0];
        Command command = getCommand(commandName);
        if (command == null) return List.of();

        String[] args = Arrays.copyOfRange(parts, 1, parts.length);
        List<String> suggestions = command.getSuggestions(args);

        String baseCommand = prefix + commandName + " ";
        String currentArgs = String.join(" ", Arrays.copyOfRange(args, 0, args.length - 1));
        if (!currentArgs.isEmpty()) currentArgs += " ";

        String finalPrefix = baseCommand + currentArgs;
        return suggestions.stream().map(s -> finalPrefix + s).toList();
    }
}
