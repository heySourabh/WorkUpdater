/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uninstaller;

import java.io.File;
import java.util.Scanner;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 *
 * @author Sourabh Bhat
 */
public class Uninstaller {

    /**
     * @param args the command line arguments
     */
    static String installationPath = "D:\\";
    static String programName = "PeriodicWorkUpdate.jar";

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        JFileChooser fileChooser = new JFileChooser(installationPath);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setApproveButtonText("Installed Here");
        fileChooser.setDialogTitle("Select Installation Directory");
        int selectedAction = fileChooser.showOpenDialog(null);
        if(selectedAction == JFileChooser.CANCEL_OPTION) {
            JOptionPane.showMessageDialog(null, "Installation folder not selected!", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        installationPath = fileChooser.getSelectedFile().getAbsolutePath();
        
        int response = JOptionPane.showOptionDialog(null,
                "<html>Are you sure you want to <FONT COLOR=RED><B>uninstall</B></FONT> WorkUpdater software?<br>" +
                "This will however not delete the file with work updates</html>",
                "Confirm Uninstall",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE, null,
                new String[]{"<html><font color=RED>Uninstall</font></html>", "Exit"},
                "Exit");
        if (response != 0) {
            System.out.println("Not Uninstalling software...");
        } else {
            System.out.println("Uninstalling software...");
            // Check if registry has Workupdater key
            System.out.println("Scanning registry for \"WorkUpdater\" key");
            String regCommand = "reg query HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run /v WorkUpdater";
            boolean regHasWorkUpdater = false;
            try {
                Process p = Runtime.getRuntime().exec(regCommand);
                Scanner sc = new Scanner(p.getInputStream());
                while (sc.hasNextLine()) {
                    if (sc.nextLine().indexOf("WorkUpdater") != -1) {
                        System.out.println("Registry has WorkUpdater");
                        regHasWorkUpdater = true;
                    } else {
                        System.out.println("Registry does not have WorkUpdater");
                    }
                }
                p.waitFor();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            // Delete registry key if exists
            regCommand = "reg delete HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run /v WorkUpdater /f";
            if (regHasWorkUpdater) {
                System.out.println("Deleting registry key...");
                try {
                    Process p = Runtime.getRuntime().exec(regCommand);
                    // If delete is unsuccessful exit with message
                    if (p.waitFor() != 0) {
                        JOptionPane.showMessageDialog(null,
                                "Error deleting registry",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        System.exit(1);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(null,
                        "Registry for \"WorkUpdater\" doesnot exist for current user",
                        "No registry entry",
                        JOptionPane.INFORMATION_MESSAGE);
                System.exit(1);
            }
            // Delete software
            // Check Work updater software exists

            File prog = new File(installationPath, programName);
            if (prog.exists()) {
                // Delete software
                System.out.println("Deleting program file");
                boolean fileDeleted = prog.delete();
                if (!fileDeleted){
                    JOptionPane.showMessageDialog(null, "Could not delete the software from computer...\n" +
                            "Please stop program & delete program manually from directory : " + installationPath,
                            "Error occured while deleting program file", JOptionPane.ERROR_MESSAGE);
                }
            }

            JOptionPane.showMessageDialog(null, "Uninstallation over");
        }
    }
}
