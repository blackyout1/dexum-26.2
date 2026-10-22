package dexum.client.system.friends;

import com.google.gson.*;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FriendManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final List<Friend> friends = new ArrayList<>();
    private static File friendsFile;

    public static void init() {
        File runDir = Minecraft.getInstance().gameDirectory;
        File dexumDir = new File(runDir, "dexum");
        if (!dexumDir.exists()) dexumDir.mkdirs();
        friendsFile = new File(dexumDir, "friends.json");
        load();
    }

    public static void addFriend(String name) {
        if (name == null || name.isBlank()) return;
        if (!isFriend(name)) { friends.add(new Friend(name.trim())); save(); }
    }

    public static void addFriend(String name, String alias) {
        if (name == null || name.isBlank()) return;
        if (!isFriend(name)) { friends.add(new Friend(name.trim(), alias != null ? alias.trim() : name.trim())); save(); }
    }

    public static void removeFriend(String name) {
        if (name == null) return;
        if (friends.removeIf(f -> f.getName().equalsIgnoreCase(name.trim()))) save();
    }

    public static boolean isFriend(String name) {
        if (name == null) return false;
        return friends.stream().anyMatch(f -> f.getName().equalsIgnoreCase(name.trim()));
    }

    public static List<Friend> getFriends() { return Collections.unmodifiableList(friends); }

    public static Friend getFriend(String name) {
        if (name == null) return null;
        return friends.stream().filter(f -> f.getName().equalsIgnoreCase(name.trim())).findFirst().orElse(null);
    }

    public static void setAlias(String name, String alias) {
        Friend f = getFriend(name);
        if (f != null) { f.setAlias(alias != null ? alias.trim() : f.getName()); save(); }
    }

    public static void clear() { friends.clear(); save(); }

    @Deprecated public static void add(String username) { addFriend(username); }
    @Deprecated public static void remove(String username) { removeFriend(username); }

    @Deprecated
    public static List<String> getAll() {
        List<String> names = new ArrayList<>();
        for (Friend f : friends) names.add(f.getName());
        return Collections.unmodifiableList(names);
    }

    public static void save() {
        if (friendsFile == null) return;
        try {
            JsonArray arr = new JsonArray();
            for (Friend f : friends) {
                JsonObject obj = new JsonObject();
                obj.addProperty("name", f.getName());
                if (f.getAlias() != null && !f.getAlias().equals(f.getName()))
                    obj.addProperty("alias", f.getAlias());
                arr.add(obj);
            }
            try (Writer w = new OutputStreamWriter(new FileOutputStream(friendsFile), StandardCharsets.UTF_8)) {
                GSON.toJson(arr, w);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void load() {
        if (friendsFile == null || !friendsFile.exists()) return;
        try {
            String content = new String(Files.readAllBytes(friendsFile.toPath()), StandardCharsets.UTF_8);
            JsonElement root = JsonParser.parseString(content);
            if (!root.isJsonArray()) return;
            friends.clear();
            for (JsonElement el : root.getAsJsonArray()) {
                if (el.isJsonPrimitive()) {
                    friends.add(new Friend(el.getAsString()));
                } else if (el.isJsonObject()) {
                    JsonObject obj = el.getAsJsonObject();
                    String name = obj.has("name") ? obj.get("name").getAsString() : null;
                    if (name == null || name.isBlank()) continue;
                    String alias = obj.has("alias") ? obj.get("alias").getAsString() : name;
                    friends.add(new Friend(name, alias));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
