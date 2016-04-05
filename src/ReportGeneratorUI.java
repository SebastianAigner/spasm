import com.oracle.webservices.internal.api.message.PropertySet;

import javax.swing.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class ReportGeneratorUI extends JDialog  implements PropertyChangeListener, Thread.UncaughtExceptionHandler{
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JProgressBar progressBar1;
    private JTextField textField1;
    private ReportGenerator reportGenerator;

    public ReportGeneratorUI() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

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
        reportGenerator = new ReportGenerator();
        reportGenerator.setVideoURL(textField1.getText());
        reportGenerator.addPropertyChangeListener(this);
        reportGenerator.execute();
// add your code here
        //dispose();
    }

    private void onCancel() {
// add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        ReportGeneratorUI dialog = new ReportGeneratorUI();
        dialog.pack();
        dialog.setTitle("Twitch Report Generator");
        //dialog.setSize(200,200);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        //ReportGenerator r = new ReportGenerator();
        //System.exit(0);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if("progress" == evt.getPropertyName()) {
            int progress = (int)evt.getNewValue();
            System.out.println(progress);
            progressBar1.setValue(progress);
            progressBar1.setStringPainted(true);
            progressBar1.setString(progress + "%");
        }
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        t.toString();
        e.printStackTrace();
        e.toString();
    }
}
