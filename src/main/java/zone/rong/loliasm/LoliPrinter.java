package zone.rong.loliasm;

import javax.annotation.Nullable;
import java.io.*;

public class LoliPrinter {

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
