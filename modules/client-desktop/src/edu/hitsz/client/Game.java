package edu.hitsz.client;

import edu.hitsz.client.aircraft.AbstractAircraft;
import edu.hitsz.client.aircraft.HeroAircraft;
import edu.hitsz.client.basic.AbstractFlyingObject;
import edu.hitsz.client.basic.AbstractItem;
import edu.hitsz.client.bullet.BaseBullet;
import edu.hitsz.common.AircraftBranch;
import edu.hitsz.common.BranchUpgradeChoice;
import edu.hitsz.common.ChapterId;
import edu.hitsz.common.EntityRenderSizing;
import edu.hitsz.common.GameConstants;
import edu.hitsz.common.GamePhase;
import edu.hitsz.common.protocol.dto.LaserSnapshot;
import edu.hitsz.common.protocol.dto.ExplosionSnapshot;
import edu.hitsz.common.protocol.dto.WorldSnapshot;
import edu.hitsz.common.protocol.SnapshotTypes;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * 游戏主面板。
 * 联机模式下仅负责输入转发、快照应用和本地渲染。
 * @author hitsz
 */
public class Game extends JPanel {

    private int backGroundTop = 0;

    private final Timer timer;
    private final int timeInterval = 40;

    private final HeroAircraft heroAircraft;
    private final List<AbstractAircraft> playerAircrafts;
    private final ClientWorldState clientWorldState;
    private final SnapshotApplier snapshotApplier;
    private final GameAudioStateMachine gameAudioStateMachine;
    private ClientCommandPublisher commandPublisher;
    private String localSessionId;
    private final List<AbstractAircraft> enemyAircrafts;
    private final List<BaseBullet> heroBullets;
    private final List<BaseBullet> enemyBullets;
    private final List<LaserSnapshot> activeLasers;
    private final List<ExplosionSnapshot> explosionSnapshots;
    private final List<AbstractItem> items;

    //当前玩家分数
    private int localHp = 0;
    private int score = 0;
    private int level = 1;
    private String localSelectedSkill;
    private AircraftBranch localAircraftBranch = AircraftBranch.STARTER_BLUE;
    private boolean localBranchUnlocked;
    private long localSkillCooldownRemainingMillis;
    private List<AircraftBranch> localAvailableBranchChoices = java.util.Collections.emptyList();
    private List<BranchUpgradeChoice> localAvailableUpgradeChoices = java.util.Collections.emptyList();
    private BranchUpgradeChoice localSelectedUpgradeChoice;
    private boolean localReady;
    private boolean gameStarted;
    private int readyPlayerCount;
    private int connectedPlayerCount;
    private String difficulty = "NORMAL";
    private String roomCode;
    private String hostSessionId;
    private boolean localHost;
    private int totalScore;
    private boolean bossActive;
    private int nextBossScoreThreshold;
    private ChapterId chapterId = ChapterId.CH1;
    private GamePhase gamePhase = GamePhase.LOBBY;
    private boolean chapterTransitionFlash;

    public Game() {
        heroAircraft = HeroAircraft.getSingleton();
        heroAircraft.setLocation(
                GameConstants.WINDOW_WIDTH / 2,
                GameConstants.WINDOW_HEIGHT - EntityRenderSizing.HERO_HEIGHT
        );

        enemyAircrafts = new LinkedList<>();
        playerAircrafts = new LinkedList<>();
        clientWorldState = new ClientWorldState();
        snapshotApplier = new DefaultSnapshotApplier();
        gameAudioStateMachine = new GameAudioStateMachine();
        heroBullets = new LinkedList<>();
        enemyBullets = new LinkedList<>();
        activeLasers = new LinkedList<>();
        explosionSnapshots = new LinkedList<>();
        items = new LinkedList<>();
        playerAircrafts.add(heroAircraft);
        clientWorldState.getPlayerAircrafts().add(heroAircraft);

        //启动英雄机鼠标监听
        new HeroController(this, heroAircraft);

        this.timer = new Timer(timeInterval, e -> {
            repaint();
            checkResultAction();
        });
    }

