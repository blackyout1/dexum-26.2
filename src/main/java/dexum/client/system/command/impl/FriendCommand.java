package dexum.client.system.command.impl;

import dexum.client.system.command.Command;
import dexum.client.system.friends.Friend;
import dexum.client.system.friends.FriendManager;

import java.util.List;

/**
 * Команда для управления друзьями
 */
public class FriendCommand extends Command {
    
    public FriendCommand() {
        super("friend", "Управление друзьями", ".friend <add|remove|list|clear|alias> [ник] [псевдоним]", "f");
    }
    
    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            sendUsage();
            sendMessage("§7Доступные действия:");
            sendMessage("  §fadd §7<ник> [псевдоним] - добавить друга");
            sendMessage("  §fremove §7<ник> - удалить друга");
            sendMessage("  §flist §7- список друзей");
            sendMessage("  §fclear §7- очистить список");
            sendMessage("  §falias §7<ник> <псевдоним> - изменить псевдоним");
            return;
        }
        
        String action = args[0].toLowerCase();
        
        switch (action) {
            case "add" -> {
                if (args.length < 2) {
                    sendError("Укажите ник игрока");
                    return;
                }
                
                String name = args[1];
                
                if (FriendManager.isFriend(name)) {
                    sendError(name + " уже в списке друзей");
                    return;
                }
                
                if (args.length >= 3) {
                    String alias = args[2];
                    FriendManager.addFriend(name, alias);
                    sendMessage("§a" + name + " §7добавлен как §f" + alias);
                } else {
                    FriendManager.addFriend(name);
                    sendMessage("§a" + name + " §7добавлен в друзья");
                }
            }
            
            case "remove", "del" -> {
                if (args.length < 2) {
                    sendError("Укажите ник игрока");
                    return;
                }
                
                String name = args[1];
                
                if (!FriendManager.isFriend(name)) {
                    sendError(name + " не в списке друзей");
                    return;
                }
                
                FriendManager.removeFriend(name);
                sendMessage("§c" + name + " §7удален из друзей");
            }
            
            case "list" -> {
                List<Friend> friends = FriendManager.getFriends();
                
                if (friends.isEmpty()) {
                    sendMessage("§7Список друзей пуст");
                    return;
                }
                
                sendMessage("§7Друзья §f(" + friends.size() + ")§7:");
                for (Friend friend : friends) {
                    if (friend.getAlias().equals(friend.getName())) {
                        sendMessage("  §a" + friend.getName());
                    } else {
                        sendMessage("  §a" + friend.getName() + " §7(§f" + friend.getAlias() + "§7)");
                    }
                }
            }
            
            case "clear" -> {
                if (FriendManager.getFriends().isEmpty()) {
                    sendMessage("§7Список друзей уже пуст");
                    return;
                }
                
                int count = FriendManager.getFriends().size();
                FriendManager.clear();
                sendMessage("§7Список друзей очищен §f(" + count + ")");
            }
            
            case "alias" -> {
                if (args.length < 3) {
                    sendError("Использование: .friend alias <ник> <псевдоним>");
                    return;
                }
                
                String name = args[1];
                String alias = args[2];
                
                if (!FriendManager.isFriend(name)) {
                    sendError(name + " не в списке друзей");
                    return;
                }
                
                FriendManager.setAlias(name, alias);
                sendMessage("§7Псевдоним для §a" + name + " §7изменен на §f" + alias);
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
            return List.of("add", "remove", "list", "clear", "alias").stream()
                    .filter(s -> s.startsWith(partial))
                    .toList();
        }
        
        if (args.length == 2 && (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("alias"))) {
            String partial = args[1].toLowerCase();
            return FriendManager.getFriends().stream()
                    .map(Friend::getName)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .toList();
        }
        
        return List.of();
    }
}
