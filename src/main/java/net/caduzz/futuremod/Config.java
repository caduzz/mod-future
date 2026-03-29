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

    static final ModConfigSpec SPEC = BUILDER.build();

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }
}
