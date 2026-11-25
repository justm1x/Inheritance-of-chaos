package com.inheritanceofchaos.battle;

// Design pattern "Observer".
// The battle does not know anything about Swing, it just tells its listeners what happened.
// The game window is a listener and redraws itself.
//
// Careful: these methods are called from the BATTLE thread,
// so the window has to use SwingUtilities.invokeLater before touching any Swing components.
public interface BattleListener {

    void onLog(String message, LogType type);

    void onUpdate(BattleSnapshot snapshot);

    void onBattleEnd(BattleOutcome outcome);
}
