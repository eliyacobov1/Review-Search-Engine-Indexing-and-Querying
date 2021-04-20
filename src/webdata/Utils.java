package webdata;

import java.io.Closeable;
import java.io.IOException;

public class Utils
{
    /**
     * safely closes the 2 given streams
     */
    public static void safelyCloseStreams(Closeable f1, Closeable f2)
    {
        if (f1 != null)
        {
            try { f1.close(); }
            catch (IOException e) { e.printStackTrace(); }
        }

        if (f2 != null)
        {
            try { f2.close(); }
            catch (IOException e) { e.printStackTrace(); }
        }
    }
}
