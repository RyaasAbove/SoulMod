package net.ryaas.soulmod.powers;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;

import java.util.function.Supplier;

public class Ability {
    private final String id;
    private final String description;
    private final String iconPath; // Path to the icon texture
    private final boolean chargeable;



    public Ability(String id, String description, String iconPath, boolean chargeable) {
        this.id = id;
        this.description = description;
        this.iconPath = iconPath;
        this.chargeable = chargeable;


    }

    public void execute(Player player, float charge) {

    }

    float getCost() {
        return 0;
    }

    float getCooldown() {
        return 0;
    }


    AbilityType getType() {
        return null;
    }

    enum AbilityType {
        PROJECTILE,
        BUFF,
        DEBUFF,
        UTILITY
        // Add more types as needed
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getIconPath() {
        return iconPath;
    }

    // Implement ability-specific methods or handlers as needed
    public boolean isChargeable() {
        return chargeable;
    }


}
