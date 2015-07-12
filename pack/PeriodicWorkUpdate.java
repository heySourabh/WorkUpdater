package pack;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Properties;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

/**
 *
 * @author Sourabh Bhat
 */
public class PeriodicWorkUpdate {

    static File runDirectoryPath;
    // Change this date for expiry extension
    static int expiryYear = 2025;
    static int expiryMonth = Calendar.JULY;
    static int expiryDay = 1;
    // Program variables
    static SystemTray systemTray;
    static TrayIcon trayIcon;
    static MenuItem stopItem;
    static volatile boolean stop = false;
    static MenuItem pauseItem;
    static volatile boolean pause = false;
    static MenuItem newUpdateItem;
    static MenuItem editItem;
    static MenuItem aboutItem;
    static File workFile;
    static JDialog currentDialogInstance = null;
    static FileWriter workFileWriter = null;
    static Update previousUpdate = new Update();
    static Update oldUpdate = new Update();

    public static void main(String[] args) {
        if (args.length == 1) {
            runDirectoryPath = new File(args[0]);
            if (!runDirectoryPath.isDirectory()) {
                System.out.println("Argument passed is not directory... Needs a directory path to run from");
                System.exit(1);
            }
        } else {
            System.out.println("Check usage arguments... ");
            System.exit(1);
        }
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        GregorianCalendar expiryMessageDate = new GregorianCalendar(expiryYear, expiryMonth, expiryDay);

        GregorianCalendar finalExpiryDate = new GregorianCalendar();
        finalExpiryDate.setTime(expiryMessageDate.getTime());
        finalExpiryDate.add(Calendar.DAY_OF_MONTH, 30);

        GregorianCalendar today = new GregorianCalendar();

        if (today.after(finalExpiryDate)) {
            JOptionPane.showMessageDialog(null, "This software has expired... \n"
                    + "...Please collect the latest version from Sourabh Bhat (heySourabh@gmail.com)", "Software expired",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        if (today.after(expiryMessageDate)) {
            JOptionPane.showMessageDialog(null, "<html>Licence of this software has expired.<br>"
                    + "Please contact Sourabh Bhat (heySourabh@gmail.com) for latest version<br>"
                    + "This is a free software... <br>"
                    + "...and this expiry is planned only so that you have the latest updated and <br>"
                    + "bug free software, if any bugs are found after this distribution.<br>"
                    + "You may continue to use this software for another <font color=red><b>"
                    + (finalExpiryDate.getTimeInMillis() - today.getTimeInMillis()) / 1000 / 60 / 60 / 24 + "</b></font> days, before you collect the new version of software.<br>"
                    + "I would be happy to listen to suggestions / bugs to fix in next release.</html>", "Collect latest software version",
                    JOptionPane.INFORMATION_MESSAGE);
        }
        PropertyList.readProperties();

        // If work file exists open file, else create new file for saving work.. Close program in case of problem
        workFile = new File(runDirectoryPath, PropertyList.workUpdateFilePath);
        boolean createNewWorkFile = true;
        if (workFile.exists()) {
            createNewWorkFile = false;
            try {
                workFileWriter = new FileWriter(workFile, true);
            } catch (IOException ex) {
                ex.printStackTrace();
                createNewWorkFile = true;
            }
        }

        if (createNewWorkFile) {
            try {
                workFileWriter = new FileWriter(workFile);
            } catch (IOException ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        }
        // System tray icon & commands
        systemTray = SystemTray.getSystemTray();
        Dimension iconSize = systemTray.getTrayIconSize();
        Image iconImage = getIconImage(iconSize);

        PopupMenu trayPopupMenu = new PopupMenu("Work Details Commands");
        stopItem = new MenuItem("Stop...");
        stopItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (stop == false && JOptionPane.showConfirmDialog(null, "Are you sure you want to exit?") == JOptionPane.YES_OPTION) {
                    stop = true;
                    exit(0);
                }
            }
        });
        trayPopupMenu.add(stopItem);

        pauseItem = new MenuItem();
        pauseItem.setLabel("Pause entering work");
        pauseItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (pause) {
                    pause = false;
                    pauseItem.setLabel("Pause entering work");
                } else {
                    pause = true;
                    pauseItem.setLabel("Continue entering work");
                }
            }
        });
        trayPopupMenu.add(pauseItem);

        newUpdateItem = new MenuItem();
        newUpdateItem.setLabel("Add new update...");
        newUpdateItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                newUpdate();
            }
        });
        trayPopupMenu.add(newUpdateItem);

        editItem = new MenuItem();
        editItem.setLabel("Edit using notepad...");
        editItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                editUsingNotepad();
            }
        });
        trayPopupMenu.add(editItem);

        aboutItem = new MenuItem();
        aboutItem.setLabel("About / Help...");
        aboutItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                aboutDiaog();
            }
        });
        trayPopupMenu.add(aboutItem);

        trayIcon = new TrayIcon(iconImage, "Work Detail Update Program... by Sourabh", trayPopupMenu);
        try {
            systemTray.add(trayIcon);
        } catch (AWTException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, ex.getMessage() + "\n"
                    + "Will not be able to display System Taskbar Icon", "Windows error occurred", JOptionPane.ERROR_MESSAGE);
        }
        trayIcon.displayMessage("Work updater", "Work updater started...", TrayIcon.MessageType.INFO);
        trayIcon.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editUsingNotepad();
            }
        });
