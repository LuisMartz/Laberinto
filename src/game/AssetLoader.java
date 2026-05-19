package game;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public final class AssetLoader {
    private static final String ASSET_DIR = "assets";

    private AssetLoader() {
    }

    public static File file(String name) {
        return new File(ASSET_DIR, name);
    }

    public static ImageIcon icon(String name) {
        return new ImageIcon(file(name).getPath());
    }

    public static Image image(String name) {
        return icon(name).getImage();
    }

    public static BufferedImage imageBuffer(String name) throws IOException {
        return ImageIO.read(file(name));
    }
}
