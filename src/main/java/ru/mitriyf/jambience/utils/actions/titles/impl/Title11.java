package ru.mitriyf.jambience.utils.actions.titles.impl;

import org.bukkit.entity.Player;
import ru.mitriyf.jambience.utils.actions.titles.Title;

@SuppressWarnings("deprecation")
public class Title11 implements Title {
    @Override
    public void send(Player p, String title, String subtitle, int i, int d, int k) {
        p.sendTitle(title, subtitle, i, d, k);
    }
}
