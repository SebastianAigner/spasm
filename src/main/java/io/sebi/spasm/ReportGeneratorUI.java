package io.sebi.spasm;

import javax.swing.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;

public class ReportGeneratorUI extends JDialog implements PropertyChangeListener {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JProgressBar reportCreationProgressBar;
    private JTextField urlEntryField;
    private JLabel twitchMessageLabel;
    private ReportGenerator reportGenerator;
    private boolean reportGenerationDone = false;

    public ReportGeneratorUI() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        twitchMessageLabel.setText("");
        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        reportGenerator = new ReportGenerator(urlEntryField.getText());
        reportGenerator.addPropertyChangeListener(this);
        reportGenerator.execute();
    }

    private void onCancel() {
        if (reportGenerator != null) {
            reportGenerator.cancel(false);
        }
        dispose();
    }

    public static void main(String[] args) {
        ReportGeneratorUI dialog = new ReportGeneratorUI();
        dialog.pack();
        dialog.setTitle("Twitch Report Generator");
        dialog.setLocationRelativeTo(null);
        dialog.setResizable(false);
        dialog.setVisible(true);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {
            int progress = (int) evt.getNewValue();
            reportCreationProgressBar.setValue(progress);
            reportCreationProgressBar.setStringPainted(true);
            reportCreationProgressBar.setString(progress + "%");
            String lastMessage = reportGenerator.getLastMessage();
            twitchMessageLabel.setText(lastMessage.length() > 30 ? lastMessage.substring(0, 30) : lastMessage);
            this.pack();
        }
        if (reportGenerator.getState() == SwingWorker.StateValue.DONE && !reportGenerationDone) {
            reportGenerationDone = true; // For some reason the "done" event gets fired twice. This fixes that issue.
            System.out.println("It's done!");
            JFileChooser chooser = new JFileChooser();
            int choice = chooser.showDialog(null, "Save Game Report");
            if (choice == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                if (!file.exists()) {
                    try {
                        boolean creation = file.createNewFile();
                        if (creation) {
                            FileOutputStream fos = new FileOutputStream(file);
                            fos.write(reportGenerator.getResult().getBytes());
                        }
                    } catch (Exception ex) {
                        System.out.println("Not saving. Dumping to console for backup.");
                        System.out.println(reportGenerator.getResult());
                    }
                }
            }
        }
    }
}
