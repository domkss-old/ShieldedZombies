package net.domkss.shieldedzombies.goals;

public interface ShieldBlockCooldownTracker {
    int getShieldCooldown();
    void setShieldCooldown(int ticks);
}
