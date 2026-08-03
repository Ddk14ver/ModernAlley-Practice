package dev.revere.alley;

import dev.revere.alley.feature.arena.Arena;
import dev.revere.alley.feature.arena.ArenaService;
import dev.revere.alley.feature.kit.KitService;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.data.ProfileData;
import dev.revere.alley.common.text.CC;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * AlleyAPI – A central class providing easy access to Alley.
 * AlleyAPI – 提供便捷访问Alley功能的中央类。
 * <p>
 * This class allows other developers to interact with the server functionalities of Alley,
 * 此类允许其他开发人员与Alley的服务器功能进行交互，
 * such as registering custom code to be executed during bootstrap enable and disable,
 * 例如注册在插件启用和禁用期间执行的自定义代码，
 * and accessing player profiles.
 * 以及访问玩家资料。
 * </p>
 * <p>
 * Developers can use this class to easily hook into the lifecycle of the Alley bootstrap
 * 开发人员可以使用此类轻松地挂载到Alley插件的生命周期中，
 * and retrieve player profiles without having to directly interact with other parts of the code.
 * 并检索玩家资料，而无需直接与代码的其他部分交互。
 * </p>
 *
 * @author Emmy
 * @project Alley
 * @since 22/04/2025
 */
@Getter
public class Alley {

    @Getter
    private static Alley instance;

    private final List<Runnable> onEnableCallbacks;
    private final List<Runnable> onDisableCallbacks;

    public Alley() {
        instance = this;

        this.onEnableCallbacks = new ArrayList<>();
        this.onDisableCallbacks = new ArrayList<>();
    }

    /**
     * Register custom code to be executed when Alley is enabled.
     * 注册在Alley启用时要执行的自定义代码。
     * Developers can use this method to inject their code into the onEnable lifecycle of Alley.
     * 开发人员可以使用此方法将其代码注入到Alley的onEnable生命周期中。
     *
     * @param callback The code to execute on enable.
     *                 启用时要执行的代码。
     */
    public void registerOnEnableCallback(Runnable callback) {
        this.onEnableCallbacks.add(callback);
    }

    /**
     * Register custom code to be executed when Alley is disabled.
     * 注册在Alley禁用时要执行的自定义代码。
     * Developers can use this method to inject their code into the onDisable lifecycle of Alley.
     * 开发人员可以使用此方法将其代码注入到Alley的onDisable生命周期中。
     *
     * @param callback The code to execute on disable.
     *                 禁用时要执行的代码。
     */
    public void registerOnDisableCallback(Runnable callback) {
        this.onDisableCallbacks.add(callback);
    }

    /**
     * Run all registered onEnable callbacks.
     * 运行所有已注册的onEnable回调。
     * This method executes each registered callback when Alley is enabled.
     * 当Alley启用时，此方法执行每个已注册的回调。
     */
    public void runOnEnableCallbacks() {
        if (this.onEnableCallbacks.isEmpty()) {
            Bukkit.getConsoleSender().sendMessage(CC.translate("&f[&6AlleyAPI&f] No external code registered to be executed on enable."));
            return;
        }

        for (Runnable callback : this.onEnableCallbacks) {
            callback.run();
        }
    }

    /**
     * Run all registered onDisable callbacks.
     * 运行所有已注册的onDisable回调。
     * This method executes each registered callback when Alley is disabled.
     * 当Alley禁用时，此方法执行每个已注册的回调。
     */
    public void runOnDisableCallbacks() {
        if (this.onDisableCallbacks.isEmpty()) {
            Bukkit.getConsoleSender().sendMessage(CC.translate("&f[&6AlleyAPI&f] No external code registered to be executed on disable."));
            return;
        }

        for (Runnable callback : this.onDisableCallbacks) {
            callback.run();
        }
    }

    /**
     * Get the profile of a player using their UUID.
     * 使用UUID获取玩家的个人资料。
     * Profile contains all types of non-statistic related data for the player.
     * 资料包含玩家所有类型的非统计数据。
     * Such as; UUID, Username, Join date, etc.
     * 例如：UUID、用户名、加入日期等。
     *
     * @param uuid The UUID of the player to retrieve the profile for.
     *             要检索资料的玩家的UUID。
     * @return The profile associated with the UUID.
     *         与该UUID关联的个人资料。
     */
    public Profile getProfile(UUID uuid) {
        return AlleyPlugin.getInstance().getService(ProfileService.class).getProfile(uuid);
    }

    /**
     * Get the profile data of a player using their UUID.
     * 使用UUID获取玩家的资料数据。
     * ProfileData contains all types of game related data for the player.
     * ProfileData包含玩家所有类型的游戏相关数据。
     * Such as; Ranked data, Unranked data, FFA data, Divisions, Titles, ELO, etc.
     * 例如：排位数据、非排位数据、FFA数据、段位、称号、ELO等。
     *
     * @param uuid The UUID of the player to retrieve the profile data for.
     *             要检索资料数据的玩家的UUID。
     * @return The profile data associated with the UUID.
     *         与该UUID关联的资料数据。
     */
    public ProfileData getProfileData(UUID uuid) {
        return AlleyPlugin.getInstance().getService(ProfileService.class).getProfile(uuid).getProfileData();
    }

    /**
     * Get a kit by its name.
     * 通过名称获取套件。
     * This method retrieves a kit from the Alley instance using its name.
     * 此方法使用名称从Alley实例中检索套件。
     *
     * @param kitName The name of the kit to retrieve.
     *                要检索的套件名称。
     * @return The Kit object associated with the given name, or null if not found.
     *         与给定名称关联的Kit对象，如果未找到则返回null。
     */
    public Kit getKit(String kitName) {
        return AlleyPlugin.getInstance().getService(KitService.class).getKits().stream().filter(kit -> kit.getName().equalsIgnoreCase(kitName)).findFirst().orElse(null);
    }

    /**
     * Get an arena by its name.
     * 通过名称获取竞技场。
     * This method retrieves an arena from the Alley instance using its name.
     * 此方法使用名称从Alley实例中检索竞技场。
     *
     * @param arenaName The name of the arena to retrieve.
     *                  要检索的竞技场名称。
     * @return The AbstractArena object associated with the given name, or null if not found.
     *         与给定名称关联的AbstractArena对象，如果未找到则返回null。
     */
    public Arena getArena(String arenaName) {
        return AlleyPlugin.getInstance().getService(ArenaService.class).getArenas().stream().filter(arena -> arena.getName().equalsIgnoreCase(arenaName)).findFirst().orElse(null);
    }

    /**
     * Get a random arena for a specific kit.
     * 为特定套件获取随机竞技场。
     * This method retrieves a random arena from the Alley instance for the specified kit.
     * 此方法从Alley实例中检索指定套件的随机竞技场。
     *
     * @param kit The Kit object for which to retrieve a random arena.
     *            要为其检索随机竞技场的Kit对象。
     * @return A random AbstractArena object associated with the given kit.
     *         与给定套件关联的随机AbstractArena对象。
     */
    public Arena getRandomArena(Kit kit) {
        return AlleyPlugin.getInstance().getService(ArenaService.class).getRandomArena(kit);
    }
}