/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.w;

import com.futurice.tantalum3.TantalumMIDlet;
import com.futurice.tantalum3.Task;
import com.futurice.tantalum3.Worker;
import com.futurice.tantalum3.log.L;
import com.futurice.tantalum3.net.StaticWebCache;
import com.futurice.tantalum3.net.xml.RSSItem;
import com.futurice.tantalum3.net.xml.RSSModel;
import com.futurice.tantalum3.rms.DataTypeHandler;
import java.io.IOException;
import java.io.OutputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
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
public class JPodcast extends TantalumMIDlet implements CommandListener {

    private DisplayManager manager;
    private Displayable screen1;
    private Displayable screen2;
    private Displayable screen3;
    private Command back;
    private Command next;
    private Command cmdSel;
    private StaticWebCache feedCache;
    private RSSModel rssModel;
    private List lEpisodes;
            
    /**
     * Creates several screens and navigates between them.
     */
    public JPodcast() {
        super(4);
        rssModel = new RSSModel(50);
        feedCache = new StaticWebCache('5', new DataTypeHandler() {
            public Object convertToUseForm(byte[] bytes) {
                try {
                    L.i("", bytes.toString());
                    rssModel.setXML(bytes);

                    return rssModel;
                } catch (Exception e) {                    
                    L.i("Error in parsing XML", rssModel.toString());
                    return null;
                }
            }
        });        
        this.manager = new DisplayManager(Display.getDisplay(this));
        this.back = new Command("Back", Command.BACK, 1);
        this.next = new Command("Next", Command.OK, 1);
        cmdSel = new Command("Select", Command.ITEM, 1);

        this.screen1 = getSreen1();
        //this.screen1.setCommandListener(this);
        this.screen1.addCommand(this.back);
        this.screen1.addCommand(this.next);

        this.screen2 = getSreen2();
        //this.screen2.setCommandListener(this);
        this.screen2.addCommand(this.back);
        this.screen2.addCommand(this.next);

        this.screen3 = getSreen3();
        this.screen3.setCommandListener(this);
        this.screen3.addCommand(this.back);
        this.screen3.addCommand(this.next);
    }

    private void dumpItems() {
        int s = rssModel.size();
        L.i("", "Got els: " + s);
        lEpisodes.deleteAll();
        for (int i=0; i < s; ++i) {
            RSSItem it = rssModel.elementAt(i);
            L.i("", "Item " + it.getTitle());
            lEpisodes.append(it.getTitle(), null);
            
        }
        
    }
    private void startFetch(String url) {
        L.i("", "Starting to fetch");
        final Task t = new Task() {

            protected Object doInBackground(Object in) {
                L.i("", "Fetch done!!!");
                dumpItems();
                return in;
                
                    
            }
            
        };
        feedCache.update(url, t);


    }

    private Displayable getSreen1() {
        List l = new List("List [Screen 1]", List.IMPLICIT);
        l.append("Engadget podcast", null);
        l.append("The verge podcast", null);
        final JPodcast app = this;
        l.setSelectCommand(cmdSel);
        l.setCommandListener(new CommandListener() {
            public void commandAction(Command arg0, Displayable arg1) {
                L.i("", "Podcast select!");
                // TODO Auto-generated method stub
                app.manager.next(app.screen2);
                startFetch("http://feeds2.feedburner.com/TheLinuxActionShow");


            }
        });
        return l;

    }

    private void startDownload(String url) {
        String initDir = System.getProperty("fileconn.dir.music");
        int i = url.lastIndexOf('/');
        String basename = url.substring(i+1);
        String trg = initDir + "/" + basename;
        L.i("", "Get to " + trg);
        try {
            FileConnection fc = (FileConnection) Connector.open(trg, 
                    Connector.WRITE);
            fc.create();
            OutputStream os = fc.openOutputStream();
            final HttpStreamGetter httpGetter = new HttpStreamGetter(url, 
                    0, os );
                        
                    
            Worker.fork(httpGetter);
        } catch (IOException e) {
            L.e("", "Cannot open file", e);
        }
        
        
    }
    
    private Displayable getSreen2() {
        lEpisodes = new List("Episodes", List.IMPLICIT);
        lEpisodes.setCommandListener(new CommandListener() {

            public void commandAction(Command c, Displayable d) {
                L.i("", "Select episode");
                int i = lEpisodes.getSelectedIndex();
                RSSItem it = rssModel.elementAt(i);
                String link = it.getLink();
                String tgurl = (String) it.getOther().get("enclosure.url");
                L.i("", "Link: "  + tgurl);
                startDownload(tgurl);
                
                
                
                
                
                
            }
        });
        return lEpisodes;
                
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
            } else if (displayable == this.screen2) {
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
    //protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {}

    /* (non-Javadoc)
     * @see javax.microedition.midlet.MIDlet#pauseApp()
     */
    protected void pauseApp() {
    }
}
