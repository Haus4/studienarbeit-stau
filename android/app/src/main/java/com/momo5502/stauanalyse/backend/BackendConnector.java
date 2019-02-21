package com.momo5502.stauanalyse.backend;

import com.momo5502.stauanalyse.position.Direction;
import com.momo5502.stauanalyse.util.Callback;
import com.momo5502.stauanalyse.util.Downloader;

import java.util.Date;

public class BackendConnector {

    private final String host;

    public BackendConnector(String host) {
        this.host = host;
    }

    public void fetch(Downloader downloader, final Callback<byte[]> callback, String camera, Date after) {
        String url = "/fetcher.php?camera=" + camera + (after != null ? "&time=" + (after.getTime() / 1000) : "");
        perform(downloader, callback, url);
    }

    public void mask(Downloader downloader, final Callback<byte[]> callback, String camera, Direction direction) {
        String url = "/masks.php?orientation=" + direction.getIndex() + "&id=" + camera;
        perform(downloader, callback, url);
    }

    public void cameras(Downloader downloader, final Callback<byte[]> callback) {
        String url = "/kameras.php";
        perform(downloader, callback, url);
    }

    private void perform(Downloader downloader, final Callback<byte[]> callback, String url) {
        downloader.download(callback, host + url, null);
    }
}
