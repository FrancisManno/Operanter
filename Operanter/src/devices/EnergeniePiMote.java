package devices;

import op.Defaults;

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;

import db.LogEvent;

public class EnergeniePiMote extends DigitalOutput{
	
	Pin[] pinids;
	GpioPinDigitalOutput[] pins=new GpioPinDigitalOutput[6];
	int sockID=0;
	boolean verbose=true;
	int[][] controlmat={{0,0,1,1},{0,1,1,1},{0,1,1,0},{0,1,0,0}}; //all off, 1 off, 2 off, 4 off
	
	
	
	public EnergeniePiMote(Pin[] pinids, int sockID, String name,  String experimentName, String experimentType, Defaults defaults){
		super(pinids[0], name, experimentName, experimentType, defaults);
		this.pinids=pinids;
		this.sockID=sockID; //1
		try{
			for (int i=1; i<4; i++){
				System.out.println(controlmat[sockID][i]);
				if (controlmat[sockID][i]==1){
					pins[i] = gpio.provisionDigitalOutputPin(pinids[i], name, PinState.HIGH);
				}
				else{
					pins[i] = gpio.provisionDigitalOutputPin(pinids[i], name, PinState.LOW);
				}				
				pins[i].setShutdownOptions(true, PinState.LOW);
			}
			for (int i=4; i<6; i++){
				pins[i] = gpio.provisionDigitalOutputPin(pinids[i], name, PinState.LOW);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}		
	}
	
	public void toggle(){
		try{
			System.out.println(pin.getName()+" "+pinid.getName());
			pin.toggle();
			settle();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		LogEvent le=new LogEvent(name, "Toggle", experimentName, experimentType, pinid.getName());
		dbc.writeToDatabase(le);
	}
	
	public void pulseOn(int length){	
		if (pin.isHigh()){	
		}
		else{
			try{
				System.out.println(pin.getName());
				verbose=false;
				switchOn();
				MakePause2 mp2=new MakePause2(length, true);
				mp2.start();
				LogEvent le=new LogEvent(name, "PulseOn", experimentName, experimentType, pinid.getName());
				dbc.writeToDatabase(le);
			}
			catch(Exception e){
			
			}
			
		}
	}
	
	public void pulseOff(int length){	
		if (pin.isLow()){
		}
		else{
			try{
				System.out.println(pin.getName());
				verbose=false;
				switchOff();
				MakePause2 mp2=new MakePause2(length, false);
				mp2.start();
				LogEvent le=new LogEvent(name, "PulseOff", experimentName, experimentType, pinid.getName());
				dbc.writeToDatabase(le);
			}
			catch(Exception e){
			
			}	
		}
	}
	
	public void flash(int lengthOn, int lengthOff, int reps){	
		
	}
	
	public void switchOn(){
		pin.high();
		settle();
		if (verbose){
			LogEvent le;
			try {
				le = new LogEvent(name, "SwitchOn", experimentName, experimentType, pinid.getName());
				dbc.writeToDatabase(le);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		verbose=true;
	}

	public void switchOff(){
		pin.low();
		settle();
		if (verbose){
			LogEvent le;
			try {
				le = new LogEvent(name, "SwitchOff", experimentName, experimentType, pinid.getName());
				dbc.writeToDatabase(le);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		verbose=true;
	}
	

	
	public void settle(){
		MakePause mp=new MakePause();
		mp.start();
	}
	
	class MakePause extends Thread{
		
		/**
		 * Operates some code the Energenie needs to function properly.
		 * (It has to "settle" the 4 boolean GPIO pins before it can send that signal
		 * to the lineariser output pin.
		 * pins[5] should be connected to the Energenie modulator pin.
		 *
		 */
		public MakePause(){

		}

		public void run(){
			try{
				Thread.sleep(100);
				pins[5].high();
				Thread.sleep(250);
				pins[5].low();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	
	class MakePause2 extends Thread{
		
		int t;
		boolean x;
		
		/**
		 * Allows pulsing of the Energenie. (e.g. off and on again)
		 * @param t pause before the condition is reversed.
		 * @param x a boolean that states whether it is pulsed on or off.
		 */
		public MakePause2(int t, boolean x){
			this.t=t;
			this.x=x;
		}

		public void run(){
			try{
				if (t>0){
					Thread.sleep(t);
					verbose=false;
					if(x){
						switchOff();
					}
					else{
						switchOn();
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}
