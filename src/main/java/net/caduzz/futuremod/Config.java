package net.caduzz.futuremod;

import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue LOG_DIRT_BLOCK = BUILDER
            .comment("Whether to log the dirt block on common setup")
            .define("logDirtBlock", true);

    public static final ModConfigSpec.IntValue MAGIC_NUMBER = BUILDER
            .comment("A magic number")
            .defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER
            .comment("What you want the introduction message to be for the magic number")
            .define("magicNumberIntroduction", "The magic number is... ");

    // a list of strings that are treated as resource locations for items
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER
            .comment("A list of items to log on common setup.")
            .defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), () -> "", Config::validateItemName);

    public static final ModConfigSpec.IntValue INFINITE_VOID_RADIUS_BLOCKS = BUILDER
            .comment("Radius in blocks of the infinite_void_domain.")
            .defineInRange("infiniteVoid.radiusBlocks", 14, 10, 20);

    public static final ModConfigSpec.IntValue INFINITE_VOID_DURATION_SECONDS = BUILDER
            .comment("Duration in seconds for infinite_void_domain.")
            .defineInRange("infiniteVoid.durationSeconds", 45, 30, 60);

    public static final ModConfigSpec.IntValue INFINITE_VOID_COOLDOWN_SECONDS = BUILDER
            .comment("Cooldown in seconds for infinite_void_domain.")
            .defineInRange("infiniteVoid.cooldownSeconds", 45, 30, 60);

    public static final ModConfigSpec.IntValue INFINITE_VOID_PARALYSIS_SECONDS = BUILDER
            .comment("Paralysis window in seconds while the domain starts.")
            .defineInRange("infiniteVoid.paralysisSeconds", 45, 40, 60);

    public static final ModConfigSpec.IntValue PURPLE_VOID_COOLDOWN_SECONDS = BUILDER
            .comment("Cooldown in seconds for Purple Void (H slot ability).")
            .defineInRange("purpleVoid.cooldownSeconds", 50, 20, 180);

    public static final ModConfigSpec.DoubleValue PURPLE_VOID_ORB_SPEED = BUILDER
            .comment("Purple bolt: linear travel speed (blocks per tick) after fusion.")
            .defineInRange("purpleVoid.orbSpeed", 0.85, 0.35, 1.4);

    public static final ModConfigSpec.DoubleValue PURPLE_VOID_VISUAL_RADIUS = BUILDER
            .comment("Client mesh radius in blocks for the purple void (stable + bolt).")
            .defineInRange("purpleVoid.visualRadius", 1.5, 0.8, 3.5);

    public static final ModConfigSpec.DoubleValue PURPLE_VOID_TUNNEL_ERASE_RADIUS = BUILDER
            .comment("Bolt phase only: sphere radius for removing blocks and discarding entities.")
            .defineInRange("purpleVoid.tunnelEraseRadius", 1.12, 0.25, 3.5);

    public static final ModConfigSpec.DoubleValue PURPLE_VOID_SIDE_OFFSET = BUILDER
            .comment("Horizontal distance (blocks) for blue/red fusion orbs from player center (left/right).")
            .defineInRange("purpleVoid.sideOffset", 1.25, 0.4, 4.0);

    public static final ModConfigSpec.DoubleValue PURPLE_VOID_ANCHOR_FORWARD_ALONG_LOOK = BUILDER
            .comment("Fusion point = eye position + this distance along look (in front of the camera in 1st person; follows crosshair when looking down).")
            .defineInRange("purpleVoid.anchorForwardAlongLook", 1.2, 0.15, 5.0);

    public static final ModConfigSpec.DoubleValue PURPLE_VOID_MERGE_DISTANCE = BUILDER
            .comment("When orbs are this close (blocks), or both are this close to the fusion point, merge spawns the bolt.")
            .defineInRange("purpleVoid.mergeDistance", 1.75, 0.15, 6.0);

    public static final ModConfigSpec.DoubleValue PURPLE_VOID_APPROACH_BLOCKS_PER_TICK = BUILDER
            .comment("Max movement per tick (blocks) toward fusion center; fixed-rate, not FPS-dependent.")
            .defineInRange("purpleVoid.approachBlocksPerTick", 0.32, 0.08, 1.2);

    public static final ModConfigSpec.IntValue PURPLE_VOID_FUSION_ORB_MAX_TICKS = BUILDER
            .comment("Failsafe: fusion orbs discard after this many ticks if merge never happens.")
            .defineInRange("purpleVoid.fusionOrbMaxTicks", 200, 40, 600);

    public static final ModConfigSpec.IntValue PURPLE_VOID_FUSION_POP_TICKS = BUILDER
            .comment("Ticks after fusion: purple sphere visual ease-in before the bolt moves.")
            .defineInRange("purpleVoid.fusionPopTicks", 6, 0, 30);

    public static final ModConfigSpec.DoubleValue PURPLE_VOID_CHARGED_SCALE = BUILDER
            .comment("While sneaking on activation: multiply tunnel/visual radius and speed by this factor.")
            .defineInRange("purpleVoid.chargedScale", 1.35, 1.0, 2.0);

    public static final ModConfigSpec.IntValue PURPLE_VOID_ORB_MAX_LIFE_TICKS = BUILDER
            .comment("Max ticks before the purple bolt expires.")
            .defineInRange("purpleVoid.orbMaxLifeTicks", 80, 20, 400);

    public static final ModConfigSpec.DoubleValue PURPLE_VOID_BREAK_MAX_DESTROY_SPEED = BUILDER
            .comment("Erase blocks with destroy speed at or below this value (0 disables). High values chew through stone/ores.")
            .defineInRange("purpleVoid.breakMaxDestroySpeed", 50.0, 0.0, 500.0);

    static final ModConfigSpec SPEC = BUILDER.build();

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }
}
