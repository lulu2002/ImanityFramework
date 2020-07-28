package org.imanity.framework.bossbar;

public interface BossBarAdapter {

    /**
     *
     * @param bossBar The data included receiver, previous
     * @return Should appear bossbar or not
     */
    BossBarData tick(BossBar bossBar);

}
