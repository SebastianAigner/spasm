import twitch.rechat.RechatMessage;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

    private Analytics analytics = new Analytics();

    public AnalyticsGUI() {
        numberOfOcurrencesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("You did a thing!");
            }
        });
        openFileForAnalysisButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                analytics.openGameReport();
                fileOpenStatus.setText(analytics.getFileOpenStatus());
                DefaultListModel listModel = new DefaultListModel();
                List<String> rechatMessages = analytics.getMessageList();
                for(String message: rechatMessages) {
                    listModel.addElement(message);
                }
                messagePreviewList.setModel(listModel);
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("AnalyticsGUI");
        frame.setContentPane(new AnalyticsGUI().analyticsPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
