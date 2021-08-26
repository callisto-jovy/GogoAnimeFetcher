package net.bplaced.abzzezz.gogoanime.tasks;

import com.github.kokorin.jaffree.LogLevel;
import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import com.github.kokorin.jaffree.ffmpeg.UrlOutput;
import net.bplaced.abzzezz.gogoanime.GogoAnimeFetcher;
import net.bplaced.abzzezz.gogoanime.util.*;
import org.json.JSONArray;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EpisodeDownloadTask extends TaskExecutor implements Callable<Void> {
    protected final int[] count; //Start, end, current
    protected final JSONArray referrals; //All referrals

    protected final File outDir;
    protected final EpisodeDownloadProgressHandler progressHandler;
    private final EpisodeDownloadCallback userCallback;
    protected File outFile;

    private int totalBytes, progress;

    public EpisodeDownloadTask(JSONArray referrals, File outDir, int[] count, final EpisodeDownloadCallback userCallback) {
        this.referrals = referrals;
        this.count = count;
        this.outDir = outDir;
        this.userCallback = userCallback;
        //
        this.progressHandler = new EpisodeDownloadProgressHandler() {
            @Override
            public void onDownloadCompleted() {
                if (!isCancelled()) {
                    if (count[2] < count[1]) {
                        count[2]++;
                        userCallback.onDownloadCompleted(count);
                    }
                }
            }

            //Callback for the download progress, in order to display it
            @Override
            public void onDownloadProgress(final long newReadBytes) {
                progress += newReadBytes;
                /*
                if (totalBytes == -1)
                    ConsoleUtil.displayProgressbar(integer -> integer < progress % 100);
                else
                    ConsoleUtil.displayProgressbar(progress, totalBytes);


                 */
            }

            //Receive the initial size & build the notification
            @Override
            public void receiveTotalSize(final int totalByteSize) {
                totalBytes = totalByteSize;
            }

            //Error callback
            @Override
            public void onErrorThrown(final String message) {
                cancelExecution(); //Cancel the execution first
                Logger.getAnonymousLogger().log(Level.SEVERE, message);
                try {
                    userCallback.onErrorThrown(message);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public void executeAsync() {
        super.executeAsync(this);
    }

    @Override
    public Void call() {
        Logger.getAnonymousLogger().log(Level.INFO, "Starting download task");

        if (!outDir.exists()) outDir.mkdir();
        this.outFile = new File(outDir, count[2] + ".mp4");

        GogoAnimeFetcher.fetchDownloadLink(referrals.getString(count[2])).ifPresent(api -> {
            try {
                final List<String> ffmpegArguments = new LinkedList<>();

                ffmpegArguments.add("-vcodec");
                ffmpegArguments.add("copy");
                ffmpegArguments.add("-c:a");
                ffmpegArguments.add("copy");
                ffmpegArguments.add("-acodec");
                ffmpegArguments.add("mp3");
                this.startFFDefaultTask(ffmpegArguments, api);

                progressHandler.onDownloadCompleted();
            } catch (final StringIndexOutOfBoundsException | IOException e) {
                progressHandler.onErrorThrown(getError(e));
                e.printStackTrace();
            }
        });
        return null;
    }

    /**
     * Cancel task
     */
    public void cancelExecution() {
        //Flush and close the stream if needed
        this.setCancelled(true);
    }

    /**
     * Starts a new ffmpeg task
     *
     * @param ffmpegArguments arguments to supply for ffmpeg
     * @param url             url to get the m3u8-segment count from
     * @param requestHeaders  Request headers for the m3u8 operation
     * @throws IOException url / connection exceptions from the m3u8-util
     */
    protected void startFFDefaultTask(final List<String> ffmpegArguments, final String url, final String[]... requestHeaders) throws IOException {
        System.out.println(url);
       // progressHandler.receiveTotalSize(M3U8Util.getSegments(url, requestHeaders)); //Receive total segment size & set the total to receive size

        System.out.println("Segments: " + totalBytes);

        final FFmpeg fFmpeg = FFmpeg
                .atPath(new File("ffmpeg/bin").toPath())
                .addInput(UrlInput.fromUrl(url))
                .addOutput(UrlOutput.toUrl(outFile.getAbsolutePath()));

        fFmpeg.setLogLevel(LogLevel.INFO);

        ffmpegArguments.forEach(fFmpeg::addArgument);
        fFmpeg.setOutputListener(s -> {
            System.out.println(s);
            if (s.contains("Opening"))
                progressHandler.onDownloadProgress(1); //Increment the progress with one (new segment is being opened)
        });
        fFmpeg.execute();
    }

    /* Get error message */

    /**
     * @param e exception to get the localized message from
     * @return the localized message warped with geterror
     */
    protected String getError(final Exception e) {
        return getError(Objects.requireNonNull(e.getLocalizedMessage()));
    }

    protected String getError(final String e) {
        return "Downloading failed: " + e;
    }

    /**
     * Interface for all download progress callbacks
     */
    public interface EpisodeDownloadProgressHandler {
        void onDownloadCompleted();

        void onDownloadProgress(long newReadBytes);

        void receiveTotalSize(int totalByteSize);

        void onErrorThrown(final String message);
    }

    /**
     * Interface for user callback
     */
    public interface EpisodeDownloadCallback {
        void onDownloadCompleted(int[] count);

        void onErrorThrown(final String message);
    }

}
