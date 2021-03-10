package be.selckin.chromeshot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class Store {
    private final Path target;

    public Store(Path target) {
        this.target = Objects.requireNonNull(target);
    }

    public void write(byte[] content, String name) throws IOException {
        Files.createDirectories(target);
        Files.write(target.resolve(name), content);
    }
}
