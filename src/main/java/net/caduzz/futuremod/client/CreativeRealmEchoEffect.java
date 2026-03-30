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
    // Mesmo sem EFX, eco manual usa mix mais perceptível. A voz original fica intacta.
    private static final float[] ECHO_VOLUMES = { 0.75f, 0.50f, 0.30f };
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

        // Tenta aplicar reverberação via OpenAL EFX a todas as fontes.
        // Caso não haja suporte, mantém fallback de eco manual para feedback audível.
        if (!efxAvailable || !reverbEnabled) {
            if (shouldScheduleEcho(sound)) {
                scheduleManualEchos(sound, mc);
            }
        }
    }

    private static void ensureReverbInitialized() {
        // Se já está inicializado e disponivel, não refaz.
        if (efxAvailable && effectId != 0 && effectSlotId != 0) return;

        if (!isEfxSupported()) {
            efxAvailable = false;
            return;
        }

        if (effectId != 0) {
            EXTEfx.alDeleteEffects(effectId);
            effectId = 0;
        }

        if (effectSlotId != 0) {
            EXTEfx.alDeleteAuxiliaryEffectSlots(effectSlotId);
            effectSlotId = 0;
        }

        effectId = EXTEfx.alGenEffects();
        EXTEfx.alEffecti(effectId, EXTEfx.AL_EFFECT_TYPE, EXTEfx.AL_EFFECT_REVERB);

        // Reverb mais presente, mas sem sufocar agudos.
        // Dry permanece; wet é auxiliário via AL_AUXILIARY_SEND_FILTER.
        EXTEfx.alEffectf(effectId, EXTEfx.AL_REVERB_DENSITY, 0.85f);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_REVERB_DIFFUSION, 0.88f);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_REVERB_GAIN, 0.75f);          // ganho de reverb geral (aumenta presença)
        EXTEfx.alEffectf(effectId, EXTEfx.AL_REVERB_GAINHF, 0.95f);        // preservar agudos
        EXTEfx.alEffectf(effectId, EXTEfx.AL_REVERB_DECAY_TIME, 2.2f);      // decay levemente maior para mais espacialidade
        EXTEfx.alEffectf(effectId, EXTEfx.AL_REVERB_DECAY_HFRATIO, 0.78f);  // menos atenuação alta
        EXTEfx.alEffectf(effectId, EXTEfx.AL_REVERB_REFLECTIONS_GAIN, 0.20f);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_REVERB_REFLECTIONS_DELAY, 0.18f);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_REVERB_LATE_REVERB_GAIN, 1.35f);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_REVERB_LATE_REVERB_DELAY, 0.22f);
        EXTEfx.alEffectf(effectId, EXTEfx.AL_REVERB_AIR_ABSORPTION_GAINHF, 0.92f);

        effectSlotId = EXTEfx.alGenAuxiliaryEffectSlots();
        EXTEfx.alAuxiliaryEffectSloti(effectSlotId, EXTEfx.AL_EFFECTSLOT_EFFECT, effectId);

        efxAvailable = true;
        warnedMissingEfx = false;
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

        // Reconfigure o send para cada som que inicia. ===> garante confiabilidade para fontes rápidas.
        attachAuxSend(sourceId);

        ATTACHED_SOURCES.add(sourceId);
    }

    private static int extractSourceId(Channel channel) {
        if (channel == null) return 0;
        return findSourceId(channel);
    }

    private static int findSourceId(Object obj) {
        if (obj == null) return 0;

        // Verifica métodos declarados e herdados.
        for (Method method : getAllMethods(obj.getClass())) {
            if (method.getParameterCount() != 0) continue;
            String name = method.getName().toLowerCase();
            if (!name.contains("source")) continue;

            try {
                method.setAccessible(true);
                Object result = method.invoke(obj);
                int id = toSourceId(result);
                if (id > 0) return id;
            } catch (Exception ignored) {
            }
        }

        // Fallback por campos declarados e herdados.
        for (Field field : getAllFields(obj.getClass())) {
            String fieldName = field.getName().toLowerCase();
            if (!fieldName.contains("source")) continue;

            try {
                field.setAccessible(true);
                Object value = field.get(obj);
                int id = toSourceId(value);
                if (id > 0) return id;
            } catch (Exception ignored) {
            }
        }

        // Fallback: recursivo em objetos internos para encontrar o source id.
        for (Method method : getAllMethods(obj.getClass())) {
            if (method.getParameterCount() != 0) continue;
            try {
                method.setAccessible(true);
                Object nested = method.invoke(obj);
                if (nested == null || nested == obj) continue;
                int nestedId = findSourceId(nested);
                if (nestedId > 0) return nestedId;
            } catch (Exception ignored) {
            }
        }

        return 0;
    }

    private static int toSourceId(Object value) {
        if (value instanceof Number number) {
            long longValue = number.longValue();
            if (longValue > 0 && longValue <= Integer.MAX_VALUE) {
                return (int) longValue;
            }
        }
        return 0;
    }

    private static List<Method> getAllMethods(Class<?> clazz) {
        List<Method> methods = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null) {
            for (Method method : current.getDeclaredMethods()) {
                methods.add(method);
            }
            current = current.getSuperclass();
        }
        return methods;
    }

    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null) {
            for (Field field : current.getDeclaredFields()) {
                fields.add(field);
            }
            current = current.getSuperclass();
        }
        return fields;
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
            if (sound.getLocation() == null) return true;
            String path = sound.getLocation().getPath();

            // Aplicar eco em todos os sons de jogo (inclusive passo, mobs, ambiente), mas não UI/click/toast.
            return !(path.contains("ui.") || path.contains("click") || path.contains("toast"));
        } catch (Exception e) {
            return true;
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
