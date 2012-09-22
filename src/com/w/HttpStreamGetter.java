/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.w;

import com.futurice.tantalum3.Task;
import com.futurice.tantalum3.log.L;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

/**
 * GET something from a URL on the Worker thread
 *
 * Implement Runnable if you want to automatically update the UI on the EDT
 * after the GET completes
 *
 * @author pahought
 */
public class HttpStreamGetter extends Task {

    private final String url;
    protected int retriesRemaining;
    protected byte[] postMessage = null;
    protected String requestMethod = HttpConnection.GET;
    private OutputStream ostream;

    /**
     * Get the contents of a URL and return that asynchronously as a AsyncResult
     *
     * @param url - where on the Internet to synchronousGet the data
     * @param retriesRemaining - how many time to attempt connection
     * @param task - optional object notified on the EDT with the task
     */
    public HttpStreamGetter(final String url, final int retriesRemaining, 
            OutputStream ostream) {
        if (url == null || url.indexOf(':') <= 0) {
            throw new IllegalArgumentException("HttpGetter was passed bad URL: " + url);
        }
        this.url = url;
        this.retriesRemaining = retriesRemaining;
        this.ostream = ostream;
    }

    public String getUrl() {
        return this.url;
    }

    public Object doInBackground(final Object in) {
        //#debug
        L.i(this.getClass().getName() + " start", url);
        ByteArrayOutputStream bos = null;
        HttpConnection httpConnection = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        boolean tryAgain = false;
        boolean success = false;
        String curUrl = this.url;
        int redirects = 0;
        try {
            while (true) {
                httpConnection = (HttpConnection) Connector.open(curUrl);
                httpConnection.setRequestMethod(requestMethod);
                if (postMessage != null) {
                    outputStream = httpConnection.openDataOutputStream();
                    outputStream.write(postMessage);
                }


                inputStream = httpConnection.openInputStream();
                int resultCode = httpConnection.getResponseCode();
                if (resultCode == HttpConnection.HTTP_MOVED_TEMP || resultCode == HttpConnection.HTTP_MOVED_PERM || resultCode == HttpConnection.HTTP_SEE_OTHER || resultCode == HttpConnection.HTTP_TEMP_REDIRECT) {
                    curUrl = httpConnection.getHeaderField("Location");
                    httpConnection.close();

                    if (++redirects > 5) {
                        // Too many redirects - give up.
                        break;
                    }

                    continue;
                } else {
                    break;
                }
            }
  
            final long length = httpConnection.getLength();
            if(true) {                
                L.i(this.getClass().getName() + " start variable length read", url);
                
                byte[] readBuffer = new byte[16384];
                while (true) {
                    final int bytesRead = inputStream.read(readBuffer);
                    if (bytesRead > 0) {
                        ostream.write(readBuffer, 0, bytesRead);
                    } else {
                        break;
                    }
                }                
                readBuffer = null;
            }

            //#debug
            L.i(this.getClass().getName(), "complete");
            success = true;
        } catch (IllegalArgumentException e) {
            //#debug
            L.e(this.getClass().getName() + " has a problem", url, e);
        } catch (IOException e) {
            //#debug
            L.e(this.getClass().getName() + " retries remaining", url + ", retries=" + retriesRemaining, e);
            if (retriesRemaining > 0) {
                retriesRemaining--;
                tryAgain = true;
            } else {
                //#debug
                L.i(this.getClass().getName() + " no more retries", url);
            }
        } catch (Exception e) {
            //#debug
            L.e(this.getClass().getName() + " has a problem", url, e);
        } finally {
            try {
                inputStream.close();
            } catch (Exception e) {
            }
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
            }
            try {
                if (bos != null) {
                    bos.close();
                }
            } catch (Exception e) {
            }
            try {
                httpConnection.close();
            } catch (Exception e) {
            }
            inputStream = null;
            outputStream = null;
            bos = null;
            httpConnection = null;

            if (tryAgain) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                }
                doInBackground(in);
            } else if (!success) {
                cancel(false);
            }
            //#debug
            L.i("End " + this.getClass().getName(), url);
            
            
        }
        return null;
    }
}
