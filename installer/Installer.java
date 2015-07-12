package installer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 *
 * @author Sourabh Bhat
 */
public class Installer {

    static String installationFolder = "D:\\";
    static String fileName = "PeriodicWorkUpdate.jar";

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // Start screen with install button
        BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics g = image.getGraphics();
        g.setColor(Color.green);
        g.fillOval(0, 0, 32, 32);
        String[] options = {"Install", "Cancel"};
        JOptionPane startPane = new JOptionPane("Installer for Work Update Notifier",
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.DEFAULT_OPTION,
                new ImageIcon(image), options, options[0]);
        JDialog installerDialog = startPane.createDialog("Installing Work updater");
        installerDialog.setIconImage(image);
        installerDialog.setVisible(true);
        if (startPane.getValue() == null || !startPane.getValue().equals("Install")) {
            System.out.println("Installation aborted.");
            System.exit(1);
        }
        JFileChooser fileChooser = new JFileChooser(installationFolder);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setApproveButtonText("Select Installation Directory");
        fileChooser.setDialogTitle("Select Installation Directory");
        int selectedAction = fileChooser.showOpenDialog(installerDialog);
        if(selectedAction == JFileChooser.CANCEL_OPTION) {
            JOptionPane.showMessageDialog(installerDialog, "Installation folder not selected!", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        installationFolder = new File(fileChooser.getSelectedFile().getAbsoluteFile(), "WorkUpdater").getAbsolutePath();
        installerDialog.dispose();
        System.out.println("Installation started...");

        // Copy file from jar to installation folder
        File copyLocationFolder = new File(installationFolder);
        if (!copyLocationFolder.exists()) {
            if (!copyLocationFolder.mkdirs()) {
                JOptionPane.showMessageDialog(installerDialog,
                        "Unable to create destination folder",
                        "Error creating folder", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        }
        File copyLocationFile = new File(installationFolder, fileName);
        try {
            File programFile = null;
            programFile = new File(ClassLoader.getSystemResource(fileName).getFile());
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(installerDialog, "Corrupted Installer.\n" +
                    "Please report this error to heySourabh@gmail.com",
                    "Corrupted installer", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        InputStream fin = null;
        FileOutputStream fout;
        try {
            fin = ClassLoader.getSystemResourceAsStream(fileName);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(installerDialog, "Unable to copy program files from installer!\n" +
                    "Please report this error",
                    "Error copying program files",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        try {
            fout = new FileOutputStream(copyLocationFile);
            byte[] b = new byte[512];
            while (true) {
                int copiedBytes = fin.read(b);
                if (copiedBytes == -1) {
                    break;
                }
                fout.write(b, 0, copiedBytes);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(installerDialog, "Unable to copy program files to destination! \n" +
                    "Please report this error",
                    "Error copying program files",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        String regValue = "\"" + installationFolder + "\\" + fileName + " " +
                installationFolder + "\"";
        // Query registry key
        String commandQuery = "reg query " +
                "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run";
        System.out.println(commandQuery);
        boolean regAlreadyExists = false;
        try {
            Process p = Runtime.getRuntime().exec(commandQuery);
            InputStream inputStream = p.getInputStream();
            Scanner sc = new Scanner(inputStream);
            while (sc.hasNext()) {
                String token = sc.next();
                if (token.equals("WorkUpdater")) {
                    System.out.println("Registry entry already exists.");
                    regAlreadyExists = true;
                }
            }
            if(!regAlreadyExists){
                System.out.println("Reg key doesnot exist.");
            }
            p.waitFor();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // Set the registry key
        if (!regAlreadyExists) {
            String commandSet = "reg add " +
                    "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run " +
                    "/v WorkUpdater " +
                    "/t REG_SZ " +
                    "/d " +
                    regValue;
            try {
                Process p = Runtime.getRuntime().exec(commandSet);
                p.waitFor();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        // Start Updater!
        try {
            Runtime.getRuntime().exec("javaw -jar " + installationFolder + "\\" + fileName + " " + installationFolder, null, copyLocationFolder);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        System.out.println("Installation Over.");
        System.exit(0);
    }
}
