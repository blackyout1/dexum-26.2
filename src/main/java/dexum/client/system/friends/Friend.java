package dexum.client.system.friends;

import lombok.Getter;
import lombok.Setter;

/**
 * Представляет друга
 */
@Getter
public class Friend {
    private final String name;
    
    @Setter
    private String alias; // Псевдоним (опционально)
    
    public Friend(String name) {
        this.name = name;
        this.alias = name;
    }
    
    public Friend(String name, String alias) {
        this.name = name;
        this.alias = alias;
    }
    
    /**
 * Получить отображаемое имя
     */
    public String getDisplayName() {
        return alias != null && !alias.equals(name) ? alias : name;
    }
}
