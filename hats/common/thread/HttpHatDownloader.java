package hats.common.thread;

import hats.common.Hats;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class HttpHatDownloader 
	implements Runnable 
{

	public String fileName;
	
	public File fileSaveLocation;
	
	public int maxFileSize;
	
	public void HTTPHatDownloader(String name, File loc)
	{
		fileName = name;
		fileSaveLocation = loc;
		maxFileSize = 100000; //100kb
	}
	
	@Override
	public void run() 
	{
        URLConnection urlconnection = null;
        InputStream inputstream = null;
        DataOutputStream dataoutputstream = null;

        try
        {
            byte[] abyte = new byte[4096];
            URL url = new URL(fileName);
            urlconnection = url.openConnection();
            float f = 0.0F;
            
            inputstream = urlconnection.getInputStream();
            float f1 = (float)urlconnection.getContentLength();
            int i = urlconnection.getContentLength();

            if (this.fileSaveLocation.exists())
            {
                long j = this.fileSaveLocation.length();

                if (j == (long)i)
                {
                    return;
                }

                Hats.console("Deleting " + this.fileSaveLocation + " as it does not match what we currently have (" + i + " vs our " + j + ").", true);
                this.fileSaveLocation.delete();
            }

            dataoutputstream = new DataOutputStream(new FileOutputStream(this.fileSaveLocation));
            
            if (this.maxFileSize > 0 && f1 > (float)this.maxFileSize)
            {
            	Hats.console("Filesize is bigger than maximum allowed (file is " + f1 + ", limit is " + this.maxFileSize + ")", true);
            }

            boolean flag = false;
            int k;

            while ((k = inputstream.read(abyte)) >= 0)
            {
                f += (float)k;

                if (this.maxFileSize > 0 && f > (float)this.maxFileSize)
                {
                    throw new IOException("Filesize was bigger than maximum allowed (got >= " + f + ", limit was " + this.maxFileSize + ")");
                }

                dataoutputstream.write(abyte, 0, k);
            }

        }
        catch (Throwable throwable)
        {
            throwable.printStackTrace();
        }
        finally
        {
            try
            {
                if (inputstream != null)
                {
                    inputstream.close();
                }
            }
            catch (IOException ioexception)
            {
                ;
            }

            try
            {
                if (dataoutputstream != null)
                {
                    dataoutputstream.close();
                }
            }
            catch (IOException ioexception1)
            {
                ;
            }
        }

	}

}
