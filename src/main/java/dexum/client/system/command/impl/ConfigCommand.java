package dexum.client.system.command.impl;

import dexum.client.system.command.Command;
import dexum.client.system.config.Config;
import dexum.client.system.config.ConfigManager;

import java.util.List;

/**
 * Команда для управления конфигами
 */
public class ConfigCommand extends Command {
    
    public ConfigCommand() {
        super("config", "Управление конфигами", ".config <save|load|list|delete> [название]", "cfg", "c");
    }
    
    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            sendUsage();
            sendMessage("§7Доступные действия:");
            sendMessage("  §fsave §7<название> - сохранить конфиг");
            sendMessage("  §fload §7<название> - загрузить конфиг");
            sendMessage("  §flist §7- список конфигов");
            sendMessage("  §fdelete §7<название> - удалить конфиг");
            return;
        }
        
        String action = args[0].toLowerCase();
        
        switch (action) {
            case "save" -> {
                if (args.length < 2) {
                    sendError("Укажите название конфига");
                    return;
                }
                
                String name = args[1];
                Config config = new Config(name);
                
                if (config.save()) {
                    sendMessage("§aКонфиг §f" + name + " §aсохранен");
                    ConfigManager.loadConfigList();
                } else {
                    sendError("Не удалось сохранить конфиг");
                }
            }
            
            case "load" -> {
                if (args.length < 2) {
                    sendError("Укажите название конфига");
                    return;
                }
                
                String name = args[1];
                Config config = new Config(name);
                
                if (!config.exists()) {
                    sendError("Конфиг не найден: " + name);
                    return;
                }
                
                if (config.load()) {
                    sendMessage("§aКонфиг §f" + name + " §aзагружен");
                } else {
                    sendError("Не удалось загрузить конфиг");
                }
            }
            
            case "list" -> {
                ConfigManager.loadConfigList();
                List<Config> configs = ConfigManager.getConfigs();
                
                if (configs.isEmpty()) {
                    sendMessage("§7Нет сохраненных конфигов");
                    return;
                }
                
                sendMessage("§7Доступные конфиги:");
                for (Config config : configs) {
                    String current = config.equals(ConfigManager.getCurrentConfig()) ? " §a(текущий)" : "";
                    sendMessage("  §f" + config.getName() + current);
                }
            }
            
            case "delete" -> {
                if (args.length < 2) {
                    sendError("Укажите название конфига");
                    return;
                }
                
                String name = args[1];
                Config config = new Config(name);
                
                if (!config.exists()) {
                    sendError("Конфиг не найден: " + name);
                    return;
                }
                
                if (config.delete()) {
                    sendMessage("§cКонфиг §f" + name + " §cудален");
                    ConfigManager.loadConfigList();
                } else {
                    sendError("Не удалось удалить конфиг");
                }
            }
            
            default -> {
                sendError("Неизвестное действие: " + action);
                sendUsage();
            }
        }
    }
    
    @Override
    public List<String> getSuggestions(String[] args) {
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            return List.of("save", "load", "list", "delete").stream()
                    .filter(s -> s.startsWith(partial))
                    .toList();
        }
        
        if (args.length == 2 && (args[0].equalsIgnoreCase("load") || args[0].equalsIgnoreCase("delete"))) {
            String partial = args[1].toLowerCase();
            return ConfigManager.getConfigs().stream()
                    .map(Config::getName)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .toList();
        }
        
        return List.of();
    }
}
