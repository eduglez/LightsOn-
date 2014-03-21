package es.csic.lec.lightson.arduino;

public interface ISwitch {
	
	enum COMMAND{
		SWITCH_ON,
		SWITCH_OFF,
		IDENTIFY,
		NOTHING
	}
	
	enum RETURN{
		OK,
		FAIL,
		ON,
		OFF
	}
	
	enum COMM_PORT_STATUS{
		CONNECTED,
		NO_CONNECTED
	}
	
	public void setCommPort(String port);
	
	public void setCommPortStatusListener(ICommPortStatusListener commPortStatusListener);
	
	public void sendCommand(ISwitch.COMMAND command);
	
	public void setSwtichCommandReceiver(ISwitchListener switchCommandListener);
}
