package com.app.listapp.tiff;

/**
 * Created by beyka on 4/26/17.
 */

public interface IProgressListener {
    public void reportProgress(long processedPixels, long totalPixels);
}
