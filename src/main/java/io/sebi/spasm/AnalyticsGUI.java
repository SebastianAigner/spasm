package io.sebi.spasm;

import io.sebi.spasm.rechat.RechatMessage;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Sebastian Aigner
 */

/**
 * The Analytics GUI is the main interface of the project. It houses message previews and other important information.
 */
public class AnalyticsGUI {
    private JTextField wordCountField;
    private JButton findMessagesButton;
    private JButton openFileForAnalysisButton;
    private JPanel analyticsPanel;
    private JLabel fileOpenStatus;
    private JList<RechatMessage> messagePreviewList;
    private JButton openInStreamButton;
    private JList<String> mostOccuringWordsList;
    private Desktop desktop;
    private final JFrame frame;
    private BargraphDiagram bargraphDiagram;
    private JSlider bargraphGranularitySlider;
    private JLabel bargraphMaximumLabel;
    private JLabel currentScrubPositionLabel;
    private JLabel messagePreviewLabel;
    private JButton createReportFromLinkButton;

    private List<RechatMessage> currentMessageSelection;
    private Analytics analytics = new Analytics();

    public AnalyticsGUI(JFrame frame) {
        this.frame = frame;
        if (Desktop.isDesktopSupported()) {
            desktop = Desktop.getDesktop();
        }

        fileOpenStatus.setText("Ready");
        findMessagesButton.addActionListener(new ActionListener() {
            /**
             * When the button to find messages is clicked, the analytics part of the software will find the search
             * query from the word count field. The graphs and message preview will be updated accordingly.
             * @param e
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                currentMessageSelection = new ArrayList<>();
                DefaultListModel<RechatMessage> listModel = new DefaultListModel<>();
                List<RechatMessage> results = analytics.findMessageTextContains(wordCountField.getText(), false, false, true);
                if (results == null) {
                    JOptionPane.showMessageDialog(frame, "Please open a file before searching!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                for (RechatMessage rechatMessage : results) {
                    listModel.addElement(rechatMessage);
                    currentMessageSelection.add(rechatMessage);
                }
                messagePreviewLabel.setText("Message Preview (" + currentMessageSelection.size() + " total)");
                messagePreviewList.setModel(listModel);
                updateBargraph();
            }
        });

        openFileForAnalysisButton.addActionListener(new ActionListener() {
            /**
             * When the users clicks the "Open File for Analysis" button, the analytics will be reinitialised with the
             * data set opened by the user, the UI will be updated accordingly.
             * @param e
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                int choice = chooser.showDialog(null, "Open Game Report");
                if (choice == JFileChooser.CANCEL_OPTION) {
                    return;
                }
                String file = "";
                FileInputStream fis;
                try {
                    fis = new FileInputStream(chooser.getSelectedFile());
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                    return;
                }
                Scanner s = new Scanner(fis);
                while (s.hasNextLine()) {
                    file += s.nextLine();
                }
                analytics.openGameReport(file);

                switch (analytics.getFileOpenStatus()) {
                    case SUCCESS:
                        fileOpenStatus.setText("Successfully opened file.");
                        break;
                    case CANCEL:
                        fileOpenStatus.setText("No file opened.");
                        return;
                    case EMPTY:
                        fileOpenStatus.setText("File is empty.");
                        break;
                    default:
                        return;
                }

                frame.setTitle("Analyzing " + analytics.getBroadcast().getTitle());
                updateMostUsedWords();
                updateMessagePreviewList();
                updateBargraph();
                messagePreviewLabel.setText("Message Preview (" + analytics.getMessageList().size() + " total)");
            }
        });

        openInStreamButton.addActionListener(new ActionListener() {
            @Override
            /**
             * When the user clicks the "Open in Stream" button, a new browser window will open with the past broadcast
             * opened at the given timestamp.
             */
            public void actionPerformed(ActionEvent e) {
                RechatMessage rechatMessage = messagePreviewList.getSelectedValue();
                String link = analytics.getLinkForChatMessage(rechatMessage);
                if (link == null) {
                    JOptionPane.showMessageDialog(frame, "Please select a link! ", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                openInBrowser(link);

            }
        });

        mostOccuringWordsList.addMouseListener(new MouseAdapter() {
            @Override
            /**
             * When the user double clicks on a most occuring message, treat it the same as a selection of a message and
             * then a click on "find messages"
             */
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getClickCount() == 2 && mostOccuringWordsList.getSelectedValue() != null) {
                        wordCountField.setText(mostOccuringWordsList.getSelectedValue());
                        findMessagesButton.doClick();
                }
            }
        });

        bargraphGranularitySlider.addChangeListener(new ChangeListener() {
            @Override
            /**
             * When the user changes the granularity of the bar graph, update the bargraph
             */
            public void stateChanged(ChangeEvent e) {
                updateBargraph();
            }
        });

        bargraphDiagram.addMouseListener(new MouseAdapter() {
            @Override
            /**
             * When the user clicks inside the barchart, preview the current scrub position. If he double-clicks,
             * open the broadcast at the position indicated by the mouse cursor in his browser.
             */
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                long timestamp = analytics.getTimestampForPercentage(e.getX() / (double) bargraphDiagram.getWidth());
                currentScrubPositionLabel.setText("Current Scrub Position: " + TimestampHelper.secondPrecisionTimestampToString(timestamp));
                if (e.getClickCount() == 2) {
                    openInBrowser(analytics.getLinkForPercentage(e.getX() / (double) bargraphDiagram.getWidth()));
                }
            }
        });

