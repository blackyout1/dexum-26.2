package dexum.client.system.command.impl;

import dexum.client.system.api.Category;
import dexum.client.system.api.Function;
import dexum.client.system.api.FunctionManager;
import dexum.client.system.command.Command;

/**
 * Команда для отображения списка модулей
 */
public class ModulesCommand extends Command {
    
    public ModulesCommand() {
        super("modules", "Показывает список всех модулей", ".modules [категория]", "m", "mods");
    }
    
    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            // Показать все модули по категориям
            sendMessage("§7========== §bМодули §7==========");
            
            for (Category category : Category.values()) {
                var functions = FunctionManager.getFunctionsByCategory(category);
                if (functions.isEmpty()) continue;
                
                sendMessage("§b" + category.getName() + " §8[§7" + functions.size() + "§8]");
                
                StringBuilder sb = new StringBuilder("  ");
                for (Function function : functions) {
                    String status = function.isEnabled() ? "§a" : "§7";
                    sb.append(status).append(function.getName()).append("§7, ");
                }
                
                // Удалить последнюю запятую
                if (sb.length() > 2) {
                    sb.setLength(sb.length() - 4);
                }
                
                sendMessage(sb.toString());
            }
        } else {
            // Показать модули конкретной категории
            String categoryName = String.join(" ", args);
            Category category = findCategory(categoryName);
            
            if (category == null) {
                sendError("Категория не найдена: " + categoryName);
                sendMessage("§7Доступные категории: §fCombat, Movement, Render, Player, World");
                return;
            }
            
            var functions = FunctionManager.getFunctionsByCategory(category);
            
            sendMessage("§7========== §b" + category.getName() + " §7==========");
            
            for (Function function : functions) {
                String status = function.isEnabled() ? "§a✓" : "§c✗";
                sendMessage(status + " §f" + function.getName() + " §8- §7" + function.getDesc());
            }
        }
    }
    
    private Category findCategory(String name) {
        for (Category category : Category.values()) {
            if (category.getName().equalsIgnoreCase(name) || category.name().equalsIgnoreCase(name)) {
                return category;
            }
        }
        return null;
    }
}
