package edu.hitsz.client;

import edu.hitsz.client.aircraft.HeroAircraft;
import edu.hitsz.common.GameConstants;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 英雄机控制类
 * 监听鼠标，控制英雄机的移动
 * @author hitsz
 */
public class HeroController {
    private final Game game;
    private final HeroAircraft heroAircraft;
    private final MouseAdapter mouseAdapter;
    private final KeyAdapter keyAdapter;

    public HeroController(Game game, HeroAircraft heroAircraft){
        this.game = game;
        this.heroAircraft = heroAircraft;

        mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                int x = e.getX();
                int y = e.getY();
                if ( x<0 || x>GameConstants.WINDOW_WIDTH || y<0 || y>GameConstants.WINDOW_HEIGHT){
                    // 防止超出边界
                    return;
                }
                game.handleLocalHeroInput(x, y);
            }
        };

        keyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_1:
                        if (game.isBranchSelectionVisible()) {
                            game.handleLocalBranchChoiceByIndex(0);
                        } else if (game.isUpgradeSelectionVisible()) {
                            game.handleLocalUpgradeChoiceByIndex(0);
                        }
                        break;
                    case KeyEvent.VK_2:
                        if (game.isBranchSelectionVisible()) {
                            game.handleLocalBranchChoiceByIndex(1);
                        } else if (game.isUpgradeSelectionVisible()) {
                            game.handleLocalUpgradeChoiceByIndex(1);
                        }
                        break;
                    case KeyEvent.VK_3:
                        if (game.isBranchSelectionVisible()) {
                            game.handleLocalBranchChoiceByIndex(2);
                        } else if (game.isUpgradeSelectionVisible()) {
                            game.handleLocalUpgradeChoiceByIndex(2);
                        }
                        break;
                    case KeyEvent.VK_4:
                        if (game.isUpgradeSelectionVisible()) {
                            game.handleLocalUpgradeChoiceByIndex(3);
                        }
                        break;
                    case KeyEvent.VK_SPACE:
                        game.handleLocalSkillCast();
                        break;
                    case KeyEvent.VK_ENTER:
                        game.handleLocalReadyToggle();
                        break;
                    case KeyEvent.VK_S:
                        game.handleLocalStartRequest();
                        break;
                    default:
                        break;
                }
            }
        };

        game.addMouseListener(mouseAdapter);
        game.addMouseMotionListener(mouseAdapter);
        game.addKeyListener(keyAdapter);
        game.setFocusable(true);
    }


}
