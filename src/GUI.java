import org.rspeer.runetek.api.component.Interfaces;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class GUI extends JFrame {
    private braceletAlcher ctx;
    public GUI(braceletAlcher main)
    {
        this.ctx = main;
        initComponents();
    }



    JButton button1 = new JButton();
    JTextField inputText = new JTextField();
    Container contentPane = getContentPane();
    private void buttionActionPerformed(ActionEvent e)
    {
        this.setVisible(false);
    }
    private void initComponents()
    {
        contentPane.setLayout(null);
        button1.setText("Start");
        button1.setBounds(15,55,200,50);
        button1.addActionListener(e-> buttionActionPerformed(e));
        inputText.setBounds(15,10,200,30);
        contentPane.add(inputText);
        contentPane.add(button1);
        pack();
        setLocationRelativeTo(null);

    }
    public int getBraceletPrice()
    {
        return Integer.parseInt(inputText.getText());
    }

}


