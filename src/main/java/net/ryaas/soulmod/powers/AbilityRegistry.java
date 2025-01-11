package net.ryaas.soulmod.powers;

import net.minecraftforge.registries.ObjectHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AbilityRegistry {
    private static final Map<String, Ability> ABILITIES = new HashMap<>();

    // Initialize abilities
    public static void registerAbilities() {
        // Example abilities
        registerAbility(new Ability("fireball", "Launch a fireball towards a target.", "starspawn.png", false));
        registerAbility(new Ability("starspawn", "Spawn a selection of stars.", "space_path_spritesheet.png", true));
        registerAbility(new Ability("rg", "Spawn a giant red star.", "space_path_spritesheet.png", true));
        // Add more abilities as needed
    }

    private static void registerAbility(Ability ability) {
        ABILITIES.put(ability.getId(), ability);
    }

    public static Ability getAbility(String id) {
        return ABILITIES.get(id);
    }

    public static Set<String> getAllAbilityIds() {
        return ABILITIES.keySet();
    }

    // A convenience method
    public static boolean isChargeable(String abilityId) {
        Ability ability = getAbility(abilityId);
        if (ability == null) return false;
        return ability.isChargeable();
    }

}