    public void handleLocalHeroInput(int x, int y) {
        if (commandPublisher != null) {
            commandPublisher.publishMove(x, y);
        }
    }

    public void handleLocalSkillInput(String skillType) {
        if (commandPublisher != null) {
            commandPublisher.publishSkill(skillType);
        }
    }

    public void handleLocalReadyToggle() {
        if (commandPublisher != null) {
            commandPublisher.publishReady(!localReady);
        }
    }

    public void handleLocalSkillSelection(String skillType) {
        // Lobby-time skill selection has been removed for now.
    }

    public void handleLocalBranchChoice(String branch) {
        if (commandPublisher != null && isBranchSelectionVisible()) {
            commandPublisher.publishBranchChoice(branch);
        }
    }

    public void handleLocalBranchChoiceByIndex(int index) {
        if (!isBranchSelectionVisible()) {
            return;
        }
        if (index < 0 || index >= localAvailableBranchChoices.size()) {
            return;
        }
        handleLocalBranchChoice(localAvailableBranchChoices.get(index).name());
    }

    public void handleLocalUpgradeChoice(String choice) {
        if (commandPublisher != null && isUpgradeSelectionVisible()) {
            commandPublisher.publishUpgradeChoice(choice);
        }
    }

    public void handleLocalUpgradeChoiceByIndex(int index) {
        if (!isUpgradeSelectionVisible()) {
            return;
        }
        if (index < 0 || index >= localAvailableUpgradeChoices.size()) {
            return;
        }
        handleLocalUpgradeChoice(localAvailableUpgradeChoices.get(index).name());
    }

    public void handleLocalSkillCast() {
        if (localBranchUnlocked && localSelectedSkill != null) {
            handleLocalSkillInput(localSelectedSkill);
        }
    }

    public void handleLocalStartRequest() {
        if (commandPublisher != null && !gameStarted && localHost) {
            commandPublisher.publishStartGame();
        }
    }

    public List<AbstractAircraft> getPlayerAircrafts() {
        return playerAircrafts;
    }

    public List<AbstractAircraft> getEnemyAircrafts() {
        return enemyAircrafts;
    }

    public List<BaseBullet> getHeroBullets() {
        return heroBullets;
    }

    public List<BaseBullet> getEnemyBullets() {
        return enemyBullets;
    }

    public List<AbstractItem> getItems() {
        return items;
    }

    public void applyWorldSnapshot(WorldSnapshot snapshot) {
        snapshotApplier.apply(snapshot, clientWorldState, localSessionId);
        playerAircrafts.clear();
        playerAircrafts.addAll(clientWorldState.getPlayerAircrafts());
        enemyAircrafts.clear();
        enemyAircrafts.addAll(clientWorldState.getEnemyAircrafts());
        heroBullets.clear();
        heroBullets.addAll(clientWorldState.getHeroBullets());
        enemyBullets.clear();
        enemyBullets.addAll(clientWorldState.getEnemyBullets());
        activeLasers.clear();
        activeLasers.addAll(clientWorldState.getActiveLasers());
        explosionSnapshots.clear();
        explosionSnapshots.addAll(clientWorldState.getExplosionSnapshots());
        items.clear();
        items.addAll(clientWorldState.getItems());
        localHp = clientWorldState.getLocalHp();
        score = clientWorldState.getLocalScore();
        level = clientWorldState.getLocalLevel();
        localSelectedSkill = clientWorldState.getLocalSelectedSkill();
        localAircraftBranch = clientWorldState.getLocalAircraftBranch();
        localBranchUnlocked = clientWorldState.isLocalBranchUnlocked();
        localSkillCooldownRemainingMillis = clientWorldState.getLocalSkillCooldownRemainingMillis();
        localAvailableBranchChoices = clientWorldState.getLocalAvailableBranchChoices();
        localAvailableUpgradeChoices = clientWorldState.getLocalAvailableUpgradeChoices();
        localSelectedUpgradeChoice = clientWorldState.getLocalSelectedUpgradeChoice();
        localReady = clientWorldState.isLocalReady();
        gameStarted = clientWorldState.isGameStarted();
        readyPlayerCount = clientWorldState.getReadyPlayerCount();
        connectedPlayerCount = clientWorldState.getConnectedPlayerCount();
        difficulty = clientWorldState.getDifficulty();
        roomCode = clientWorldState.getRoomCode();
        hostSessionId = clientWorldState.getHostSessionId();
        localHost = clientWorldState.isLocalHost();
        totalScore = clientWorldState.getTotalScore();
        bossActive = clientWorldState.isBossActive();
        nextBossScoreThreshold = clientWorldState.getNextBossScoreThreshold();
        chapterId = clientWorldState.getChapterId();
        gamePhase = clientWorldState.getGamePhase();
        chapterTransitionFlash = clientWorldState.isChapterTransitionFlash();
        AudioManager.getInstance().apply(
                gameAudioStateMachine.onSnapshot(
                gameStarted,
                bossActive,
                localHp,
                explosionSnapshots.size(),
                clientWorldState.getBulletHitAudioCount(),
                clientWorldState.getSupplyPickupAudioCount()
                )
        );
    }

