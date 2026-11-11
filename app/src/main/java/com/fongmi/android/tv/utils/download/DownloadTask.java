package com.fongmi.android.tv.utils.download;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class DownloadTask {

    public static final int WAITING = 0;
    public static final int RUNNING = 1;
    public static final int DONE = 2;
    public static final int ERROR = 3;
    public static final int CANCELED = 4;

    private static final AtomicLong SEQ = new AtomicLong();

    private final String id;
    private final String url;
    private final String name;
    private final Map<String, String> headers;
    private final long createTime;
    private volatile int status = WAITING;
    private volatile long total = -1;
    private volatile long downloaded = 0;
    private volatile String filePath;
    private volatile String error;
    private transient volatile okhttp3.Call call;

    public DownloadTask(String url, String name, Map<String, String> headers) {
        this.id = System.currentTimeMillis() + "-" + SEQ.incrementAndGet();
        this.url = url;
        this.name = name;
        this.headers = headers == null ? new HashMap<>() : new HashMap<>(headers);
        this.createTime = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public long getCreateTime() {
        return createTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getDownloaded() {
        return downloaded;
    }

    public void setDownloaded(long downloaded) {
        this.downloaded = downloaded;
    }

    public void addDownloaded(long bytes) {
        this.downloaded += bytes;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public okhttp3.Call getCall() {
        return call;
    }

    public void setCall(okhttp3.Call call) {
        this.call = call;
    }

    public int getPercent() {
        if (total <= 0) return 0;
        return (int) Math.min(100, downloaded * 100 / total);
    }

    public boolean isFinished() {
        return status == DONE || status == ERROR || status == CANCELED;
    }
}
