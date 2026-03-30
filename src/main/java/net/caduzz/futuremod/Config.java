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
            .comment("Orb travel speed (blocks per tick).")
            .defineInRange("purpleVoid.orbSpeed", 0.72, 0.35, 1.4);

    public static final ModConfigSpec.DoubleValue PURPLE_VOID_ORB_RADIUS = BUILDER
            .comment("Radius of the dense sphere: damage, block breaking, and particles (blocks).")
            .defineInRange("purpleVoid.orbRadius", 4.5, 1.5, 9.0);

    public static final ModConfigSpec.DoubleValue PURPLE_VOID_BREAK_RADIUS_SCALE = BUILDER
            .comment("Block destruction radius as a fraction of orb radius (1.0 = same sphere as damage).")
            .defineInRange("purpleVoid.breakRadiusScale", 1.05, 0.5, 1.5);

    public static final ModConfigSpec.IntValue PURPLE_VOID_ORB_MAX_LIFE_TICKS = BUILDER
            .comment("Max ticks before the orb expires without hitting a wall.")
            .defineInRange("purpleVoid.orbMaxLifeTicks", 22, 8, 60);

    public static final ModConfigSpec.DoubleValue PURPLE_VOID_PULL_STRENGTH = BUILDER
            .comment("Inward pull while inside the orb (gravity-like); scaled stronger near the center.")
            .defineInRange("purpleVoid.pullStrength", 0.58, 0.12, 1.4);

    public static final ModConfigSpec.DoubleValue PURPLE_VOID_LIFE_STEAL_RATIO = BUILDER
            .comment("Fraction of damage dealt returned as healing to the caster.")
            .defineInRange("purpleVoid.lifeStealRatio", 0.30, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue PURPLE_VOID_BREAK_MAX_DESTROY_SPEED = BUILDER
            .comment("Break blocks with destroy speed at or below this value (0 disables breaking).")
            .defineInRange("purpleVoid.breakMaxDestroySpeed", 1.5, 0.0, 50.0);

    static final ModConfigSpec SPEC = BUILDER.build();

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }
}
