package com.inheritanceofchaos;

import com.inheritanceofchaos.ui.GameWindow;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

// Start of the program.
// Swing windows must be created on the Swing thread (Event Dispatch Thread),
// that is why everything is started inside SwingUtilities.invokeLater.
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // make the window look like a normal Windows program
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // not important, the default look is fine too
            }
            GameWindow window = new GameWindow();
            window.setVisible(true);
            window.showStartMenu();
        });
    }
}
