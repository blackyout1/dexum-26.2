package dexum.client.system.command.impl;

import dexum.client.system.api.Function;
import dexum.client.system.api.FunctionManager;
import dexum.client.system.command.Command;
import org.lwjgl.glfw.GLFW;

/**
 * Команда для привязки клавиш к модулям
 */
public class BindCommand extends Command {
    
    public BindCommand() {
        super("bind", "Привязывает клавишу к модулю", ".bind <модуль> <клавиша|clear>", "b");
    }
    
    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            sendUsage();
            return;
        }
        
        String moduleName = args[0];
        String keyName = args[1].toUpperCase();
        
        Function function = findFunction(moduleName);
        
        if (function == null) {
            sendError("Модуль не найден: " + moduleName);
            return;
        }
        
        if (keyName.equalsIgnoreCase("clear") || keyName.equalsIgnoreCase("none")) {
            function.setKeybind(GLFW.GLFW_KEY_UNKNOWN);
            sendMessage("Привязка для §f" + function.getName() + " §7очищена");
            return;
        }
        
        int keyCode = getKeyCode(keyName);
        
        if (keyCode == GLFW.GLFW_KEY_UNKNOWN) {
            sendError("Неверная клавиша: " + keyName);
            return;
        }
        
        function.setKeybind(keyCode);
        sendMessage("§f" + function.getName() + " §7привязан к §f" + keyName);
    }
    
    @Override
    public java.util.List<String> getSuggestions(String[] args) {
        // Первый аргумент - название модуля
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            return FunctionManager.getFunctions().stream()
                    .map(Function::getName)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .sorted()
                    .toList();
        }
        
        // Второй аргумент - клавиша
        if (args.length == 2) {
            String partial = args[1].toUpperCase();
            java.util.List<String> keys = java.util.Arrays.asList(
                    "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
                    "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
                    "LSHIFT", "RSHIFT", "LCTRL", "RCTRL", "LALT", "RALT",
                    "SPACE", "ENTER", "TAB", "CAPSLOCK", "ESCAPE", "CLEAR", "NONE"
            );
            return keys.stream()
                    .filter(key -> key.startsWith(partial))
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
    
    private int getKeyCode(String keyName) {
        // Буквы
        if (keyName.length() == 1 && Character.isLetter(keyName.charAt(0))) {
            return GLFW.GLFW_KEY_A + (keyName.charAt(0) - 'A');
        }
        
        // Цифры
        if (keyName.length() == 1 && Character.isDigit(keyName.charAt(0))) {
            return GLFW.GLFW_KEY_0 + (keyName.charAt(0) - '0');
        }
        
        // Специальные клавиши
        return switch (keyName) {
            case "LSHIFT", "LEFTSHIFT" -> GLFW.GLFW_KEY_LEFT_SHIFT;
            case "RSHIFT", "RIGHTSHIFT" -> GLFW.GLFW_KEY_RIGHT_SHIFT;
            case "LCTRL", "LEFTCTRL" -> GLFW.GLFW_KEY_LEFT_CONTROL;
            case "RCTRL", "RIGHTCTRL" -> GLFW.GLFW_KEY_RIGHT_CONTROL;
            case "LALT", "LEFTALT" -> GLFW.GLFW_KEY_LEFT_ALT;
            case "RALT", "RIGHTALT" -> GLFW.GLFW_KEY_RIGHT_ALT;
            case "SPACE" -> GLFW.GLFW_KEY_SPACE;
            case "ENTER" -> GLFW.GLFW_KEY_ENTER;
            case "TAB" -> GLFW.GLFW_KEY_TAB;
            case "CAPSLOCK" -> GLFW.GLFW_KEY_CAPS_LOCK;
            case "ESC", "ESCAPE" -> GLFW.GLFW_KEY_ESCAPE;
            case "BACKSPACE" -> GLFW.GLFW_KEY_BACKSPACE;
            default -> GLFW.GLFW_KEY_UNKNOWN;
        };
    }
}
