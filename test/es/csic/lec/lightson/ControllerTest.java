package es.csic.lec.lightson;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import es.csic.lec.lightson.arduino.ISwitchListener;
import es.csic.lec.lightson.arduino.ISwitch;
import es.csic.lec.lightson.arduino.ISwitch.COMMAND;
import es.csic.lec.lightson.arduino.ISwitch.RETURN;
import es.csic.lec.lightson.arduino.ICommNameProvider;
import es.csic.lec.lightson.arduino.ICommPortStatusListener;
import es.csic.lec.lightson.view.IViewEventsListener;
import es.csic.lec.lightson.view.IView;

public class ControllerTest {
	
	class ViewStub implements IView{
		
		boolean switchOnEnabled;
		
		boolean switchOffEnabled;
		
		int state;
		
		int STATE_ON = 0;
		
		int STATE_OFF = 1;
		
		int STATE_NO = 2;
		
		public ViewStub(){
			switchOnEnabled=false;
			switchOffEnabled=false;
			state=STATE_NO;
			errorString=null;
			infoString=null;
		}
		
		@Override
		public void enableSwitchOn() {
			switchOnEnabled=true;
			switchOffEnabled=false;
		}

		@Override
		public void enableSwitchOff() {
			switchOffEnabled=true;
			switchOnEnabled=false;
		}

		@Override
		public void disableSwitch() {
			switchOffEnabled=false;
			switchOnEnabled=false;
			
		}

		@Override
		public void setOffState() {
			state=STATE_OFF;
		}

		@Override
		public void setOnState() {
			state=STATE_ON;
		}

		@Override
		public void setDisconnectedState() {
			state=STATE_NO;
			
		}
		
		@Override
		public void setViewListener(IViewEventsListener eventsListener) {
			// TODO Auto-generated method stub
			
		}

		private String errorString;
		
		private String infoString;
		
		@Override
		public void showInfo(String string) {
			this.infoString=string;
			
		}

		@Override
		public void showError(String message) {
			this.errorString=message;
		}

		
		
	}
	
	class CommNameProviderDriver implements ICommNameProvider{
		
		String[] comms={"COM1", "COM2"};
		
		@Override
		public String[] getCommNames() {
			return comms;
		}
		
	}
	
	class ArduinoDriver implements ISwitch{
		ISwitchListener arduinoCommandListener;
		ICommPortStatusListener commPortStatusListener;
		
		
		COMMAND lastSendedCommand;
		RETURN desiredReturn;
		
		String commPort;
		COMM_PORT_STATUS desiredCommReturn;
		
		public ArduinoDriver(){
			arduinoCommandListener=null;
			commPortStatusListener=null;
			
			lastSendedCommand=null;
			desiredReturn=RETURN.OK;
			
			commPort=null;
			desiredCommReturn=COMM_PORT_STATUS.CONNECTED;
		}
		
		@Override
		public void sendCommand(COMMAND command) {
			lastSendedCommand=command;
			
//			if(arduinoCommandListener!=null){
//				arduinoCommandListener.arduinoCommandReturn(command, desiredReturn);
//			}
			
		}

		@Override
		public void setSwtichCommandReceiver(
				ISwitchListener arduinoCommandListener) {
			this.arduinoCommandListener=arduinoCommandListener;
			
		}

		@Override
		public void setCommPort(String port){
			
			if(commPortStatusListener!=null){
				if(port!="COM1"&&port!="COM2"){
					commPortStatusListener.onCommPortStatusChanged(COMM_PORT_STATUS.NO_CONNECTED);
				}
				else{
					commPortStatusListener.onCommPortStatusChanged(desiredCommReturn);
					this.commPort=port;
				}
			}
			
			

		}
		

		@Override
		public void setCommPortStatusListener(
				ICommPortStatusListener commPortStatusListener) {
			this.commPortStatusListener=commPortStatusListener;
			
		}
		
		void sendCommandAsReceived(COMMAND command, RETURN ret){
			if(arduinoCommandListener!=null)
				arduinoCommandListener.switchCommandReturn(command, ret);
		}
		
	}

	
	private Controller controller;
	
	private ViewStub viewStub;
	
	private CommNameProviderDriver commNameProviderDriver;
	
	private ArduinoDriver arduinoDriver;
	
	@Before
	public void setup(){
		viewStub = new ViewStub();
		commNameProviderDriver = new CommNameProviderDriver();
		arduinoDriver = new ArduinoDriver();
		
		controller= new Controller(viewStub, arduinoDriver);
	}
	
	@Test
	public void changeCommOFF(){
		controller.onChangeCommTo("COM1");
		assertEquals(COMMAND.IDENTIFY, arduinoDriver.lastSendedCommand);
		assertEquals("COM1", arduinoDriver.commPort);
		assertEquals(viewStub.STATE_NO, viewStub.state);
		assertEquals(false, viewStub.switchOffEnabled);
		assertEquals(false, viewStub.switchOnEnabled);
		
		arduinoDriver.sendCommandAsReceived(COMMAND.IDENTIFY, RETURN.OFF);
		
		assertEquals("COM1", arduinoDriver.commPort);
		assertEquals(viewStub.STATE_OFF, viewStub.state);
		assertEquals(false, viewStub.switchOffEnabled);
		assertEquals(true, viewStub.switchOnEnabled);
		assertNotNull(viewStub.infoString);
		assertNull(viewStub.errorString);
	}
	
