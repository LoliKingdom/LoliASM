package zone.rong.blahajasm;

import javax.annotation.Nullable;
import java.io.*;

public class BlahajPrinter {

    public static void prettyPrintClass(byte[] bytes, @Nullable File fileLocation) {
        FileOutputStream stream = null;
        if (fileLocation != null) {
            try {
                stream = new FileOutputStream(fileLocation);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (stream == null) {
            stream = new FileOutputStream(FileDescriptor.out);
        }
        try {
            stream.write(bytes);
            stream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
