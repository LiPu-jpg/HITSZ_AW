package edu.hitsz.aircraft;

/**
 * 王牌敌机
 */
public class AceEnemy extends EliteEnemy {

    public AceEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.shootNum = 2;
        this.power = 40;
    }

    @Override
    public void forward() {
        super.forward();
    }
}
