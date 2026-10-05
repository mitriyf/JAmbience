package ru.mitriyf.jambience.compat.impl.v1_8;

import org.bukkit.entity.Player;
import ru.mitriyf.jambience.compat.abstraction.SetVisibility;

import java.lang.reflect.Method;

public class SetVisibilityV8 implements SetVisibility {
    private final Method setVisibilityMethod;

    @SuppressWarnings("all")
    public SetVisibilityV8(Method setVisibilityMethod) {
        this.setVisibilityMethod = setVisibilityMethod;
    }

    @Override
    public void setVisibility(Player player, int visibility) throws Exception {
        setVisibilityMethod.invoke(player.spigot(), visibility);
    }
}
