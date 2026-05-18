package visitintramuros;

import visitintramuros.view.HomeScreen;

import javax.swing.*;

/**
 * Main — entry point for VisitIntramuros.
 *
 * HOW TO RUN IN VSCODE:
 *   1. Open the VisitIntramuros folder in VSCode
 *   2. Make sure you have the "Extension Pack for Java" installed
 *   3. Right-click Main.java → Run Java
 *      OR in terminal:
 *        cd src
 *        javac -d ../out visitintramuros/interfaces/*.java visitintramuros/model/*.java visitintramuros/controller/*.java visitintramuros/view/*.java visitintramuros/Main.java
 *        java -cp ../out visitintramuros.Main
 */
public class Main {

    public static void main(String[] args) {
        // Run on the Swing Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("VisitIntramuros — Interactive Map Application");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1100, 700);
            frame.setMinimumSize(new java.awt.Dimension(900, 600));
            frame.setLocationRelativeTo(null); // center on screen

            // Start on the Home / Title screen
            frame.setContentPane(new HomeScreen(frame));
            frame.setVisible(true);
        });
    }
}
