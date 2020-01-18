import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class App {
    private static JFrame jframe;


    private JButton startButton;
    private JButton stopButton;
    private JTextField ipField;
    private JLabel ipLabel;
    private JLabel delayLabel;
    private JTextField delayField;
    private JPanel Panel;
    private JLabel ipErrorLabel;
    private JLabel delayErrorLabel;
    private JLabel smoothLabel;
    private JCheckBox smoothCheckbox;
    private String[] colorGrabMethods = {"Average color", "Most saturated color", "Most saturated sector"};
    private JComboBox<String> colorGrabMethodComboBox;
    private JLabel colorGrabMethodLabel;

    private CommandThread commandThread;
    public String ip = "192.168.1.";
    public int delay = 100;
    public boolean smooth = true;
    private boolean ipValid = false;
    private boolean delayValid = true;
    private boolean running = false;


    public App() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(colorGrabMethods);
        colorGrabMethodComboBox.setModel(model);
        startButton.addActionListener(e -> {
            if (ipValid && !running && delayValid) {
                running = true;
                commandThread = new CommandThread(App.this);
                Thread thread = new Thread(commandThread);
                thread.start();
            } else {
                //TODO: show a message dialog here!
                //JOptionPane.showMessageDialog(panel);
            }
        });

        ipField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                super.keyTyped(e);
                //Get the new string and match it to regex of ip address
                if (e.getKeyChar() == '\b') {
                    //Backspace case
                    ip = ipField.getText();
                } else {
                    ip = ipField.getText() + e.getKeyChar();
                }
                Pattern pattern = Pattern.compile("\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b");
                Matcher matcher = pattern.matcher(ip);
                if (matcher.matches()) {
                    ipErrorLabel.setText("");
                    ipValid = true;
                } else {
                    ipErrorLabel.setText("IP not ipValid!");
                    ipValid = false;
                }
            }
        });
        delayField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                super.keyTyped(e);
                String delayString;
                if (e.getKeyChar() == '\b') {
                    //Backspace case
                    delayString = delayField.getText();
                } else {
                    delayString = delayField.getText() + e.getKeyChar();
                }
                try {
                    int delayInt = Integer.parseInt(delayString);
                    delayErrorLabel.setText("");
                    delayValid = true;
                    delay = delayInt;
                } catch (NumberFormatException nfe) {
                    delayErrorLabel.setText("Delay not valid!");
                    delayValid = false;
                }
            }
        });
        stopButton.addActionListener(e -> {
            if (running) {
                running = false;
                try {
                    commandThread.stop();
                } catch (NullPointerException ex) {
                    System.out.println("Server tried to stop. but something went wrong!");
                    ex.printStackTrace();
                }
            }

        });
        smoothCheckbox.addActionListener(e -> smooth = !smooth);
    }

    public static void main(String[] args) {
        jframe = new JFrame("App");
        jframe.setContentPane(new App().Panel);
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.pack();
        jframe.setVisible(true);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    public void error() {
        this.running = false;
        System.out.println("Error occurred! Aborting server launch!");
    }


}
