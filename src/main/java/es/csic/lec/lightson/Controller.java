package es.csic.lec.lightson;

import es.csic.lec.lightson.arduino.ISwitchListener;
import es.csic.lec.lightson.arduino.ISwitch;
import es.csic.lec.lightson.arduino.ICommPortStatusListener;
import es.csic.lec.lightson.arduino.ISwitch.COMMAND;
import es.csic.lec.lightson.arduino.ISwitch.COMM_PORT_STATUS;
import es.csic.lec.lightson.arduino.ISwitch.RETURN;
import es.csic.lec.lightson.view.IViewEventsListener;
import es.csic.lec.lightson.view.IView;

public class Controller implements IViewEventsListener, ICommPortStatusListener, ISwitchListener{
	private IView view;
	
	private ISwitch arduino;
	
	private ISwitch.COMMAND waitingFor;
	
	public Controller(IView view, ISwitch arduino){
		this.view=view;
		view.setViewListener(this);
		
		this.arduino=arduino;
		arduino.setSwtichCommandReceiver(this);
		arduino.setCommPortStatusListener(this);
		
		waitingFor=COMMAND.NOTHING;

	}


	@Override
	public void switchCommandReturn(COMMAND command, RETURN ret) {
		switch(command){
			case SWITCH_OFF:
				if(waitingFor.equals(COMMAND.SWITCH_OFF)){
					waitingFor=COMMAND.NOTHING;
					
					if(ret.equals(RETURN.OK)){
						view.enableSwitchOn();
						view.setOffState();
						view.showInfo("Switch Off OK");
					}else{
						view.enableSwitchOff();
						view.setOnState();
						view.showError("Switch Off ERROR");
					}
				}else{
					assert false:"Not waitingFor a SwitchOFF";
				}
				
			break;
			
			case SWITCH_ON:
				if(waitingFor.equals(COMMAND.SWITCH_ON)){
					waitingFor=COMMAND.NOTHING;
					
					if(ret.equals(RETURN.OK)){
						view.enableSwitchOff();
						view.setOnState();
						view.showInfo("Switch On OK");
					}else{
						view.enableSwitchOn();
						view.setOffState();
						view.showError("Switch On ERROR");
					}
				}else{
					assert false:"Not waitingFor a SwitchOn";
				}
			break;
			
			case IDENTIFY:
				//if(waitingFor.equals(COMMAND.IDENTIFY)){
					waitingFor=COMMAND.NOTHING;
					
					if(ret.equals(RETURN.ON)){
						view.enableSwitchOff();
						view.setOnState();
						view.showInfo("Light is ON know");
					}else if(ret.equals(RETURN.OFF)){
						view.enableSwitchOn();
						view.setOffState();
						view.showInfo("Light is OFF know");
					}else{
						assert false:"IDENTIFY RETURN nor valid";
					}
				//}else{
				//	assert false:"Not waitingFor a Identify";
				//}
				break;
				
			default:
				assert false:"Not a valid return command";
				break;
		}
		
	}

	@Override
	public void onException(Throwable e) {
		view.showError(e.getMessage());
	}

	@Override
	public void onCommPortStatusChanged(COMM_PORT_STATUS commPortStatus) {
		switch(commPortStatus){
		case CONNECTED:
			waitingFor=COMMAND.IDENTIFY;
			arduino.sendCommand(COMMAND.IDENTIFY);
			view.showInfo("Succesfully Connected but checking Arduino");
			break;
		case NO_CONNECTED:
			view.setDisconnectedState();
			view.disableSwitch();
			view.showError("Disconnected");
			break;
		}
	}


	@Override
	public void onSwitchOn() {
		waitingFor=COMMAND.SWITCH_ON;
		view.disableSwitch();
		arduino.sendCommand(COMMAND.SWITCH_ON);
	}


	@Override
	public void onSwitchOff() {
		waitingFor=COMMAND.SWITCH_OFF;
		view.disableSwitch();
		arduino.sendCommand(COMMAND.SWITCH_OFF);
	}

	
	@Override
	public void onChangeCommTo(String commName){
		view.disableSwitch();
		view.setDisconnectedState();
		arduino.setCommPort(commName);
	}


	@Override
	public void onExit(){
        System.exit(0);
	}


	@Override
	public void onReset() {
        view.disableSwitch();
    	view.setDisconnectedState();
	}


	@Override
	public void onCommPortException(Exception e) {
		view.showError(e.getMessage());
		
	}
	
}
