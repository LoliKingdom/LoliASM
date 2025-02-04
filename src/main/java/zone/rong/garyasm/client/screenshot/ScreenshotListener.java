package zone.rong.garyasm.client.screenshot;

import net.minecraftforge.client.event.ScreenshotEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import zone.rong.garyasm.GaryLogger;

import java.awt.*;
import java.awt.datatransfer.*;

public class ScreenshotListener {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onScreenshot(ScreenshotEvent event) {
        if (!event.isCanceled() && event.getScreenshotFile() != null) {
            GaryLogger.instance.info("Copied screenshot {} to clipboard!", event.getScreenshotFile().getName());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new TransferableImage(event.getImage()), null);
        }
    }

    private static class TransferableImage implements Transferable {

        final Image i;

        TransferableImage(Image i) {
            this.i = i;
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (flavor.equals(DataFlavor.imageFlavor) && i != null) {
                return i;
            } else {
                throw new UnsupportedFlavorException(flavor);
            }
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            DataFlavor[] flavors = new DataFlavor[1];
            flavors[0] = DataFlavor.imageFlavor;
            return flavors;
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(DataFlavor.imageFlavor);
        }

    }

}
