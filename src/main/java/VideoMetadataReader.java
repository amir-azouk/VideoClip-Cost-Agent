import org.mp4parser.IsoFile;
import org.mp4parser.boxes.iso14496.part12.MovieBox;
import org.mp4parser.boxes.iso14496.part12.MovieHeaderBox;

import java.io.File;
import java.io.IOException;

public class VideoMetadataReader {

    public static int readDurationSeconds(File videoFile) throws IOException {
        try (IsoFile isoFile = new IsoFile(videoFile.getAbsolutePath())) {
            MovieBox movieBox = isoFile.getMovieBox();
            if (movieBox == null) {
                throw new IOException("Could not find movie metadata in this file.");
            }

            MovieHeaderBox header = movieBox.getMovieHeaderBox();
            long duration = header.getDuration();
            long timescale = header.getTimescale();

            double durationSeconds = (double) duration / timescale;
            return (int) Math.round(durationSeconds);
        }
    }
}