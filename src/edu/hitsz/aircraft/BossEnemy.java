package edu.hitsz.aircraft;

/**
 * Boss 敌机
 */
public class BossEnemy extends EliteEnemy {

    public BossEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.shootNum = 3;
        this.power = 50;
    }

    @Override
    public void forward() {
        super.forward();
    }
}