    public void attachCommandPublisher(ClientCommandPublisher commandPublisher) {
        this.commandPublisher = commandPublisher;
    }

    public void setLocalSessionId(String localSessionId) {
        this.localSessionId = localSessionId;
    }

    public ChapterId getChapterId() {
        return chapterId;
    }

    public int getLocalHp() {
        return localHp;
    }

    public AircraftBranch getLocalAircraftBranch() {
        return localAircraftBranch;
    }

    public boolean isLocalPlayerAlive() {
        return localHp > 0;
    }

    public GamePhase getGamePhase() {
        return gamePhase;
    }

    public boolean isChapterTransitionFlash() {
        return chapterTransitionFlash;
    }

    public boolean isUpgradeSelectionVisible() {
        return gameStarted
                && gamePhase == GamePhase.UPGRADE_SELECTION
                && !chapterTransitionFlash
                && !localAvailableUpgradeChoices.isEmpty();
    }

    public boolean isBranchSelectionVisible() {
        return gameStarted
                && gamePhase == GamePhase.BRANCH_SELECTION
                && !chapterTransitionFlash
                && !localAvailableBranchChoices.isEmpty();
    }

    /**
     * 游戏启动入口，定时重绘并基于快照检查结束状态。
     */
    public void action() {
        if (!timer.isRunning()) {
            timer.start();
        }
    }

    /**
     * 基于服务器同步的英雄机状态判断游戏是否结束。
     */
    private void checkResultAction() {
        // Keep repainting across death/lobby transitions; the server snapshot is authoritative.
    }

