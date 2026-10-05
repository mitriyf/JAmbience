package ru.mitriyf.jambience.compat.impl.v1_9;

import org.bukkit.entity.Player;
import ru.mitriyf.jambience.compat.abstraction.SetVisibility;

public class SetVisibilityV9 implements SetVisibility {
    @Override
    public void setVisibility(Player player, int visibility) {
        player.setViewDistance(visibility);
    }
}
