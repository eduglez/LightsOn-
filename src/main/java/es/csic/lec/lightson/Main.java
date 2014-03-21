package es.csic.lec.lightson;  

import java.awt.*;
import java.io.*;
import java.util.Properties;
import javax.swing.*;
import org.apache.commons.io.IOUtils;
import es.csic.lec.lightson.arduino.SwitchAdapter;
import es.csic.lec.lightson.view.TrayView;

public class Main {
	
    protected static Image createImage(String path, String description) {
    	byte[] imageBytes;
    	
		try {
			imageBytes = IOUtils.toByteArray(Main.class.getResourceAsStream(path));
		} catch (IOException e) {
			imageBytes=null;
		}
    	
        if (imageBytes == null) {
            System.err.println("Resource not found: " + path);
            return null;
        } else {
            return (new ImageIcon(imageBytes, description)).getImage();
        }
        

    }
    
    private static String PROPERTIES_FILE="config.properties";
    
    private static String COMPORT_PROPERTIE="comport";
    
	public static void main(String[] args) {
		if (!SystemTray.isSupported()) {
            System.err.println("SystemTray is not supported");
            return;
        }
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        
        

        
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	TrayView vista = new TrayView(createImage("bulb_off.gif", "tray icon"), createImage("bulb_on.gif", "tray icon"), createImage("bulb_no.gif", "tray icon"));
            	Controller controller = new Controller(vista, SwitchAdapter.getInstance());
            	vista.setCommNameProvider(SwitchAdapter.getInstance());
            	vista.setViewListener(controller);
            	vista.createAndShowGUI();
            	
            	String comPort=null;
                try {
                	Properties prop = new Properties();
                	prop.load(new FileInputStream(PROPERTIES_FILE));
                	comPort=prop.getProperty(COMPORT_PROPERTIE);
                	if(comPort!=null){
                		controller.onChangeCommTo(comPort);
                	}
                } catch (IOException ex) {
                	System.out.println("No config file");
                }
            	
            }
        });
        
        /*
        ScheduledExecutorService scheduler =
        	       Executors.newScheduledThreadPool(1);
        
        final Runnable beeper = new Runnable() {
            public void run() {
            	
            	try {
					URL oracle = new URL("http://161.111.168.7/~edu/updates/prueba.txt");
					BufferedReader in = new BufferedReader(
					    new InputStreamReader(oracle.openStream()));
					
					    String inputLine;
					    inputLine = in.readLine();
					    if(inputLine!=null){
					    	if(inputLine.contains("YES")){
					    		SwitchAdapter.getInstance().sendCommand(COMMAND.SWITCH_ON);
					    	}
					    	System.out.println(inputLine);
					    }
					    
					    in.close();
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
                    
            }
        };
        
        scheduler.scheduleAtFixedRate(beeper, 10, 60, TimeUnit.SECONDS);
        */
    }
    
}