	@Test
	public void changeCommON(){
		controller.onChangeCommTo("COM1");
		assertEquals(COMMAND.IDENTIFY, arduinoDriver.lastSendedCommand);
		assertEquals("COM1", arduinoDriver.commPort);
		assertEquals(viewStub.STATE_NO, viewStub.state);
		assertEquals(false, viewStub.switchOffEnabled);
		assertEquals(false, viewStub.switchOnEnabled);
		
		arduinoDriver.sendCommandAsReceived(COMMAND.IDENTIFY, RETURN.ON);
		
		assertEquals("COM1", arduinoDriver.commPort);
		assertEquals(viewStub.STATE_ON, viewStub.state);
		assertEquals(true, viewStub.switchOffEnabled);
		assertEquals(false, viewStub.switchOnEnabled);
		assertNotNull(viewStub.infoString);
		assertNull(viewStub.errorString);
	}
	
	
	@Test
	public void changeCommNotCommAvailableException(){
		controller.onChangeCommTo("COM4");
		assertNotSame(arduinoDriver.commPort, "COM4");
		assertNotNull(viewStub.errorString);
		assertNull(viewStub.infoString);
		assertEquals(viewStub.STATE_NO, viewStub.state);
	}

	
	@Test
	public void switchOnOk(){
		//Init, we "read" the Strings
		changeCommOFF();viewStub.errorString=null;viewStub.infoString=null;
		
		controller.onSwitchOn();
		assertEquals(ISwitch.COMMAND.SWITCH_ON, arduinoDriver.lastSendedCommand);
		assertEquals(viewStub.STATE_OFF, viewStub.state);
		assertEquals(false, viewStub.switchOffEnabled);
		assertEquals(false, viewStub.switchOnEnabled);
		
		arduinoDriver.sendCommandAsReceived(COMMAND.SWITCH_ON, RETURN.OK);
		
		assertEquals(viewStub.STATE_ON, viewStub.state);
		assertEquals(true, viewStub.switchOffEnabled);
		assertEquals(false, viewStub.switchOnEnabled);
		assertNotNull(viewStub.infoString);
		assertNull(viewStub.errorString);
	}
	
	@Test
	public void switchOffOk(){
		//Init, we "read" the Strings
		changeCommON();viewStub.errorString=null;viewStub.infoString=null;
		
		controller.onSwitchOff();
		assertEquals(ISwitch.COMMAND.SWITCH_OFF, arduinoDriver.lastSendedCommand);
		assertEquals(viewStub.STATE_ON, viewStub.state);
		assertEquals(false, viewStub.switchOffEnabled);
		assertEquals(false, viewStub.switchOnEnabled);
		
		arduinoDriver.sendCommandAsReceived(COMMAND.SWITCH_OFF, RETURN.OK);
		
		assertEquals(viewStub.STATE_OFF, viewStub.state);
		assertEquals(false, viewStub.switchOffEnabled);
		assertEquals(true, viewStub.switchOnEnabled);
		assertNotNull(viewStub.infoString);
		assertNull(viewStub.errorString);
	}
	
	
	@Test
	public void switchOnFail(){
		//Init, we "read" the Strings
		changeCommOFF();viewStub.errorString=null;viewStub.infoString=null;
		
		controller.onSwitchOn();
		assertEquals(ISwitch.COMMAND.SWITCH_ON, arduinoDriver.lastSendedCommand);
		assertEquals(viewStub.STATE_OFF, viewStub.state);
		assertEquals(false, viewStub.switchOffEnabled);
		assertEquals(false, viewStub.switchOnEnabled);
		
		arduinoDriver.sendCommandAsReceived(COMMAND.SWITCH_ON, RETURN.FAIL);
		
		assertEquals(viewStub.STATE_OFF, viewStub.state);
		assertEquals(false, viewStub.switchOffEnabled);
		assertEquals(true, viewStub.switchOnEnabled);
		assertNotNull(viewStub.errorString);
		assertNull(viewStub.infoString);
	}
	
	@Test
	public void switchOffFail(){
		//Init, we "read" the Strings
		changeCommON();viewStub.errorString=null;viewStub.infoString=null;
		
		controller.onSwitchOff();
		assertEquals(ISwitch.COMMAND.SWITCH_OFF, arduinoDriver.lastSendedCommand);
		assertEquals(viewStub.STATE_ON, viewStub.state);
		assertEquals(false, viewStub.switchOffEnabled);
		assertEquals(false, viewStub.switchOnEnabled);
		
		arduinoDriver.sendCommandAsReceived(COMMAND.SWITCH_OFF, RETURN.FAIL);
		
		assertEquals(viewStub.STATE_ON, viewStub.state);
		assertEquals(true, viewStub.switchOffEnabled);
		assertEquals(false, viewStub.switchOnEnabled);
		assertNotNull(viewStub.errorString);
		assertNull(viewStub.infoString);
	}
	
	@Test
	public void reset(){
		//Init, we "read" the Strings
		changeCommON();viewStub.errorString=null;viewStub.infoString=null;
		
		controller.onReset();
		assertEquals(false, viewStub.switchOffEnabled);
		assertEquals(false, viewStub.switchOnEnabled);
		assertEquals(viewStub.STATE_NO, viewStub.state);
		
	}
	
	@Test(expected=AssertionError.class)
	public void notWaitedResponse(){
		arduinoDriver.sendCommandAsReceived(COMMAND.SWITCH_OFF, RETURN.FAIL);
	}
	
}
