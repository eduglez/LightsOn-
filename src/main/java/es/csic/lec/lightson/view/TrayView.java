package es.csic.lec.lightson.view;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import es.csic.lec.lightson.arduino.ICommNameProvider;

public class TrayView implements IView{
	
	private Image bulbOff;
	private Image bulbOn;
	private Image bulbNo;
	private PopupMenu popup;
	private TrayIcon trayIcon;
	private SystemTray tray;
	private MenuItem miResetConnection;
	private Menu mConnectionMenu;
	private MenuItem miTurnOn;
	private MenuItem miTurnOff;
	
	public TrayView(Image bulbOff, Image bulbOn, Image bulbNo, ICommNameProvider commNameProvider, IViewEventsListener switchEventsListener){
		this.bulbOff=bulbOff;
		this.bulbOn=bulbOn;
		this.bulbNo=bulbNo;
		this.commNameProvider=commNameProvider;
		this.switchEventsListener=switchEventsListener;
		
	}
	
	public TrayView(Image bulbOff, Image bulbOn, Image bulbNo){
		this.bulbOff=bulbOff;
		this.bulbOn=bulbOn;
		this.bulbNo=bulbNo;
		this.commNameProvider=null;
		this.switchEventsListener=null;
	}
	
	private ICommNameProvider commNameProvider;
	
	public void setCommNameProvider(ICommNameProvider commNameProvider){
		this.commNameProvider=commNameProvider;
	}
	
	private IViewEventsListener switchEventsListener;
	
	
	private void resetConnections(){
		
    	mConnectionMenu.removeAll();
    	
    	if(commNameProvider!=null){
    		for(String c: commNameProvider.getCommNames()){
    			MenuItem mi=new MenuItem(c);
    			mConnectionMenu.add(mi);
      	  		mi.addActionListener(new TrayView.SerialCommPortLabelClickActionListener(c));
    		}
    		mConnectionMenu.addSeparator();
    	}
    	
        mConnectionMenu.add(miResetConnection);
        
        
    }
	
	public void createAndShowGUI() {
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }
        
        popup = new PopupMenu();
        
        trayIcon = new TrayIcon(bulbNo);
        trayIcon.setImageAutoSize(true);
        tray = SystemTray.getSystemTray();
        
        miTurnOn = new MenuItem("ON");
        miTurnOn.setEnabled(false);
        miTurnOn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
					if(switchEventsListener!=null){
						switchEventsListener.onSwitchOn();
					}
			}
		});
        
        
        miTurnOff = new MenuItem("OFF");
        miTurnOff.setEnabled(false);
        miTurnOff.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {

					if(switchEventsListener!=null){
						switchEventsListener.onSwitchOff();
					}
	
			}
		});
        
        mConnectionMenu = new Menu("Connection");
        
        miResetConnection = new MenuItem("Reset");
        
        miResetConnection.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				switchEventsListener.onReset();
				resetConnections();
			}
		});
        
        
        resetConnections();
        
        MenuItem exitItem = new MenuItem("Exit");
        
        popup.add(miTurnOn);
        popup.add(miTurnOff);
        popup.addSeparator();
        popup.add(mConnectionMenu);
        
        

        popup.add(exitItem);
        
        trayIcon.setPopupMenu(popup);
        
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
            return;
        }

        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	tray.remove(trayIcon);
            	if(switchEventsListener!=null){
            		switchEventsListener.onExit();
            	}
            }
        });
    }

	@Override
	public void enableSwitchOn() {
		miTurnOn.setEnabled(true);
		miTurnOff.setEnabled(false);
		
	}

	@Override
	public void enableSwitchOff() {
		miTurnOn.setEnabled(false);
		miTurnOff.setEnabled(true);
		
	}

	@Override
	public void disableSwitch() {
		miTurnOn.setEnabled(false);
		miTurnOff.setEnabled(false);
		
	}

	@Override
	public void setOffState() {
		trayIcon.setImage(bulbOff);
	}

	@Override
	public void setOnState() {
		trayIcon.setImage(bulbOn);
	}
	
	@Override
	public void setDisconnectedState(){
		trayIcon.setImage(bulbNo);
	}
	
	@Override
	public void setViewListener(IViewEventsListener switchEventsListener) {
		this.switchEventsListener=switchEventsListener;
	}
	
	class SerialCommPortLabelClickActionListener implements ActionListener {
    	
    	private String commName;
    	
    	public SerialCommPortLabelClickActionListener(String commName){
    		this.commName=commName;
    	}
    	
    	@Override
    	public void actionPerformed(ActionEvent e){
    		if(switchEventsListener!=null){
    			switchEventsListener.onChangeCommTo(commName);
    		}
    	}

    }

	@Override
	public void showInfo(String string) {
		trayIcon.displayMessage(string, string, MessageType.INFO);
		
	}

	@Override
	public void showError(String message) {
		trayIcon.displayMessage(message, message, MessageType.ERROR);
		
	}
}
