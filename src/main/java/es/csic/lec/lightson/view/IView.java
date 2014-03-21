package es.csic.lec.lightson.view;

public interface IView {
	
	public void enableSwitchOn();
	public void enableSwitchOff();
	public void disableSwitch();
	
	public void setOffState();
	public void setOnState();
	public void setDisconnectedState();
	
	public void setViewListener(IViewEventsListener viewListener);
	public void showInfo(String string);
	public void showError(String message);
	
}
