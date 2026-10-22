package dexum.client.system.command.impl;

import dexum.client.system.command.Command;
import dexum.client.system.command.CommandManager;

/**
 * Команда помощи - показывает список всех команд
 */
public class HelpCommand extends Command {
    
    public HelpCommand() {
        super("help", "Показывает список всех команд", ".help [страница]", "h", "?");
    }
    
    @Override
    public void execute(String[] args) {
        int page = 1;
        int commandsPerPage = 8;
        
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                sendError("Неверный номер страницы!");
                return;
            }
        }
        
        var commands = CommandManager.getCommands();
        int totalPages = (int) Math.ceil((double) commands.size() / commandsPerPage);
        
        if (page < 1 || page > totalPages) {
            sendError("Страница должна быть от 1 до " + totalPages);
            return;
        }
        
        sendMessage("§7========== §bПомощь §7(§f" + page + "§7/§f" + totalPages + "§7) ==========");
        
        int start = (page - 1) * commandsPerPage;
        int end = Math.min(start + commandsPerPage, commands.size());
        
        for (int i = start; i < end; i++) {
            Command cmd = commands.get(i);
            String aliases = cmd.getAliases().isEmpty() ? "" : " §8[§7" + String.join("§8, §7", cmd.getAliases()) + "§8]";
            sendMessage("§b." + cmd.getName() + aliases + " §8- §7" + cmd.getDescription());
        }
        
        if (page < totalPages) {
            sendMessage("§7Используйте §b.help " + (page + 1) + " §7для следующей страницы");
        }
    }
}
