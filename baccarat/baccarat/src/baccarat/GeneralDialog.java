package baccarat;

import java.awt.Button;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class GeneralDialog extends JDialog{
	
	public GeneralDialog(JFrame context, String dialogName, String message, String buttonMessage)
	{
		super(context, dialogName, true);
			
		JLabel messageLabel = new JLabel(message);
		
        Button b = new Button(buttonMessage);  
        b.addActionListener( new ActionListener()  
        {  
            public void actionPerformed( ActionEvent e )  
            {  
                dispose();
            }  
        });
		
		add(messageLabel);
        add(b);
		pack();
		setSize(8 * message.length(), 100);
        setLayout(new FlowLayout());
        setResizable(false);
        setLocationRelativeTo(context);
        show(true);
	}
}
