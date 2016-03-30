import twitch.rechat.RechatMessage;

import javax.swing.*;
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
    private JButton numberOfOcurrencesButton;
    private JButton openFileForAnalysisButton;
    private JPanel analyticsPanel;
    private JLabel fileOpenStatus;
    private JList messagePreviewList;
    private JButton openInStreamButton;
    private JList mostOccuringWordsList;
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
        bargraphDiagram.setValues(Arrays.asList(1,2,3,4,5));
        this.frame = frame;
        if(Desktop.isDesktopSupported()) {
            desktop = Desktop.getDesktop();
        }
        fileOpenStatus.setText("Ready");
        numberOfOcurrencesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentMessageSelection = new ArrayList<RechatMessage>();
                DefaultListModel listModel = new DefaultListModel();
                List<RechatMessage> results = analytics.findMesasgeTextContains(wordCountField.getText(), false);
                for(RechatMessage rechatMessage: results) {
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

                frame.setTitle("Analyzing " + analytics.getMatch().title);
                updateMostUsedWords();
                updateMessagePreviewList();
                updateBargraph();
                messagePreviewLabel.setText("Message Preview (" + analytics.getMessageList().size() + " total)");
            }
        });
        openInStreamButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RechatMessage rechatMessage = (RechatMessage)messagePreviewList.getSelectedValue();
                    openInBrowser(analytics.getLinkForChatMessage(rechatMessage));

            }
        });
        mostOccuringWordsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if(e.getClickCount() == 2) {
                    wordCountField.setText(mostOccuringWordsList.getSelectedValue().toString());
                    numberOfOcurrencesButton.doClick();

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
                if(e.getClickCount() == 2) {
                    openInBrowser(analytics.getLinkForPercentage(e.getX() / (double) bargraphDiagram.getWidth()));
                }
            }
        });
        createReportFromLinkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    ReportGenerator.createReport();
                }
                catch(Exception ex) {

                }
            }
        });
    }

    public void updateMostUsedWords() {
        DefaultListModel mostUsedWordsListModel = new DefaultListModel();
        for(String word: analytics.getMostMessagedWords()) {
            mostUsedWordsListModel.addElement(word);
        }
        mostOccuringWordsList.setModel(mostUsedWordsListModel);
    }

    public void updateMessagePreviewList() {
         DefaultListModel listModel = new DefaultListModel();
                List<RechatMessage> rechatMessages = analytics.getMessageList();
                currentMessageSelection = new ArrayList<>();
                for(RechatMessage message: rechatMessages) {
                    currentMessageSelection.add(message);
                    listModel.addElement(message);
                }
                messagePreviewList.setModel(listModel);
    }

    public void updateBargraph() {
        bargraphDiagram.setValues(analytics.getChatmessageDistribution(currentMessageSelection,bargraphGranularitySlider.getValue()));
        bargraphMaximumLabel.setText("Maximum: " + Collections.max(bargraphDiagram.getValues()));
        bargraphDiagram.repaint();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Twitch Chat Analytics");
        frame.setContentPane(new AnalyticsGUI(frame).analyticsPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public void openInBrowser(String link) {
                try {
                    desktop.browse(new URI(link));
                }
                catch(Exception ex) {
                    ex.printStackTrace();
                }
    }
}
