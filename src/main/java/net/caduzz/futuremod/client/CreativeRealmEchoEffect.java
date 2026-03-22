package net.caduzz.futuremod.client;

import java.util.ArrayList;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.mojang.blaze3d.audio.Channel;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.EXTEfx;

import net.caduzz.futuremod.FutureMod;
import net.caduzz.futuremod.dimension.ModDimensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundSourceEvent;
import net.neoforged.neoforge.client.event.sound.PlayStreamingSourceEvent;

/**
 * Reverberação real via OpenAL EFX (sem eco manual).
 * Ativa apenas na Creative Realm e aplica o send auxiliar de reverb em todas
 * as fontes ativas, criando ambiente de caverna grande e vazia.
 */
@EventBusSubscriber(modid = FutureMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class CreativeRealmEchoEffect {

    private static final Set<Integer> ATTACHED_SOURCES = ConcurrentHashMap.newKeySet();
    private static final List<ScheduledEcho> SCHEDULED_ECHOS = new CopyOnWriteArrayList<>();

    private static final int DELAY_MIN_TICKS = 3; // 0.15s
    private static final int DELAY_MAX_TICKS = 7; // 0.35s
    private static final float[] ECHO_VOLUMES = { 0.55f, 0.32f, 0.18f };
    private static final int MAX_PENDING_ECHOS = 128;

    private static int effectId = 0;
    private static int effectSlotId = 0;
    private static boolean efxAvailable = false;
    private static boolean reverbEnabled = false;
    private static boolean warnedMissingEfx = false;
    private static int tickCounter = 0;
    private static boolean playingEchoNow = false;

    private record ScheduledEcho(
            int playAtTick,
            ResourceLocation location,
            SoundSource source,
            float volume,
            float pitch,
            double x,
            double y,
            double z,
            RandomSource random) {
    }

    /**
     * Marca sons disparados pelo proprio fallback de eco.
     * Isso evita recursao (eco do eco) e saturacao da fila.
     */
    private static final class EchoedSoundInstance extends SimpleSoundInstance {
        private EchoedSoundInstance(
                ResourceLocation location,
                SoundSource source,
                float volume,
                float pitch,
                RandomSource random,
                double x,
                double y,
                double z) {
            super(location, source, volume, pitch, random, false, 0, SoundInstance.Attenuation.LINEAR, x, y, z, true);
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        tickCounter++;

        if (mc.level == null) {
            disableReverb();
            return;
        }

        boolean inCreativeRealm = mc.level.dimension().equals(ModDimensions.CREATIVE_REALM_LEVEL);
        if (!inCreativeRealm) {
            disableReverb();
            return;
        }

        runEchoQueue(mc);

        ensureReverbInitialized();
        if (!efxAvailable) {
            if (!warnedMissingEfx) {
                FutureMod.LOGGER.warn("OpenAL EFX indisponivel. Reverb da Creative Realm nao sera aplicado.");
                warnedMissingEfx = true;
            }
            return;
        }

        if (!reverbEnabled) {
            enableReverb();
        }
    }

    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        if (!mc.level.dimension().equals(ModDimensions.CREATIVE_REALM_LEVEL)) return;

        SoundInstance sound = event.getOriginalSound();
        if (sound == null) return;
        if (playingEchoNow) return;
        if (sound instanceof EchoedSoundInstance) return;
        if (!shouldScheduleEcho(sound)) return;

        scheduleManualEchos(sound, mc);
    }

    private static void ensureReverbInitialized() {
        if (efxAvailable || effectId != 0 || effectSlotId != 0) return;
        if (!isEfxSupported()) return;

        effectId = EXTEfx.alGenEffects();
        EXTEfx.alEffecti(effectId, EXTEfx.AL_EFFECT_TYPE, EXTEfx.AL_EFFECT_REVERB);

        // Reverb suave de caverna grande:
        // pre-delay ~0.2s, decay ~2.0s, room size médio/grande, damping leve.
        EXTEfx.alEffectf(effectId, EXTEfx.AL_REVERB_DENSITY, 0.78f);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_REVERB_DIFFUSION, 0.82f);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_REVERB_GAIN, 0.45f);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_REVERB_GAINHF, 0.86f);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_REVERB_DECAY_TIME, 2.0f);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_REVERB_DECAY_HFRATIO, 0.72f);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_REVERB_REFLECTIONS_GAIN, 0.13f);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_REVERB_REFLECTIONS_DELAY, 0.20f);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_REVERB_LATE_REVERB_GAIN, 1.15f);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_REVERB_LATE_REVERB_DELAY, 0.24f);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_REVERB_AIR_ABSORPTION_GAINHF, 0.97f);

        effectSlotId = EXTEfx.alGenAuxiliaryEffectSlots();
        EXTEfx.alAuxiliaryEffectSloti(effectSlotId, EXTEfx.AL_EFFECTSLOT_EFFECT, effectId);

        efxAvailable = true;
    }

    private static boolean isEfxSupported() {
        long context = ALC10.alcGetCurrentContext();
        if (context == 0L) return false;
        long device = ALC10.alcGetContextsDevice(context);
        if (device == 0L) return false;
        return ALC10.alcIsExtensionPresent(device, "ALC_EXT_EFX");
    }

    private static void enableReverb() {
        reverbEnabled = true;
    }

    private static void disableReverb() {
        if (!reverbEnabled && ATTACHED_SOURCES.isEmpty()) return;

        for (int sourceId : ATTACHED_SOURCES) {
            clearAuxSend(sourceId);
        }
        ATTACHED_SOURCES.clear();
        SCHEDULED_ECHOS.clear();
        reverbEnabled = false;
    }

    @SubscribeEvent
    public static void onPlaySoundSource(PlaySoundSourceEvent event) {
        tryAttachForChannel(event.getChannel());
    }

    @SubscribeEvent
    public static void onPlayStreamingSource(PlayStreamingSourceEvent event) {
        tryAttachForChannel(event.getChannel());
    }

    private static void tryAttachForChannel(Channel channel) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || !mc.level.dimension().equals(ModDimensions.CREATIVE_REALM_LEVEL)) return;

        ensureReverbInitialized();
        if (!efxAvailable) return;
        if (!reverbEnabled) enableReverb();

        int sourceId = extractSourceId(channel);
        if (sourceId <= 0) return;
        if (ATTACHED_SOURCES.add(sourceId)) {
            attachAuxSend(sourceId);
        }
    }

    private static int extractSourceId(Channel channel) {
        if (channel == null) return 0;

        int id = findSourceId(channel);
        if (id > 0) return id;

        // Alguns builds encapsulam a source em um objeto interno acessivel por metodo.
        try {
            for (Method method : channel.getClass().getDeclaredMethods()) {
                if (method.getParameterCount() == 0) {
                    method.setAccessible(true);
                    Object nested = method.invoke(channel);
                    int nestedId = findSourceId(nested);
                    if (nestedId > 0) return nestedId;
                }
            }
        } catch (Exception e) {
            // Ignora e retorna 0.
        }
        return 0;
    }

    private static int findSourceId(Object obj) {
        if (obj == null) return 0;

        // Tenta métodos "getSource"/"source".
        for (Method method : obj.getClass().getDeclaredMethods()) {
            if (method.getParameterCount() == 0 && method.getReturnType() == int.class) {
                String name = method.getName().toLowerCase();
                if (name.contains("source")) {
                    try {
                        method.setAccessible(true);
                        int id = (int) method.invoke(obj);
                        if (id > 0) return id;
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        // Fallback por campo int contendo "source".
        Class<?> type = obj.getClass();
        while (type != null) {
            for (Field field : type.getDeclaredFields()) {
                if (field.getType() == int.class && field.getName().toLowerCase().contains("source")) {
                    try {
                        field.setAccessible(true);
                        int id = field.getInt(obj);
                        if (id > 0) return id;
                    } catch (Exception ignored) {
                    }
                }
            }
            type = type.getSuperclass();
        }

        return 0;
    }

    private static void attachAuxSend(int sourceId) {
        AL11.alSource3i(sourceId, EXTEfx.AL_AUXILIARY_SEND_FILTER, effectSlotId, 0, 0);
    }

    private static void clearAuxSend(int sourceId) {
        AL11.alSource3i(sourceId, EXTEfx.AL_AUXILIARY_SEND_FILTER, 0, 0, 0);
    }

    private static void runEchoQueue(Minecraft mc) {
        if (SCHEDULED_ECHOS.isEmpty()) return;

        List<ScheduledEcho> toRemove = new ArrayList<>();
        for (ScheduledEcho echo : SCHEDULED_ECHOS) {
            if (echo.playAtTick <= tickCounter) {
                EchoedSoundInstance echoed = new EchoedSoundInstance(
                        echo.location,
                        echo.source,
                        echo.volume,
                        echo.pitch,
                        echo.random,
                        echo.x,
                        echo.y,
                        echo.z);
                playingEchoNow = true;
                try {
                    mc.getSoundManager().play(echoed);
                } finally {
                    playingEchoNow = false;
                }
                toRemove.add(echo);
            }
        }
        SCHEDULED_ECHOS.removeAll(toRemove);
    }

    private static boolean shouldScheduleEcho(SoundInstance sound) {
        try {
            if (sound.getLocation() == null) return false;
            SoundSource source = sound.getSource();
            if (source == SoundSource.MUSIC || source == SoundSource.RECORDS || source == SoundSource.MASTER) {
                return false;
            }
            String path = sound.getLocation().getPath();
            return !(path.contains("ui.") || path.contains("click") || path.contains("toast"));
        } catch (Exception e) {
            return false;
        }
    }

    private static void scheduleManualEchos(SoundInstance original, Minecraft mc) {
        final float baseVolume;
        final float basePitch;
        try {
            baseVolume = original.getVolume();
            basePitch = original.getPitch();
        } catch (Exception e) {
            return;
        }

        RandomSource rng = mc.player != null ? mc.player.getRandom() : SoundInstance.createUnseededRandom();
        double x = original.getX();
        double y = original.getY();
        double z = original.getZ();

        if (original.getAttenuation() == SoundInstance.Attenuation.NONE && mc.player != null) {
            x = mc.player.getX();
            y = mc.player.getY();
            z = mc.player.getZ();
        }

        int delay = DELAY_MIN_TICKS + rng.nextInt(DELAY_MAX_TICKS - DELAY_MIN_TICKS + 1);
        
        for (float echoVolumeFactor : ECHO_VOLUMES) {
            float echoVolume = Math.max(0.01f, baseVolume * echoVolumeFactor);
            float echoPitch = basePitch * (0.94f + rng.nextFloat() * 0.04f);
            while (SCHEDULED_ECHOS.size() >= MAX_PENDING_ECHOS) {
                SCHEDULED_ECHOS.remove(0);
            }
            SCHEDULED_ECHOS.add(new ScheduledEcho(
                    tickCounter + delay,
                    original.getLocation(),
                    original.getSource(),
                    echoVolume,
                    echoPitch,
                    x,
                    y,
                    z,
                    rng));
            delay += DELAY_MIN_TICKS + rng.nextInt(DELAY_MAX_TICKS - DELAY_MIN_TICKS + 1);
        }
    }
}
