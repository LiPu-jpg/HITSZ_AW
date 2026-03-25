package edu.hitsz.aircraft;

/**
 * 强化精英敌机
 */
public class ElitePlusEnemy extends EliteEnemy {

    public ElitePlusEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.power = 35;
    }

    @Override
    public void forward() {
        super.forward();
    }
}
