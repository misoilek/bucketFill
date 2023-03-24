import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

public class Frame extends JFrame implements ActionListener {

    private JLabel X, Y, picture;
    private JTextField X_text, Y_text;
    private JPanel topPanel, imgPanel;
    private JButton colorPicker, paintButton;
    private BufferedImage img;
    private Color color = Color.BLACK;


    public Frame(){
        initializeFrame();
    }

    private void initializeFrame() {
        setTitle("Sketcher");
        setSize(800, 600);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        X_text = new JTextField(3);
        Y_text = new JTextField(3);
        X = new JLabel("X position: ");
        Y = new JLabel("Y position: ");
        colorPicker = new JButton("Color picker");
        paintButton = new JButton("Paint");
        imgPanel = new JPanel();
        topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        paintButton.addActionListener(this);

        try {
            img = ImageIO.read(new File("src/images/obrazok.bmp"));
            picture = new JLabel(new ImageIcon(img));
            picture.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    X_text.setText(Integer.toString(e.getX()));
                    Y_text.setText(Integer.toString(e.getY()));
                    bucketFill(e.getX(), e.getY(), color);
                }
            });
            imgPanel.add(picture);

        } catch (IOException e){
            System.out.println("Image not found");
        }

        colorPicker.addActionListener(this);

        topPanel.add(X);
        topPanel.add(X_text);
        topPanel.add(Y);
        topPanel.add(Y_text);
        topPanel.add(paintButton);
        topPanel.add(colorPicker);
        topPanel.setBackground(Color.RED);

        add(topPanel, BorderLayout.NORTH);
        add(imgPanel, BorderLayout.CENTER);
    }

    private void bucketFill(int x, int y, Color c){
        int baseColor = img.getRGB(x, y);
        if (color != null && baseColor != c.getRGB()){
            fillColor(x, y, baseColor,c);
        } else{
            System.out.println("Pick a color first");
        }

        picture.setIcon(new ImageIcon(img));
    }

    public boolean isShaded(int currX, int currY, int[]neighbour, int baseColor){
        //R,G,B of root pixel
        int redRoot = (baseColor>>16) & 0xff;
        int greenRoot = (baseColor>>8) & 0xff;
        int blueRoot = baseColor & 0xff;

        //R,G,B of neighbour
        int red = (img.getRGB(currX + neighbour[0], currY + neighbour[1]) >> 16) & 0xff;
        int green = (img.getRGB(currX + neighbour[0], currY + neighbour[1]) >> 8) & 0xff;
        int blue = (img.getRGB(currX + neighbour[0], currY + neighbour[1])) & 0xff;

        //5% of R,G,B
        int shadedRed = (int) (red*0.05);
        int shadedGreen = (int) (green*0.05);
        int shadedBlue = (int) (blue*0.05);

        if((redRoot <= red+shadedRed  && redRoot >= (red-shadedRed))
                && (greenRoot <= (green+shadedGreen)  && greenRoot >= (green-shadedGreen))
                && (blueRoot <= (blue+shadedBlue) && blueRoot >= (blue-shadedBlue))){
            return true;
        }
        return false;
    }

    private boolean hasNeighbour(int currX, int currY, int[] neighbour, int baseColor, Color c ) {
        //checking boundaries of possible neighbour on the NORTH, WEST, SOUTH, EAST
        if (currX+neighbour[0] < img.getWidth() && currY+neighbour[1] < img.getHeight()  && currX + neighbour[0] >= 0
                && currY + neighbour[1] >= 0 && c.getRGB() != img.getRGB(neighbour[0] + currX, neighbour[1] + currY)
                && isShaded(currX, currY, neighbour, baseColor)){
            return true;
        }
        return false;
    }

    private void fillColor(int x, int y, int baseColor, Color c) {

        Queue<Point> queue = new LinkedList<>();
        queue.add(new Point(x,y));
        img.setRGB(x,y,c.getRGB());;

        int[][] possibleNeighbours = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}}; //NORTH, WEST, SOUTH, EAST
        int currX, currY;

        while(queue.size() > 0){
            //getting X and Y of last visited pixel
            currX = queue.peek().x;
            currY = queue.peek().y;

            queue.remove();

            for (int[] neighbour:possibleNeighbours){
                if (hasNeighbour(currX, currY, neighbour, baseColor, c)){
                    queue.add(new Point(currX+neighbour[0], currY+neighbour[1]));
                    img.setRGB(currX+neighbour[0], currY+neighbour[1], c.getRGB());
                }
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource()==colorPicker){
            JColorChooser jColorChooser = new JColorChooser();
            color = jColorChooser.showDialog(null, "Color picker", Color.BLACK);
        } else if(e.getSource()==paintButton){
            try {
                if((Integer.parseInt(Y_text.getText()) >= 0 &&  Integer.parseInt(Y_text.getText()) < picture.getWidth())
                        && (Integer.parseInt(X_text.getText()) >= 0 && Integer.parseInt(X_text.getText()) < picture.getHeight())){
                    bucketFill(Integer.parseInt(X_text.getText()), Integer.parseInt(Y_text.getText()), color);
                } else {
                    throw new NumberTooHigh();
                }
            } catch (NumberFormatException ex){
                System.out.println("Cannot convert letters to int");
            } catch (NumberTooHigh ex) {
                System.out.println("Number is not within of image range");
            }
        }
    }
}
