// TODO implement s-tap when I have the time

//package me.jameesyy.slant.movement;
//
//
//import com.google.common.eventbus.Subscribe;
//import net.minecraft.client.settings.KeyBinding;
//import net.minecraft.entity.Entity;
//import net.minecraft.entity.player.EntityPlayer;
//import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
//import net.minecraftforge.event.entity.player.AttackEntityEvent;
//
//import java.util.Random;
//import java.util.concurrent.ThreadLocalRandom;
//
//public class STap  {
//    public ComboSetting eventType;
//    public SliderSetting range, chance, tapMultiplier;
//    public TickSetting onlyPlayers;
//    public TickSetting onlySword;
//    public TickSetting dynamic;
//    public DoubleSliderSetting waitMs;
//    public DoubleSliderSetting actionMs;
//    public DoubleSliderSetting hitPer;
//    public int hits, rhit;
//    public boolean call, p;
//    public long s;
//    private StapState state = StapState.NONE;
//    private final CoolDown timer = new CoolDown(0);
//    private Entity target;
//
//    public Random r = new Random();
//
//    public STap() {
//        super("STap", ModuleCategory.combat);
//
//        this.registerSetting(eventType = new ComboSetting("Event:", WTap.EventType.Attack));
//        this.registerSetting(onlyPlayers = new TickSetting("Only combo players", true));
//        this.registerSetting(onlySword = new TickSetting("Only sword", false));
//        this.registerSetting(waitMs = new DoubleSliderSetting("Press s for ... ms", 30, 40, 1, 300, 1));
//        this.registerSetting(actionMs = new DoubleSliderSetting("STap after ... ms", 20, 30, 1, 300, 1));
//        this.registerSetting(chance = new SliderSetting("Chance %", 100, 0, 100, 1));
//        this.registerSetting(hitPer = new DoubleSliderSetting("Once every ... hits", 1, 1, 1, 10, 1));
//        this.registerSetting(range = new SliderSetting("Range: ", 3, 1, 6, 0.05));
//        this.registerSetting(dynamic = new TickSetting("Dynamic tap time", false));
//        this.registerSetting(tapMultiplier = new SliderSetting("wait time sensitivity", 1F, 0F, 5F, 0.1F));
//    }
//
//    @Subscribe
//    public void onRender2D(Render2DEvent e) {
//        if (state == StapState.NONE)
//            return;
//        if (state == StapState.WAITINGTOTAP && timer.hasFinished()) {
//            startCombo();
//        } else if (state == StapState.TAPPING && timer.hasFinished()) {
//            finishCombo();
//        }
//    }
//
//    @Subscribe
//    public void onForgeEvent(ForgeEvent fe) {
//        if (fe.getEvent() instanceof AttackEntityEvent) {
//            AttackEntityEvent e = ((AttackEntityEvent) fe.getEvent());
//
//            target = e.target;
//
//            if (isSecondCall() && eventType.getMode() == EventType.Attack)
//                sTap();
//        } else if (fe.getEvent() instanceof LivingUpdateEvent) {
//            LivingUpdateEvent e = ((LivingUpdateEvent) fe.getEvent());
//
//            if (eventType.getMode() == EventType.Hurt && e.entityLiving.hurtTime > 0
//                    && e.entityLiving.hurtTime == e.entityLiving.maxHurtTime && e.entity == this.target)
//                sTap();
//        }
//    }
//
//    public void sTap() {
//        if (state != StapState.NONE)
//            return;
//        if (!(Math.random() <= chance.getInput() / 100)) {
//            hits++;
//        }
//        if (mc.thePlayer.getDistanceToEntity(target) > range.getInput()
//                || (onlyPlayers.isToggled() && !(target instanceof EntityPlayer))
//                || (onlySword.isToggled() && !Utils.Player.isPlayerHoldingSword()) || !(rhit >= hits))
//            return;
//        trystartCombo();
//    }
//
//    public void finishCombo() {
//        state = StapState.NONE;
//        KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), false);
//        hits = 0;
//        int easports = (int) (hitPer.getInputMax() - hitPer.getInputMin() + 1);
//        rhit = ThreadLocalRandom.current().nextInt((easports));
//        rhit += (int) hitPer.getInputMin();
//    }
//
//    public void startCombo() {
//        state = StapState.TAPPING;
//        KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), true);
//        double cd = ThreadLocalRandom.current().nextDouble(waitMs.getInputMin(), waitMs.getInputMax() + 0.01);
//        if (dynamic.isToggled() && mc.thePlayer != null && target != null) {
//            cd = 3 - mc.thePlayer.getDistanceToEntity(target) < 3
//                    ? (cd + (3 - mc.thePlayer.getDistanceToEntity(target) * tapMultiplier.getInput() * 10))
//                    : cd;
//        }
//
//        timer.setCooldown((long) cd);
//        timer.start();
//    }
//
//    public void trystartCombo() {
//        state = StapState.WAITINGTOTAP;
//        timer.setCooldown(
//                (long) ThreadLocalRandom.current().nextDouble(actionMs.getInputMin(), actionMs.getInputMax() + 0.01));
//        timer.start();
//    }
//
//    public void guiButtonToggled(TickSetting b) {
//
//    }
//
//    private boolean isSecondCall() {
//        if (call) {
//            call = false;
//            return true;
//        } else {
//            call = true;
//            return false;
//        }
//    }
//
//    public enum StapState {
//        NONE, WAITINGTOTAP, TAPPING,
//    }
//}
