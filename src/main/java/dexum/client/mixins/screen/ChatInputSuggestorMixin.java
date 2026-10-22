package dexum.client.mixins.screen;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import dexum.client.system.command.CommandManager;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Mixin(CommandSuggestions.class)
public abstract class ChatInputSuggestorMixin {

    @Shadow @Final EditBox input;
    @Shadow private ParseResults<?> currentParse;
    @Shadow private CompletableFuture<Suggestions> pendingSuggestions;
    @Shadow private CommandSuggestions.SuggestionsList suggestions;
    @Shadow boolean keepSuggestions;
    @Shadow public abstract void showSuggestions(boolean narrateFirstSuggestion);

    @Inject(method = "updateCommandInfo", at = @At("HEAD"), cancellable = true, require = 0)
    private void onRefresh(CallbackInfo ci) {
        String text = this.input.getValue();
        if (!text.startsWith(CommandManager.getPrefix())) return;
        List<String> suggestionList = CommandManager.getSuggestions(text);
        if (suggestionList.isEmpty()) return;
        ci.cancel();
        this.currentParse = null;
        if (this.keepSuggestions) return;
        this.input.setSuggestion(null);
        this.suggestions = null;
        StringRange range = StringRange.between(0, text.length());
        List<Suggestion> built = suggestionList.stream()
                .map(s -> new Suggestion(range, s)).collect(Collectors.toList());
        this.pendingSuggestions = CompletableFuture.completedFuture(new Suggestions(range, built));
        this.showSuggestions(false);
    }
}
