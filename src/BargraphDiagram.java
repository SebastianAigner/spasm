import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by sebi on 29.03.16.
 */
public class BargraphDiagram extends JPanel {
    private List<Integer> values;
    //TODO: Support different colors
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        int panelWidth = this.getWidth();
        int panelHeight = this.getHeight();
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.BLUE);
        int maximum = 50;
        if (values == null) {
            values = Arrays.asList(1, 2, 3, 4);
        }
        try {
            maximum = Collections.max(values);
        } catch (Exception e) {
        }
        int resolution = values.size();
        for (int i = 0; i < values.size(); ++i) {
            int startX = (int) (panelWidth / (double) resolution * i);
            int width = (int) (panelWidth / (double) resolution);
            int height = (int) (values.get(i) / (double) maximum * panelHeight);
            int startY = panelHeight - height;
            g2d.fillRect(startX, startY, width, height);
        }
    }

    public List<Integer> getValues() {
        return values;
    }

    public void setValues(List<Integer> values) {
        this.values = values;
    }


}
