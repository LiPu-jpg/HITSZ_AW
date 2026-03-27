package edu.hitsz.client;

import edu.hitsz.client.aircraft.HeroAircraft;
import edu.hitsz.client.aircraft.OtherPlayer;

public class PlayerAircraftModelTest {

    public static void main(String[] args) {
        HeroAircraft hero1 = HeroAircraft.getSingleton();
        HeroAircraft hero2 = HeroAircraft.getSingleton();
        OtherPlayer teammateA = new OtherPlayer("p2", 100, 100, 0, 0, 1000);
        OtherPlayer teammateB = new OtherPlayer("p3", 120, 100, 0, 0, 1000);

        assert hero1 == hero2 : "HeroAircraft must remain singleton";
        assert teammateA != teammateB : "OtherPlayer must not be singleton";
    }
}
