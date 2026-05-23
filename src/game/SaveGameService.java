package game;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class SaveGameService {
    public File createTimestampedSaveFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File("partida_" + timeStamp + ".txt");
    }

    public void save(File file, MazeState mazeState, SkillTreeProgression skillTreeProgression) throws IOException {
        Properties properties = mazeState.toProperties();
        skillTreeProgression.save(properties);
        try (FileOutputStream output = new FileOutputStream(file)) {
            properties.store(output, "Laberinto save");
        }
    }

    public void save(File file, MazeState mazeState, SkillTreeProgression skillTreeProgression, PlayerData playerData) throws IOException {
        Properties properties = mazeState.toProperties();
        skillTreeProgression.save(properties);
        playerData.save(properties);
        try (FileOutputStream output = new FileOutputStream(file)) {
            properties.store(output, "Laberinto save");
        }
    }

    public void load(File file, MazeState mazeState, SkillTreeProgression skillTreeProgression) throws IOException {
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream(file)) {
            properties.load(input);
        }
        mazeState.load(properties);
        skillTreeProgression.load(properties);
    }

    public void load(File file, MazeState mazeState, SkillTreeProgression skillTreeProgression, PlayerData playerData) throws IOException {
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream(file)) {
            properties.load(input);
        }
        mazeState.load(properties);
        playerData.load(properties);
        skillTreeProgression.load(properties);
    }
}