    //***********************
    //      Paint 各部分
    //***********************
    /**
     * 重写 paint方法
     * 通过重复调用paint方法，实现游戏动画
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);

        // 绘制背景,图片滚动
        BufferedImage currentBackground = ImageManager.backgroundFor(chapterId, bossActive);
        g.drawImage(currentBackground, 0, this.backGroundTop - GameConstants.WINDOW_HEIGHT, null);
        g.drawImage(currentBackground, 0, this.backGroundTop, null);
        this.backGroundTop += 1;
        if (this.backGroundTop == GameConstants.WINDOW_HEIGHT) {
            this.backGroundTop = 0;
        }

        // 先绘制子弹，后绘制飞机
        // 这样子弹显示在飞机的下层
        paintImageWithPositionRevised(g, enemyBullets);
        paintImageWithPositionRevised(g, heroBullets);
        paintImageWithPositionRevised(g, playerAircrafts);
        paintImageWithPositionRevised(g, enemyAircrafts);
        paintImageWithPositionRevised(g, items);
        paintLaserBeams(g);
        paintExplosionBursts(g);

        //绘制得分和生命值
        paintScoreAndLife(g);
        paintLobbyHint(g);
        paintBranchSelectionOverlay(g);
        paintUpgradeSelectionOverlay(g);

    }

    private void paintImageWithPositionRevised(Graphics g, List<? extends AbstractFlyingObject> objects) {
        if (objects.isEmpty()) {
            return;
        }

        for (AbstractFlyingObject object : objects) {
            BufferedImage image = ImageManager.get(object, chapterId);
            if (image == null) {
                image = object.getImage();
            }
            assert image != null : objects.getClass().getName() + " has no image! ";
            g.drawImage(image,
                    object.getLocationX() - object.getRenderWidth() / 2,
                    object.getLocationY() - object.getRenderHeight() / 2,
                    object.getRenderWidth(),
                    object.getRenderHeight(),
                    null);
        }
    }

    private void paintLaserBeams(Graphics g) {
        if (activeLasers.isEmpty()) {
            return;
        }
        Graphics2D graphics = (Graphics2D) g.create();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (LaserSnapshot laser : activeLasers) {
                int endX = laser.getOriginX() + (int) Math.round(Math.cos(laser.getAngle()) * laser.getLength());
                int endY = laser.getOriginY() + (int) Math.round(Math.sin(laser.getAngle()) * laser.getLength());
                paintLaserByStyle(graphics, laser, endX, endY);
            }
        } finally {
            graphics.dispose();
        }
    }

    private void paintLaserByStyle(Graphics2D graphics, LaserSnapshot laser, int endX, int endY) {
        if (SnapshotTypes.Laser.BOSS_WARNING.equals(laser.getStyle())) {
            float charge = (float) Math.max(0.0, Math.min(1.0, laser.getChargeRatio()));
            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.12f + 0.30f * charge));
            graphics.setStroke(new BasicStroke(
                    Math.max(1.0f, laser.getWidth()),
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND
            ));
            graphics.setColor(new Color(255, 120, 120));
            graphics.drawLine(laser.getOriginX(), laser.getOriginY(), endX, endY);

            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.20f + 0.50f * charge));
            graphics.setStroke(new BasicStroke(
                    Math.max(2.0f, laser.getWidth() / 4.0f),
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND
            ));
            graphics.setColor(new Color(255, 180 - (int) (80 * charge), 180 - (int) (120 * charge)));
            graphics.drawLine(laser.getOriginX(), laser.getOriginY(), endX, endY);
            return;
        }
        if (SnapshotTypes.Laser.BOSS_FIRING.equals(laser.getStyle())) {
            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
            graphics.setStroke(new BasicStroke(
                    Math.max(1.0f, laser.getWidth()),
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND
            ));
            graphics.setColor(new Color(180, 0, 0));
            graphics.drawLine(laser.getOriginX(), laser.getOriginY(), endX, endY);

            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));
            graphics.setStroke(new BasicStroke(
                    Math.max(3.0f, laser.getWidth() / 3.0f),
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND
            ));
            graphics.setColor(new Color(255, 240, 240));
            graphics.drawLine(laser.getOriginX(), laser.getOriginY(), endX, endY);
            return;
        }

        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
        graphics.setStroke(new BasicStroke(
                Math.max(1.0f, laser.getWidth()),
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND
        ));
        graphics.setColor(new Color(255, 64, 64));
        graphics.drawLine(laser.getOriginX(), laser.getOriginY(), endX, endY);

        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f));
        graphics.setStroke(new BasicStroke(
                Math.max(2.0f, laser.getWidth() / 3.0f),
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND
        ));
        graphics.setColor(new Color(255, 240, 180));
        graphics.drawLine(laser.getOriginX(), laser.getOriginY(), endX, endY);
    }

    private void paintExplosionBursts(Graphics g) {
        if (explosionSnapshots.isEmpty()) {
            return;
        }
        Graphics2D graphics = (Graphics2D) g.create();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (ExplosionSnapshot explosion : explosionSnapshots) {
                int radius = Math.max(8, explosion.getRadius());
                int diameter = radius * 2;
                int left = explosion.getX() - radius;
                int top = explosion.getY() - radius;

                graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.28f));
                graphics.setColor(new Color(255, 140, 40));
                graphics.fillOval(left, top, diameter, diameter);

                graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
                graphics.setStroke(new BasicStroke(4.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                graphics.setColor(new Color(255, 245, 200));
                graphics.drawOval(left, top, diameter, diameter);

                graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));
                graphics.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                graphics.setColor(new Color(255, 210, 120));
                graphics.drawOval(left + radius / 3, top + radius / 3, diameter - radius / 2, diameter - radius / 2);
            }
        } finally {
            graphics.dispose();
        }
    }

    private void paintScoreAndLife(Graphics g) {
        int x = 10;
        int y = 25;
        g.setColor(Color.RED);
        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        g.drawString("SCORE: " + this.score, x, y);
        y = y + 20;
        g.drawString("LIFE: " + this.localHp, x, y);
        y = y + 20;
        g.drawString("LEVEL: " + this.level, x, y);
        y = y + 20;
        g.drawString(buildSkillStatusText(), x, y);
        y = y + 20;
        g.drawString("ROOM: " + (roomCode == null ? "-" : roomCode), x, y);
        y = y + 20;
        g.drawString("DIFF: " + this.difficulty, x, y);
        y = y + 20;
        g.drawString("TOTAL: " + this.totalScore, x, y);
        y = y + 20;
        g.drawString(bossActive ? "BOSS: ACTIVE" : "BOSS AT: " + this.nextBossScoreThreshold, x, y);
    }

    private void paintLobbyHint(Graphics g) {
        if (gameStarted) {
            if (isLocalPlayerAlive()) {
                if (bossActive) {
                    g.setColor(new Color(90, 0, 0, 150));
                    g.fillRoundRect(150, 20, 220, 36, 16, 16);
                    g.setColor(Color.WHITE);
                    g.setFont(new Font("SansSerif", Font.BOLD, 20));
                    g.drawString("BOSS INBOUND", 183, 45);
                }
                return;
            }
            g.setColor(new Color(0, 0, 0, 160));
            g.fillRoundRect(60, GameConstants.WINDOW_HEIGHT / 2 - 35, 392, 70, 18, 18);
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 22));
            g.drawString("You are down. Waiting for round end...", 88, GameConstants.WINDOW_HEIGHT / 2 + 8);
            return;
        }

        g.setColor(new Color(0, 0, 0, 160));
        g.fillRoundRect(48, GameConstants.WINDOW_HEIGHT / 2 - 74, 416, 148, 18, 18);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        g.drawString("Room Lobby", 182, GameConstants.WINDOW_HEIGHT / 2 - 34);
        g.setFont(new Font("SansSerif", Font.PLAIN, 16));
        String[] lines = buildLobbyOverlayLines();
        int y = GameConstants.WINDOW_HEIGHT / 2 - 8;
        for (String line : lines) {
            g.drawString(line, 74, y);
            y += 22;
        }
    }

    private void paintUpgradeSelectionOverlay(Graphics g) {
        if (!isUpgradeSelectionVisible()) {
            return;
        }
        g.setColor(new Color(255, 255, 255, 210));
        g.fillRoundRect(58, GameConstants.WINDOW_HEIGHT / 2 - 90, 396, 180, 18, 18);
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        g.drawString("Choose Upgrade", 180, GameConstants.WINDOW_HEIGHT / 2 - 52);
        g.setFont(new Font("SansSerif", Font.PLAIN, 16));
        int y = GameConstants.WINDOW_HEIGHT / 2 - 18;
        for (int i = 0; i < localAvailableUpgradeChoices.size(); i++) {
            BranchUpgradeChoice choice = localAvailableUpgradeChoices.get(i);
            String line = (i + 1) + ". " + upgradeChoiceLabel(choice);
            if (choice == localSelectedUpgradeChoice) {
                line += " (SELECTED)";
            }
            g.drawString(line, 86, y);
            y += 24;
        }
    }

    private void paintBranchSelectionOverlay(Graphics g) {
        if (!isBranchSelectionVisible()) {
            return;
        }
        g.setColor(new Color(245, 245, 255, 220));
        g.fillRoundRect(40, GameConstants.WINDOW_HEIGHT / 2 - 108, 432, 216, 18, 18);
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        g.drawString("Choose Branch", 178, GameConstants.WINDOW_HEIGHT / 2 - 68);
        g.setFont(new Font("SansSerif", Font.PLAIN, 16));
        int y = GameConstants.WINDOW_HEIGHT / 2 - 30;
        for (int i = 0; i < localAvailableBranchChoices.size(); i++) {
            g.drawString((i + 1) + ". " + branchChoiceLabel(localAvailableBranchChoices.get(i)), 80, y);
            y += 28;
        }
    }

    private String[] buildLobbyOverlayLines() {
        return new String[]{
                "Enter ready   Host uses S to start",
                "Ready " + readyPlayerCount + "/" + connectedPlayerCount
                        + "   You " + (localReady ? "READY" : "WAITING"),
                "Host " + displayHostLabel() + "   Room " + displayRoomCode(),
                "Diff " + difficulty + "   Plane " + displayAircraftBranch(),
                "Boss at " + nextBossScoreThreshold + "   Total " + totalScore
        };
    }

    private String displayHostLabel() {
        if (localHost) {
            return "YOU";
        }
        if (hostSessionId == null || hostSessionId.trim().isEmpty()) {
            return "-";
        }
        String label = hostSessionId;
        if (label.startsWith("session-")) {
            label = label.substring("session-".length());
        }
        if (label.length() <= 10) {
            return label;
        }
        return label.substring(0, 10) + "...";
    }

    private String displayRoomCode() {
        if (roomCode == null || roomCode.trim().isEmpty()) {
            return "-";
        }
        return roomCode;
    }

    private String buildSkillStatusText() {
        if (localSelectedSkill == null || localSelectedSkill.trim().isEmpty()) {
            return "AIRCRAFT: " + displayAircraftBranch();
        }
        if (localSkillCooldownRemainingMillis <= 0L) {
            return "SKILL: " + displaySelectedSkill() + " (READY)";
        }
        double cooldownSeconds = localSkillCooldownRemainingMillis / 1000.0;
        return String.format(Locale.US, "SKILL: %s (CD %.1fs)", displaySelectedSkill(), cooldownSeconds);
    }

    private String displayAircraftBranch() {
        if (localAircraftBranch == null) {
            return "-";
        }
        switch (localAircraftBranch) {
            case STARTER_BLUE:
                return "Starter Blue";
            case RED_SPEED:
                return "Red Speed";
            case GREEN_DEFENSE:
                return "Green Defense";
            case BLACK_HEAVY:
                return "Black Heavy";
            default:
                return localAircraftBranch.name();
        }
    }

    private String displaySelectedSkill() {
        if (localSelectedSkill == null || localSelectedSkill.trim().isEmpty()) {
            return "-";
        }
        return localSelectedSkill;
    }

    private String upgradeChoiceLabel(BranchUpgradeChoice choice) {
        switch (choice) {
            case LASER_DAMAGE:
                return "Laser damage";
            case LASER_WIDTH:
                return "Laser width";
            case LASER_DURATION:
                return "Laser duration";
            case MOVE_SPEED:
                return "Move speed";
            case SPREAD_COUNT:
                return "More spread shots";
            case SPREAD_WIDTH:
                return "Wider spread";
            case BULLET_DAMAGE:
                return "Bullet damage";
            case MAX_HP:
                return "Max HP";
            case AIRBURST_DAMAGE:
                return "Airburst damage";
            case AIRBURST_RADIUS:
                return "Airburst radius";
            case AIRBURST_RANGE:
                return "Airburst range";
            default:
                return choice.name();
        }
    }

    private String branchChoiceLabel(AircraftBranch branch) {
        switch (branch) {
            case RED_SPEED:
                return "Red Speed";
            case GREEN_DEFENSE:
                return "Green Defense";
            case BLACK_HEAVY:
                return "Black Heavy";
            case STARTER_BLUE:
            default:
                return "Starter Blue";
        }
    }

}
