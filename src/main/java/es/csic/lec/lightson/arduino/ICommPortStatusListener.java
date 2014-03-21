package es.csic.lec.lightson.arduino;

public interface ICommPortStatusListener {
	public void onCommPortStatusChanged(ISwitch.COMM_PORT_STATUS commPortStatus);
	
	public void onCommPortException(Exception e);
	
}
