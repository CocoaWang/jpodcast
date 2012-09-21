/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.w;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;
import javax.microedition.midlet.*;

/**
 * @author v6vainio
 */
public class JPodcast extends MIDlet implements CommandListener {
	private DisplayManager manager;
	private Displayable screen1;
	private Displayable screen2;
	private Displayable screen3;
	private Command	 back;
	private Command	 next;
	private Command cmdSel;
	
	/**
	 * Creates several screens and navigates between them.
	 */
	public JPodcast() {
		this.manager = new DisplayManager(Display.getDisplay(this));
		this.back = new Command("Back", Command.BACK, 1);
		this.next = new Command("Next", Command.OK, 1);
		cmdSel =    new Command("Select", Command.ITEM, 1 );
		
		this.screen1 = getSreen1();
		//this.screen1.setCommandListener(this);
		this.screen1.addCommand(this.back);
		this.screen1.addCommand(this.next);
		
		this.screen2 = getSreen2();
		this.screen2.setCommandListener(this);
		this.screen2.addCommand(this.back);
		this.screen2.addCommand(this.next);
		
		this.screen3 = getSreen3();
		this.screen3.setCommandListener(this);
		this.screen3.addCommand(this.back);
		this.screen3.addCommand(this.next);
	}

	private void startFetch(String url) {
		
	}
	private Displayable getSreen1() {
		List l = new List("List [Screen 1]", List.IMPLICIT);
		l.append("Engadget podcast", null);
		l.append("The verge podcast", null);
		final JPodcast app = this;
		l.setSelectCommand( cmdSel );
		l.setCommandListener(new CommandListener() {			
			public void commandAction(Command arg0, Displayable arg1) {				
				System.out.println("Podcast select!");
				// TODO Auto-generated method stub
				app.manager.next(app.screen2);
				startFetch("http://www.engadget.com/tag/podcasts/rss.xml");
				
				
			}
		});
		return l;
		
	}

	private Displayable getSreen2() {
		return new List("List [Screen 2]", List.IMPLICIT);
	}

	private Displayable getSreen3() {
		return new TextBox("Text [Screen 3]", "", 100, TextField.ANY);
	}
	
	/* (non-Javadoc)
	 * @see javax.microedition.midlet.MIDlet#startApp()
	 */
	protected void startApp() throws MIDletStateChangeException {
		this.manager.next(this.screen1);
	}
	
	/* (non-Javadoc)
	 * @see javax.microedition.lcdui.CommandListener#commandAction(javax.microedition.lcdui.Command, javax.microedition.lcdui.Displayable)
	 */
	public void commandAction(Command command, Displayable displayable) {
		System.out.println("Command");
		if (command == this.next) {
			if (displayable == this.screen1) {
				this.manager.next(this.screen2);
			} else
			if (displayable == this.screen2) {
				this.manager.next(this.screen3);
			}
		}
		
		if (command == this.back) {
			this.manager.back();
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.microedition.midlet.MIDlet#destroyApp(boolean)
	 */
	protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {}

	/* (non-Javadoc)
	 * @see javax.microedition.midlet.MIDlet#pauseApp()
	 */
	protected void pauseApp() {}


}
