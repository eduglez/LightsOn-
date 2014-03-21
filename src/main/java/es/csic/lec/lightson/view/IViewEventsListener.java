package es.csic.lec.lightson.view;

public interface IViewEventsListener {
	public void onSwitchOn();
	public void onSwitchOff();
	public void onChangeCommTo(String commName);
	public void onExit();
	public void onReset();
}
