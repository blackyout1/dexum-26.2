package dexum.client.system.features.player;

import dexum.client.system.api.Category;
import dexum.client.system.api.Function;
import dexum.client.system.api.NewFunction;
import dexum.client.system.settings.impl.BooleanSetting;
import dexum.client.system.settings.impl.ModeSetting;
import lombok.Getter;

@NewFunction(name = "StreamerMode", desc = "Скрывает ник и клиент — для стримов и скриншотов", category = Category.PLAYER)
public class StreamerMode extends Function {

    @Getter
    private static StreamerMode instance;

    private final BooleanSetting hideNick    = new BooleanSetting("Скрыть ник", true);
    private final BooleanSetting hideClient  = new BooleanSetting("Скрыть название клиента", true);
    private final ModeSetting fakeName = new ModeSetting("Псевдоним",
            "Player", "Steve", "Alex", "Notch", "Herobrine", "Anonymous");

    public StreamerMode() {
        instance = this;
        addSettings(hideNick, hideClient, fakeName);
    }

    /** Возвращает отображаемый ник (реальный или псевдоним) */
    public String getDisplayName(String realName) {
        if (isEnabled() && hideNick.isEnabled()) {
            return fakeName.getActiveMode();
        }
        return realName;
    }

    /** Показывать ли название клиента в Watermark */
    public boolean shouldHideClientName() {
        return isEnabled() && hideClient.isEnabled();
    }
}
