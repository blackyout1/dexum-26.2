package dexum.client.mixins.interfaces;

import net.minecraft.world.phys.AABB;

public interface IEntity {
    default AABB getEntityBoundingBox() { return null; }
    default void setEntityBoundingBox(AABB box) {}
}