//        trayIcon.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                if (e.getButton() == MouseEvent.BUTTON1) {
//                    editUsingNotepad();
//                }
//            }
//        });

        // Loop for asking WorkData every hour and save at the end of work file
        previousUpdate.setUpdateText("No previous update");
        previousUpdate.setUpdateDateAndTime(null);
        oldUpdate.setUpdateText("No previous update");
        oldUpdate.setUpdateDateAndTime(null);

        if (PropertyList.periodicPopup) {
            new BringDialogToFront(); // Start thread to periodically bring front the dialog if its behind
        }
        if (PropertyList.captionUpdate) {
            new UpdateCaption(); // Start thread to update caption
        }
        while (!stop) {
            newUpdate();
            try {
                if (stop) {
                    break;
                }
                Thread.sleep(PropertyList.updateIntervalSecs * 1000);
                if (stop) {
                    break;
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            while (pause) {
                try {
                    Thread.sleep(PropertyList.pauseCheckIntervalSecs * 1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }

        exit(0);
    }

    static void exit(int exitCode) {
        try {
            workFileWriter.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        systemTray.remove(trayIcon);
        PropertyList.writeNewProperties();
        System.exit(exitCode);
    }

    static void newUpdate() {
        pause = true;

        JPanel messagePanel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 4;
        constraints.anchor = GridBagConstraints.ABOVE_BASELINE_LEADING;
        JLabel oldUpdateLabel = new JLabel() { // Written in a complicated way so that it updates as per time elapsed

            @Override
            public String getText() {
                return "<html><b>Older update : </b>"
                        + oldUpdate.getUpdateText() + " (" + getTimeDiffText(oldUpdate.getUpdateDateAndTime()) + ")" + "</html>";
            }

            @Override
            public void paint(Graphics g) {
                setText(getText());
                super.paint(g);
            }
        };

        oldUpdateLabel.setForeground(Color.gray);
        messagePanel.add(oldUpdateLabel, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 4;
        constraints.anchor = GridBagConstraints.ABOVE_BASELINE_LEADING;
        JLabel prevUpdateLabel = new JLabel() { // Written in a complicated way so that it updates as per time elapsed

            @Override
            public String getText() {
                return "<html><b>Last update : </b>"
                        + previousUpdate.getUpdateText() + " (" + getTimeDiffText(previousUpdate.getUpdateDateAndTime()) + ")" + "</html>";
            }

            @Override
            public void paint(Graphics g) {
                setText(getText());
                super.paint(g);
            }
        };
        messagePanel.add(prevUpdateLabel, constraints);

        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.ABOVE_BASELINE_TRAILING;
        JLabel currentUpdateLabel = new JLabel("<html><b>" + "Work Details in last period : " + "</b></html>");
        messagePanel.add(currentUpdateLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 2;
        constraints.gridwidth = 3;
        constraints.anchor = GridBagConstraints.ABOVE_BASELINE_LEADING;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        JTextField workDetailsText = new JTextField(50);
        messagePanel.add(workDetailsText, constraints);

        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 1;
        JLabel updateIntervalLabel = new JLabel("Next update reminder after: ");
        messagePanel.add(updateIntervalLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 3;
        constraints.gridwidth = 1;
        JTextField updateIntervalText = new JTextField(3);
        updateIntervalText.setText("" + PropertyList.updateIntervalSecs / 60);
        messagePanel.add(updateIntervalText, constraints);

        constraints.gridx = 2;
        constraints.gridy = 3;
        constraints.gridwidth = 1;
        JLabel updateIntervalTimeUnitsLabel = new JLabel(" minutes");
        messagePanel.add(updateIntervalTimeUnitsLabel, constraints);

        Toolkit.getDefaultToolkit().beep();
        JOptionPane optionPane = new JOptionPane(messagePanel, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null);
        JDialog optionDialog = optionPane.createDialog("Enter work details");
        currentDialogInstance = optionDialog;
        optionDialog.setIconImage(getIconImage(new Dimension(32, 32)));
        optionDialog.setVisible(true);
        currentDialogInstance = null; // Used by thread for updating labels & poping up the Dialog
        if (optionPane.getValue() == null || !optionPane.getValue().equals(JOptionPane.OK_OPTION)) {
            pause = false;
            optionDialog.dispose();
            return;
        }
        optionDialog.dispose();

        // Change update interval time based on the text box filled by user
        try {
            PropertyList.updateIntervalSecs = Integer.parseInt(updateIntervalText.getText()) * 60;
        } catch (Exception ex) { // Just checking if there is a valid time interval (integer)
            // If not valid integer do nothing. Will keep the old time
            ex.printStackTrace();
        }

        Calendar now = Calendar.getInstance();
        String fileLine = now.getTime().toString() + " --> " + workDetailsText.getText() + "\r\n";
        oldUpdate = previousUpdate;
        previousUpdate = new Update();
        previousUpdate.setUpdateText(workDetailsText.getText());
        previousUpdate.setUpdateDateAndTime(now);

        try {
            workFileWriter.write(fileLine);
            workFileWriter.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error adding work update", "Error", JOptionPane.ERROR_MESSAGE);
        }
        pause = false;
    }

    static String getTimeDiffText(Calendar oldCalendar) {
        if (oldCalendar == null) {
            return "no update";
        }

        Calendar now = Calendar.getInstance();
        long diffMillis = now.getTimeInMillis() - oldCalendar.getTimeInMillis();
        if (diffMillis > 12 * 60 * 60 * 1000) { // More than 12 hrs
            return "Long time ago";
        } else if (diffMillis > 60 * 60 * 1000) { // More than an hour
            return "" + diffMillis / 1000 / 60 / 60 + " hour ago";
        } else if (diffMillis > 60 * 1000) { // More than a minute
            return "" + diffMillis / 1000 / 60 + " minutes ago";
        } else if (diffMillis > 0) { // Less than a minute
            return "Little while ago";
        } else { // Sore error
            return "";
        }
    }

    static void editUsingNotepad() {
        pause = true;
        try {
            workFileWriter.close();
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec("notepad " + workFile.getAbsolutePath());
            System.out.println("Waiting for notepad to close");
            process.waitFor();
            System.out.println("Opening file again for adding updates...");
            workFileWriter = new FileWriter(workFile, true);
            System.out.println("Opened file again");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        pause = false;
    }

    static void aboutDiaog() {
        pause = true;
        String message = "<html>This program periodically asks for work updates, and adds them into a text file.<br>"
                + "- Runs in the notification area or system tray, (generally to the bottom right of the screen).<br>"
                + "- A green icon is displayed in system tray, which can be clicked to open updates in notepad, to edit manually.<br>"
                + "- Close the notepad file after manual editing is done.<br>"
                + "- You may right click the green icon to see more options.<br><br><br>"
                + "For any further queries contact:<br>"
                + "<b>Sourabh Bhat<br>"
                + "<font color=blue>heySourabh@gmail.com</font></b></html>";
        JLabel messageLabel = new JLabel(message);
        JOptionPane.showMessageDialog(null, messageLabel, "About", JOptionPane.INFORMATION_MESSAGE);
        pause = false;
    }
    static double percentIconSize = 1.0;
    static double iconShrinkAmount = .1;

    static Image getIconImage(Dimension iconSize) {
        percentIconSize -= iconShrinkAmount;
        if (percentIconSize < 0.0 || percentIconSize > 0.9) {
            iconShrinkAmount *= -1;
        }

        BufferedImage iconImage = new BufferedImage(iconSize.width, iconSize.height, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D iconGraphics = (Graphics2D) iconImage.getGraphics();
        iconGraphics.setColor(Color.green);
        iconGraphics.fillOval((int) (iconSize.width / 2 - iconSize.width * percentIconSize / 2.0),
                (int) (iconSize.height / 2 - iconSize.height * percentIconSize / 2.0),
                (int) (iconSize.width * percentIconSize),
                (int) (iconSize.height * percentIconSize));

        return iconImage;
    }
}

class Update {

    private String updateText;
    private Calendar updateDateAndTime;

    public Calendar getUpdateDateAndTime() {
        return updateDateAndTime;
    }

    public void setUpdateDateAndTime(Calendar updateDateAndTime) {
        this.updateDateAndTime = updateDateAndTime;
    }

    public String getUpdateText() {
        return updateText;
    }

    public void setUpdateText(String updateText) {
        this.updateText = updateText;
    }
}

class BringDialogToFront implements Runnable {

    Thread t;

    public BringDialogToFront() {
        t = new Thread(this);
        t.start();
    }

    public void run() {
        while (true) {
            if (PeriodicWorkUpdate.currentDialogInstance != null) {
                PeriodicWorkUpdate.currentDialogInstance.toFront();
                // PeriodicWorkUpdate.currentDialogInstance.setAlwaysOnTop(true);
                // PeriodicWorkUpdate.currentDialogInstance.setAlwaysOnTop(false);
                PeriodicWorkUpdate.currentDialogInstance.repaint();
            }
            try {
                Thread.sleep(1 * 60 * 1000); // Sleep for 1 mins
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
}

class UpdateCaption implements Runnable {

    Thread t;

    public UpdateCaption() {
        t = new Thread(this);
        t.start();
    }

    public void run() {
        while (true) {
            if (PeriodicWorkUpdate.currentDialogInstance != null) {
                PeriodicWorkUpdate.currentDialogInstance.setIconImage(
                        PeriodicWorkUpdate.getIconImage(new Dimension(32, 32)));
            }
            try {
                Thread.sleep(200); // Sleep for 1 mins
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
}

class PropertyList {

    static File propertyFile = new File(PeriodicWorkUpdate.runDirectoryPath, "Properties.txt");
    // Default Properties
    static String default_workUpdateFilePath = "WorkFile.txt";
    static String default_backupFilePath = "No Backup";
    static int default_updateIntervalSecs = 60 * 60;
    static int default_pauseCheckIntervalSecs = 10;
    static boolean default_periodicPopup = true;
    static boolean default_captionUpdate = true;
    // Current Properties
    static String workUpdateFilePathKey = "WorkUpdateFilePath";
    static String workUpdateFilePath = "WorkFile.txt";
    static String backupFilePathKey = "BackupPath";
    static String backupFilePath = "No Backup";
    static String updateIntervalSecsKey = "UpdateInterval_secs";
    static int updateIntervalSecs = 60 * 60;
    static String pauseCheckIntervalSecsKey = "PauseCheck_secs";
    static int pauseCheckIntervalSecs = 10;
    static String periodicPopupKey = "PeriodicPopup";
    static boolean periodicPopup = true;
    static String captionUpdateKey = "CaptionUpdate";
    static boolean captionUpdate = true;

    public static void writeNewProperties() {
        Properties properties = new Properties();
        // These properties are only written but not read
        properties.setProperty("ExpiryYear", "" + PeriodicWorkUpdate.expiryYear);
        properties.setProperty("ExpiryMonth", "" + PeriodicWorkUpdate.expiryMonth);
        properties.setProperty("ExpiryDay", "" + PeriodicWorkUpdate.expiryDay);

        // These properties are written and read
        properties.setProperty(workUpdateFilePathKey, workUpdateFilePath);
        properties.setProperty(backupFilePathKey, backupFilePath);
        properties.setProperty(updateIntervalSecsKey, "" + updateIntervalSecs);
        properties.setProperty(pauseCheckIntervalSecsKey, "" + pauseCheckIntervalSecs);
        properties.setProperty(periodicPopupKey, periodicPopup ? "TRUE" : "FALSE");
        properties.setProperty(captionUpdateKey, captionUpdate ? "TRUE" : "FALSE");

        try {
            FileOutputStream outputStream = new FileOutputStream(propertyFile);
            properties.store(outputStream, "Properties for Work Updater");
            outputStream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Unable to write changed properties, if any... Please report this bug");
        }
    }

    public static void writeDefaultProperties() {
        Properties properties = new Properties();
        properties.setProperty(workUpdateFilePathKey, default_workUpdateFilePath);
        properties.setProperty(backupFilePathKey, default_backupFilePath);
        properties.setProperty(updateIntervalSecsKey, "" + default_updateIntervalSecs);
        properties.setProperty(pauseCheckIntervalSecsKey, "" + default_pauseCheckIntervalSecs);
        properties.setProperty(periodicPopupKey, default_periodicPopup ? "TRUE" : "FALSE");
        properties.setProperty(captionUpdateKey, default_captionUpdate ? "TRUE" : "FALSE");

        try {
            FileOutputStream outputStream = new FileOutputStream(propertyFile);
            properties.store(outputStream, "Properties for Work Updater");
            outputStream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Unable to write changed properties, if any... Please report this bug");
        }
    }

    public static void readProperties() {
        Properties properties = new Properties();
        try {
            FileInputStream inputStream = new FileInputStream(propertyFile);
            properties.load(inputStream);
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Unable to read property file... may be this is first run...\n"
                    + "Creating default property file");
            writeDefaultProperties();
        }

        try {
            workUpdateFilePath = properties.getProperty(workUpdateFilePathKey, default_workUpdateFilePath);
            backupFilePath = properties.getProperty(backupFilePathKey, default_backupFilePath);
            updateIntervalSecs = Integer.parseInt(properties.getProperty(updateIntervalSecsKey, "" + default_updateIntervalSecs));
            pauseCheckIntervalSecs = Integer.parseInt(properties.getProperty(pauseCheckIntervalSecsKey, "" + default_pauseCheckIntervalSecs));
            periodicPopup = properties.getProperty(periodicPopupKey, default_periodicPopup ? "TRUE" : "FALSE").equals("FALSE") ? false : true;
            captionUpdate = properties.getProperty(captionUpdateKey, default_captionUpdate ? "TRUE" : "FALSE").equals("FALSE") ? false : true;
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Some properties not formatted well... \n"
                    + "Will be replacing some properties by defaults");
        }

        writeNewProperties();
    }
}
