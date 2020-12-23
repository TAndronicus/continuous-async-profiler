package pl.ks.profiling.asyncprofiler;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
class ContinuousAsyncProfilerCompressor implements Runnable {
    private static final long ONE_MINUTE = 1000 * 60;
    private static final long TEN_MINUTES = ONE_MINUTE * 10;
    private final ContinuousAsyncProfilerProperties properties;

    @Override
    @SuppressWarnings("BusyWait")
    public void run() {
        Path continuousDir = Paths.get(properties.getContinuousOutputDir());
        BiPredicate<Path, BasicFileAttributes> predicate = (p, basicFileAttributes) -> p.getFileName().toString().endsWith("jfr");
        Comparator<Path> pathComparator = (o1, o2) -> {
            long firstModified = o1.toFile().lastModified();
            long secondModified = o2.toFile().lastModified();
            if (firstModified > secondModified) {
                return 1;
            }
            if (firstModified < secondModified) {
                return -1;
            }
            return 0;
        };

        while (!Thread.interrupted()) {
            try {
                List<Path> notCompressedFiles = Files.find(continuousDir, 1, predicate)
                        .sorted(pathComparator)
                        .collect(Collectors.toList());
                int counter = notCompressedFiles.size() - 2;
                for (Path source : notCompressedFiles) {
                    if (counter <= 0) {
                        break;
                    }
                    Path target = Paths.get(source.toAbsolutePath().toString() + ".gz");
                    compressGzip(source, target);
                    source.toFile().delete();
                    counter--;
                }

                Thread.sleep(TEN_MINUTES);
            } catch (InterruptedException e) {
                log.error("Cannot list dir: " + properties.getContinuousOutputDir(), e);
            } catch (IOException e) {
                log.error("Some IO failed", e);
            }
        }
    }

    public static void compressGzip(Path source, Path target) throws IOException {
        try (GZIPOutputStream gos = new GZIPOutputStream(new FileOutputStream(target.toFile()))) {
            Files.copy(source, gos);
        }
    }
}