        createReportFromLinkButton.addActionListener(new ActionListener() {
            @Override
            /**
             * Open the report creation tool.
             */
            public void actionPerformed(ActionEvent e) {
                ReportGeneratorUI reportGeneratorUI = new ReportGeneratorUI();
                reportGeneratorUI.main(null);
            }
        });
    }

    /**
     * Updates the visual display of the most used words.
     */
    public void updateMostUsedWords() {
        DefaultListModel<String> mostUsedWordsListModel = new DefaultListModel<>();
        for (String word : analytics.getMostMessagedWords()) {
            mostUsedWordsListModel.addElement(word);
        }
        mostOccuringWordsList.setModel(mostUsedWordsListModel);
    }

    /**
     * Updates the visual display of the messages currently selected.
     */
    public void updateMessagePreviewList() {
        DefaultListModel<RechatMessage> listModel = new DefaultListModel<>();
        List<RechatMessage> rechatMessages = analytics.getMessageList();
        currentMessageSelection = new ArrayList<>();
        for (RechatMessage message : rechatMessages) {
            currentMessageSelection.add(message);
            listModel.addElement(message);
        }
        messagePreviewList.setModel(listModel);
    }

    /**
     * Redraws the bargraph and updates the "maximum" indicator
     */
    public void updateBargraph() {
        List<Integer> chatmessageDistribution = analytics.getChatmessageDistribution(currentMessageSelection, bargraphGranularitySlider.getValue());
        if (chatmessageDistribution != null) {
            bargraphDiagram.setValues(chatmessageDistribution);
        }
        if (bargraphDiagram.getValues() != null) {
            bargraphMaximumLabel.setText("Maximum: " + Collections.max(bargraphDiagram.getValues()));
        }
        bargraphDiagram.repaint();
    }

    public static void main(String[] args) {
        // Try to use the "Nimbus" Look and Feel for Java. This is purely aesthetic.
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // If the Nimbus styling is not available, we fall back to normal styling
        }
        JFrame frame = new JFrame("Twitch Chat Analytics");
        frame.setContentPane(new AnalyticsGUI(frame).analyticsPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Opens a link in the user's default browser.
     *
     * @param link Link to be opened
     */
    public void openInBrowser(String link) {
        try {
            desktop.browse(new URI(link));
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "The browser could not be started. " + ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
