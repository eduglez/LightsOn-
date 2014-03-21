package es.csic.lec.lightson.arduino;

public interface ISwitchListener {
	public void switchCommandReturn(ISwitch.COMMAND command, ISwitch.RETURN ret);
	
	public void onException(Throwable e);
	
}
