package net.caduzz.futuremod.integration;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.neoforged.fml.ModList;

import java.util.Optional;

/**
 * Integração opcional com Curios API (slot no estilo Artifacts).
 * Se o mod Curios estiver instalado, verifica se a entidade tem o item no slot "charm".
 */
public final class CuriosHelper {

    private static Boolean curiosPresent;

    public static boolean isCuriosLoaded() {
        if (curiosPresent == null) {
            curiosPresent = ModList.get().isLoaded("curios");
        }
        return curiosPresent;
    }

    /**
     * Retorna true se a entidade tem o item equipado em algum slot Curios (ex.: charm).
     * Usa reflexão para não depender do Curios em tempo de compilação.
     */
    public static boolean findFirstCurio(LivingEntity entity, Item item) {
        if (!isCuriosLoaded() || entity == null || item == null) {
            return false;
        }
        try {
            Class<?> apiClass = Class.forName("top.theillusivec4.curios.api.CuriosApi");
            Object helper = apiClass.getMethod("getCuriosHelper").invoke(null);
            @SuppressWarnings("unchecked")
            Optional<?> result = (Optional<?>) helper.getClass()
                    .getMethod("findFirstCurio", LivingEntity.class, Item.class)
                    .invoke(helper, entity, item);
            return result != null && result.isPresent();
        } catch (Exception e) {
            // Curios em outra versão/pacote (ex.: org.illusivesoulworks)
            try {
                Class<?> apiClass = Class.forName("org.illusivesoulworks.curios.api.CuriosApi");
                Object helper = apiClass.getMethod("getCuriosHelper").invoke(null);
                @SuppressWarnings("unchecked")
                Optional<?> result = (Optional<?>) helper.getClass()
                        .getMethod("findFirstCurio", LivingEntity.class, Item.class)
                        .invoke(helper, entity, item);
                return result != null && result.isPresent();
            } catch (Exception e2) {
                return false;
            }
        }
    }
}
