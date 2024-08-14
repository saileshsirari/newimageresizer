package apps.sai.com.imageresizer.util;

import androidx.annotation.NonNull;

import java.io.PrintWriter;
import java.io.Writer;

/**
 * Created by sailesh on 04/01/18.
 */

public class Logger extends PrintWriter {
    public Logger(@NonNull Writer out, boolean autoFlush) {
        super(out,autoFlush);

        
    }
}
