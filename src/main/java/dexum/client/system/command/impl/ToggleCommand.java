package dexum.client.system.command.impl;

import dexum.client.system.api.Function;
import dexum.client.system.api.FunctionManager;
import dexum.client.system.command.Command;

/**
 * Команда для включения/выключения модулей
 */
public class ToggleCommand extends Command {
    
    public ToggleCommand() {
        super("toggle", "Включает/выключает модуль", ".toggle <модуль>", "t");
    }
    
    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            sendUsage();
            return;
        }
        
        String moduleName = String.join(" ", args);
        Function function = findFunction(moduleName);
        
        if (function == null) {
            sendError("Модуль не найден: " + moduleName);
            return;
        }
        
        function.toggle();
        
        String status = function.isEnabled() ? "§aвключен" : "§cвыключен";
        sendMessage("§f" + function.getName() + " " + status);
    }
    
    @Override
    public java.util.List<String> getSuggestions(String[] args) {
        if (args.length <= 1) {
            String partial = args.length == 0 ? "" : args[0].toLowerCase();
            return FunctionManager.getFunctions().stream()
                    .map(Function::getName)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .sorted()
                    .toList();
        }
        return java.util.List.of();
    }
    
    private Function findFunction(String name) {
        return FunctionManager.getFunctions().stream()
                .filter(f -> f.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}
