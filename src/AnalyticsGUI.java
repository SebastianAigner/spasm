import twitch.rechat.RechatMessage;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by sebi on 29.03.16.
 */
public class AnalyticsGUI {
    private JTextField wordCountField;
    private JButton numberOfOccurrencesButton;
    private JButton openFileForAnalysisButton;
    private JPanel analyticsPanel;
    private JLabel fileOpenStatus;
    private JList<RechatMessage> messagePreviewList;
    private JButton openInStreamButton;
    private JList<String> mostOccuringWordsList;
    private Desktop desktop;
    private JFrame frame;
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
        numberOfOccurrencesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentMessageSelection = new ArrayList<>();
                DefaultListModel<RechatMessage> listModel = new DefaultListModel<>();
                List<RechatMessage> results = analytics.findMesasgeTextContains(wordCountField.getText(), false, false);
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
            @Override
            public void actionPerformed(ActionEvent e) {
                analytics.openGameReport();
                fileOpenStatus.setText(analytics.getFileOpenStatus());

                frame.setTitle("Analyzing " + analytics.getMatch().getTitle());
                updateMostUsedWords();
                updateMessagePreviewList();
                updateBargraph();
                messagePreviewLabel.setText("Message Preview (" + analytics.getMessageList().size() + " total)");
            }
        });

        openInStreamButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RechatMessage rechatMessage = (RechatMessage) messagePreviewList.getSelectedValue();
                openInBrowser(analytics.getLinkForChatMessage(rechatMessage));

            }
        });

        mostOccuringWordsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getClickCount() == 2) {
                    wordCountField.setText(mostOccuringWordsList.getSelectedValue().toString());
                    numberOfOccurrencesButton.doClick();

                }
            }
        });

        bargraphGranularitySlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateBargraph();
            }
        });

        bargraphDiagram.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                long timestamp = analytics.getTimestampForPercentage(e.getX() / (double) bargraphDiagram.getWidth());
                currentScrubPositionLabel.setText("Current Scrub Position: " + TimestampHelper.timestampToString(timestamp));
                if (e.getClickCount() == 2) {
                    openInBrowser(analytics.getLinkForPercentage(e.getX() / (double) bargraphDiagram.getWidth()));
                }
            }
        });

        createReportFromLinkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    ReportGenerator.createReport();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "The report could not be created. " + ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    public void updateMostUsedWords() {
        DefaultListModel<String> mostUsedWordsListModel = new DefaultListModel<>();
        for (String word : analytics.getMostMessagedWords()) {
            mostUsedWordsListModel.addElement(word);
        }
        mostOccuringWordsList.setModel(mostUsedWordsListModel);
    }

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

    public void updateBargraph() {
        bargraphDiagram.setValues(analytics.getChatmessageDistribution(currentMessageSelection, bargraphGranularitySlider.getValue()));
        bargraphMaximumLabel.setText("Maximum: " + Collections.max(bargraphDiagram.getValues()));
        bargraphDiagram.repaint();
    }

    public static void main(String[] args) {
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
        frame.pack();
        frame.setVisible(true);
    }

    public void openInBrowser(String link) {
        try {
            desktop.browse(new URI(link));
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "The browser could not be started. " + ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
