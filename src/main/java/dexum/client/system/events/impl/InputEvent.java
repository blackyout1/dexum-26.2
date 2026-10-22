package dexum.client.system.events.impl;

import dexum.client.system.events.Event;

/**
 * Событие ввода с клавиатуры. Фейрится в патче KeyboardInput.tick()
 * ДО того как движение уйдёт в player.travel().
 *
 * Используется Aura для Silent Move Fix — пересчёт forward/strafe
 * под серверный yaw атаки.
 */
public class InputEvent implements Event {

    private float movementForward;
    private float movementStrafe;

    public InputEvent(float movementForward, float movementStrafe) {
        this.movementForward = movementForward;
        this.movementStrafe  = movementStrafe;
    }

    public float getMovementForward() { return movementForward; }
    public void  setMovementForward(float v) { this.movementForward = v; }

    public float getMovementStrafe()  { return movementStrafe; }
    public void  setMovementStrafe(float v)  { this.movementStrafe = v; }
}