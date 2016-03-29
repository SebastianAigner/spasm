import twitch.rechat.RechatMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.List;
/**
 * Created by sebi on 29.03.16.
 */
public class AnalyticsGUI {
    private JTextField wordCountField;
    private JButton numberOfOcurrencesButton;
    private JButton openFileForAnalysisButton;
    private JPanel spreadDiagramPanel;
    private JPanel analyticsPanel;
    private JLabel fileOpenStatus;
    private JList messagePreviewList;
    private JButton openInStreamButton;
    private JList mostOccuringWordsList;
    private Desktop desktop;
    private JFrame frame;

    private Analytics analytics = new Analytics();

    public AnalyticsGUI(JFrame frame) {
        this.frame = frame;
        if(Desktop.isDesktopSupported()) {
            desktop = Desktop.getDesktop();
        }
        fileOpenStatus.setText("Ready");
        numberOfOcurrencesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultListModel listModel = new DefaultListModel();
                List<RechatMessage> results = analytics.findMesasgeTextContains(wordCountField.getText(), false);
                for(RechatMessage rechatMessage: results) {
                    listModel.addElement(rechatMessage);
                }
                messagePreviewList.setModel(listModel);
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
            }
        });
        openInStreamButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RechatMessage rechatMessage = (RechatMessage)messagePreviewList.getSelectedValue();
                try {
                    desktop.browse(new URI(analytics.getLinkForChatMessage(rechatMessage)));
                }
                catch(Exception ex) {
                    ex.printStackTrace();
                }
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
                for(RechatMessage message: rechatMessages) {
                    listModel.addElement(message);
                }
                messagePreviewList.setModel(listModel);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Twitch Chat Analytics");
        frame.setContentPane(new AnalyticsGUI(frame).analyticsPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
