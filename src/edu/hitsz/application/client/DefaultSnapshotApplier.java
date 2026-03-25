package edu.hitsz.application.client;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.aircraft.OtherPlayer;
import edu.hitsz.application.protocol.dto.PlayerSnapshot;
import edu.hitsz.application.protocol.dto.WorldSnapshot;

public class DefaultSnapshotApplier implements SnapshotApplier {

    @Override
    public void apply(WorldSnapshot snapshot, ClientWorldState state) {
        state.getPlayerAircrafts().clear();
        for (PlayerSnapshot playerSnapshot : snapshot.getPlayerSnapshots()) {
            if (playerSnapshot.isLocalPlayer()) {
                HeroAircraft heroAircraft = HeroAircraft.getSingleton();
                heroAircraft.setLocation(playerSnapshot.getX(), playerSnapshot.getY());
                heroAircraft.setHp(playerSnapshot.getHp());
                state.getPlayerAircrafts().add(heroAircraft);
                continue;
            }
            state.getPlayerAircrafts().add(new OtherPlayer(
                    playerSnapshot.getPlayerId(),
                    playerSnapshot.getX(),
                    playerSnapshot.getY(),
                    0,
                    0,
                    playerSnapshot.getHp()
            ));
        }
    }
}
